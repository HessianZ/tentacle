package cn.hessian.xiaoai

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.pgclient.pgConnectOptionsOf
import io.vertx.kotlin.pgclient.queryAwait
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

object DB {

  private val log = LoggerFactory.getLogger(this::class.java)

  private val pool : PgPool by lazy { connect() }

  private fun connect() : PgPool {
    val config = Vertx.vertx().orCreateContext.config()
    val connectOptions = pgConnectOptionsOf(
      port = config.getInteger("db.port", 5432),
      host = config.getString("db.host", "localhost"),
      database = config.getString("db.database", "xiaoai"),
      user = config.getString("db.user", "postgres"),
      password = config.getString("db.password", "postgres")
    )

    // Pool options
    val poolOptions = PoolOptions(jsonObjectOf(
      "maxSize" to 5
    ))

    log.info("Connect to pgsql database")
    // Create the client pool
    return PgPool.pool(connectOptions, poolOptions)
  }

  suspend fun queryRow(sql: String) : Row? = withContext(Dispatchers.IO){
    log.debug("SQL - {}", sql)
    return@withContext pool.queryAwait(sql).firstOrNull()
  }

  suspend fun queryRows(sql: String) : RowSet<Row> = withContext(Dispatchers.IO){
    log.debug("SQL - {}", sql)
    return@withContext pool.queryAwait(sql)
  }

  suspend fun queryJsonObject(sql: String) : JsonObject? {

    val row = queryRow(sql) ?: return null

    val map = HashMap<String, String>()

    for (i in 0 until row.size()) {
      val name = row.getColumnName(i)
      map[name] = row.getValue(i).toString()
    }

    return JsonObject(map as Map<String, Object>)
  }

}
