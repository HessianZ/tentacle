package cn.hessian.xiaoai.repository

import cn.hessian.xiaoai.Config
import cn.hessian.xiaoai.DB
import cn.hessian.xiaoai.camelToUnderscore
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class Repository<T : Any> {

  private val log = LoggerFactory.getLogger(this::class.java)

  abstract val tableName: String

  abstract fun fromRow(row: Row) : T?

  suspend fun getById(id : Int) : T? {
    return getByColumn("id", id.toString())
  }

  suspend fun getByColumn(column: String, value: String) : T? {
    return findFirstByWhere("\"$column\" = '$value'")
  }

  suspend fun findByWhere(where: String, order: String? = null) : List<T> {
    var sql = "SELECT * FROM \"${tableName}\" WHERE $where"

    if (order != null) {
      sql += " ORDER BY $order"
    }

    val result = ArrayList<T>()

    DB.queryRows(sql).forEach {
      result.add(fromRow(it)!!)
    }

    return result
  }

  suspend fun findByColumn(column: String, value: String, order: String? = null) : List<T> {
    return findByWhere("\"$column\" = '$value'", order)
  }

  suspend fun findFirstByWhere(where: String, order: String? = null) : T? {
    return findByWhere(where, order).firstOrNull()
  }

  suspend fun countByWhere(where: String) : Int {
    val sql = "SELECT COUNT(*) count FROM \"${tableName}\" WHERE $where"
    return DB.queryRow(sql)?.getInteger(0) ?: 0
  }

  suspend fun execute(sql: String) : Int {
    return DB.queryRow(sql)?.getInteger(0) ?: 0
  }

  suspend fun insert(obj: T): Int {

    val fields = mutableListOf<String>()
    val values = mutableListOf<String>()
    obj::class.members.filter { it is KProperty }.forEach {
      if (it.name != "id") {
        fields.add(it.name.camelToUnderscore())

        val value = it.call(obj)
        values.add(when (value) {
          is Number -> value.toString()
          is Enum<*> -> value.ordinal.toString()
          else -> "'$value'"
        })
      }
    }

    val fieldsString = fields.joinToString(",")
    val valuesString = values.joinToString(",")

    val sql = "INSERT INTO ${tableName}($fieldsString) VALUES($valuesString) RETURNING id"

    return execute(sql)
  }
}
