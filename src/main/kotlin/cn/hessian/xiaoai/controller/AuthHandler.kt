package cn.hessian.xiaoai.controller

import cn.hessian.xiaoai.domain.AuthUser
import cn.hessian.xiaoai.exception.AuthError
import cn.hessian.xiaoai.repository.AuthUserRepository
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.slf4j.LoggerFactory

class AuthHandler (val vertx: Vertx) {
  private val log = LoggerFactory.getLogger(this::class.java)

  suspend fun login(routingContext: RoutingContext) {
    val body = routingContext.bodyAsJson
    val username = body.getString("username")
    val password = body.getString("password")

    val user = AuthUserRepository.getByColumn("username", username) ?: throw AuthError.USER_NOT_FOUND.throwable

    if (user.password != AuthUser.hashPassword(password)) {
      throw AuthError.WRONG_PASSWORD.throwable
    }

    log.info("User logged in. #{} {}", user.id, user.username)

    return json {
      obj {
        "id" to user.id
        "username" to user.username
      }
    }
  }

  suspend fun logout(routingContext: RoutingContext) {

  }

  suspend fun register(routingContext: RoutingContext): AuthUser {
    val newUser = routingContext.bodyAsJson.mapTo(AuthUser::class.java)
    newUser.password = AuthUser.hashPassword(newUser.password!!)
    newUser.status = AuthUser.UserStatus.ENABLED

     AuthUserRepository.insert(newUser)

    newUser.password = ""

    log.info("User registered. #{} {}", newUser.id, newUser.username)

    return newUser
  }

  suspend fun forgetPassword(routingContext: RoutingContext) {

  }
}
