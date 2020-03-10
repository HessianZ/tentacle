package cn.hessian.xiaoai.handler

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.slf4j.LoggerFactory

class OAuthHandler (val vertx: Vertx) {

  private val log = LoggerFactory.getLogger(this::class.java)

  suspend fun authorize(routingContext: RoutingContext) {
    val request = routingContext.request()

    val clientId = request.getFormAttribute("client_id")
    val redirectUri = request.getFormAttribute("redirect_uri")
    val state = request.getFormAttribute("state")

    val username = request.getFormAttribute("username")
    val password = request.getFormAttribute("password")

    val response = routingContext.response()

    // 不走库、没算法，直接干
    if (clientId == "xiao-ai" && username == "hessian" && password == "123") {
      response.statusCode = 302
      val code = "123123123"
      val url = "$redirectUri?code=$code&state=$state"
      response.putHeader("Location", url)
      response.end()
    } else {
      log.warn("Login failed ClientId:{} Username:{} Password:{}", clientId, username, password)
      routingContext.fail(HttpResponseStatus.FORBIDDEN.code())
    }
  }

  suspend fun accessToken(routingContext: RoutingContext) {

    val request = routingContext.request()

    val clientId = request.getFormAttribute("client_id")
    val clientSecret = request.getFormAttribute("client_secret")
    val code = request.getFormAttribute("code")

    // TODO: validate

    val res = json {
      obj (
        "access_token" to "123123",
        "refresh_token" to "123123",
        "expires_in" to 7200
//          "open_id" to "1111"
//          "scope" to "123123"
//          "state" to "123123"
      )
    }

    routingContext.response().end(res.toString())
  }

  suspend fun refreshToken(routingContext: RoutingContext) {

    val request = routingContext.request()

    val clientId = request.getFormAttribute("client_id")
    val clientSecret = request.getFormAttribute("client_secret")
    val refreshToken = request.getFormAttribute("refresh_token")

    // TODO: validate

    val res = json {
      obj (
        "access_token" to "123123",
        "refresh_token" to "123123",
        "expires_in" to 7200
//          "open_id" to "1111"
//          "scope" to "123123"
//          "state" to "123123"
      )
    }

    routingContext.response().end(res.toString())
  }
}
