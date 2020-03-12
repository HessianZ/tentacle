package cn.hessian.xiaoai.repository

import cn.hessian.xiaoai.Config
import cn.hessian.xiaoai.DB
import cn.hessian.xiaoai.camelToUnderscore
import cn.hessian.xiaoai.domain.Domain
import cn.hessian.xiaoai.domain.Timestamp
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType

abstract class Repository<T : Domain> {

  private val log = LoggerFactory.getLogger(this::class.java)

  abstract val tableName: String

  abstract val entityClass: KClass<T>

  suspend fun getById(id : Int) : T? {
    return getByColumn("id", id.toString())
  }

  suspend fun getByColumn(column: String, value: String) : T? {
    return findFirstByWhere("\"$column\" = '$value'")
  }

  suspend fun findByWhere(where: String?, order: String? = null, offset: Int = 0, limit: Int = 0) : List<T> {

    val sql = StringBuffer("SELECT * FROM \"${tableName}\"")

    if (where != null && where.isNotBlank()) {
      sql.append(" WHERE $where")
    }

    if (order != null && order.isNotBlank()) {
      sql.append(" ORDER BY $order")
    }

    if (limit > 0) {
      sql.append(" LIMIT $offset, $limit")
    }

    val result = ArrayList<T>()

    DB.queryRows(sql.toString()).forEach {
      val obj: T = it.mapTo(entityClass)
      result.add(obj)
    }

    return result
  }

  suspend fun findByColumn(column: String, value: String, order: String? = null) : List<T> {
    return findByWhere("\"$column\" = '$value'", order)
  }

  suspend fun findFirstByWhere(where: String, order: String? = null) : T? {
    return findByWhere(where, order).firstOrNull()
  }

  suspend fun countByWhere(where: String?) : Int {
    var sql = "SELECT COUNT(*) count FROM \"${tableName}\""
    if (where != null && where.isNotBlank()) {
      sql += " WHERE $where"
    }
    return DB.queryRow(sql)?.getInteger(0) ?: 0
  }

  suspend fun execute(sql: String) : Int {
    return DB.queryRow(sql)?.getInteger(0) ?: 0
  }

  suspend fun insert(obj: T): Int {
    val fields = mutableListOf<String>()
    val values = mutableListOf<String>()

    if (obj is Timestamp) {
      val now = LocalDateTime.now()
      obj.createdAt = now
      obj.modifiedAt = now
    }

    entityClass.members.filter { it is KProperty }.forEach {
      if (it.name != "id") {
          fields.add(it.name.camelToUnderscore())

            val value = it.call(obj)
          values.add(when (value) {
          is Number -> value.toString()
          is Enum<*> -> value.ordinal.toString()
          is LocalDateTime -> "'${value.format(Config.DEFAULT_DATETIME_FORMATTER)}'"
          is JsonObject -> "'${Json.encode(value)}'"
          else -> "'$value'"
        })
      }
    }

    val fieldsString = fields.joinToString(",")
    val valuesString = values.joinToString(",")

    val sql = "INSERT INTO ${tableName}($fieldsString) VALUES($valuesString) RETURNING id"

    obj.id = execute(sql)

    return obj.id!!
  }

  suspend fun update(obj: T) : Boolean {
    val fields = mutableListOf<String>()

    if (obj is Timestamp) {
      obj.modifiedAt = LocalDateTime.now()
    }

    entityClass.members.filter { it is KProperty }.forEach {
      if (it.name != "id") {
        val value = it.call(obj)
        fields.add("\"${it.name.camelToUnderscore()}\" = " + when (value) {
          is Number -> value.toString()
          is Enum<*> -> value.ordinal.toString()
          is LocalDateTime -> "'${value.format(Config.DEFAULT_DATETIME_FORMATTER)}'"
          is JsonObject -> "'${Json.encode(value)}'"
          else -> "'$value'"
        })
      }
    }

    val fieldsString = fields.joinToString(",")

    val sql = "UPDATE ${tableName} SET $fieldsString WHERE id = ${obj.id}"

    return execute(sql) > 0
  }

  suspend fun insert(obj: JsonObject): Int {
    val fields = mutableListOf<String>()
    val values = mutableListOf<String>()
    entityClass.members.filter { it is KProperty }.forEach {
      if (it.name != "id") {
        fields.add(it.name.camelToUnderscore())

        val value = obj.getValue(it.name)
        values.add(when (it.returnType) {
          is Number -> value.toString()
          is Enum<*> -> value.toString()
          else -> "'$value'"
        })
      }
    }

    val fieldsString = fields.joinToString(",")
    val valuesString = values.joinToString(",")

    val sql = "INSERT INTO ${tableName}($fieldsString) VALUES($valuesString) RETURNING id"

    return execute(sql)
  }

  fun <X : Domain> Row.mapTo(clazz: KClass<X>) : X {
    val instance = clazz.primaryConstructor?.call() ?: throw Exception("Can not instantiate row object to $clazz")

    clazz.members.forEach {
      if (it is KMutableProperty) {
        val value = getValue(it.name.camelToUnderscore()) ?: return@forEach
        val javaClass = it.getter.returnType.javaType as Class<*>
        if (javaClass.isEnum && value is Number) {
          it.setter.call(instance, javaClass.declaredFields[value.toInt()].get(javaClass))
        } else {
          it.setter.call(instance, value)
        }
      }
    }

    return instance
  }

  suspend fun delete(id: Int): Int {
    val sql = "DELETE FROM ${tableName} WHERE id = $id"
    return execute(sql)
  }
}

private operator fun StringBuffer.plusAssign(s: String) {
  append(s)
}
