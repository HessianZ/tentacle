package cn.hessian.xiaoai.handler

import cn.hessian.xiaoai.Config
import cn.hessian.xiaoai.domain.AuthUser
import cn.hessian.xiaoai.repository.AuthUserRepository
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.core.json.obj
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class AuthHandler (val vertx: Vertx) {
  private val log = LoggerFactory.getLogger(this::class.java)

  suspend fun login(routingContext: RoutingContext) {
    val body = routingContext.bodyAsJson

    val user = AuthUserRepository.getByColumn("username", body.getString("username"))

    lateinit var result: ApiResponse
    try {
      if (user == null) {
        throw Exception("用户不存在")
      }
      if (user.password != body.getString("password")) {
        throw Exception("密码不正确")
      }

      result = ApiResponse.success(json {
        obj {
          "id" to user.id
          "username" to user.username
        }
      })
    } catch (e: Exception) {
      result = ApiResponse.fail(e.message)
    }

    routingContext.response().end(Json.encode(result))
  }

  suspend fun logout(routingContext: RoutingContext) {

  }

  suspend fun register(routingContext: RoutingContext) {

    val json = routingContext.bodyAsJson

    val datetime = LocalDateTime.now().format(Config.DEFAULT_DATETIME_FORMATTER)

    val newUser = AuthUser(
      username = json.getString("username"),
      password = json.getString("password"),
      email = json.getString("username"),
      status = AuthUser.UserStatus.ENABLED,
      miToken = "",
      createdAt = datetime,
      modifiedAt = datetime
    )

    val id = AuthUserRepository.insert(newUser)
    val result = ApiResponse.success(newUser.copy(id = id, password = ""))
    routingContext
      .response()
      .putHeader("Content-Type", "application/json")
      .end(Json.encode(result))
  }

  suspend fun forgetPassword(routingContext: RoutingContext) {

  }
}
