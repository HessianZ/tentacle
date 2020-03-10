package cn.hessian.xiaoai.handler

import cn.hessian.xiaoai.*
import cn.hessian.xiaoai.domain.Action
import cn.hessian.xiaoai.repository.ActionRepository
import cn.hessian.xiaoai.repository.AuthUserRepository
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.web.client.sendAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

class SkillApiHandler (vertx: Vertx) {

  private val log = LoggerFactory.getLogger(this::class.java)

  private val httpClient = WebClient.create(vertx, WebClientOptions(json {
    obj(
      "userAgent" to Config.HTTP_USER_AGENT,
      "keepAlive" to false
    )
  }))

  private val commandRunner = CommandRunner(vertx)

  private fun checkSign(context: RoutingContext, secret: String) : Boolean {

    // e.g. MIAI-HmacSHA256-V1 2k3SNzRMN8KncT+AoDcjrQ==::4b9d22c32142d71776103c5e58226ff06e5a592c28b0d5c113409e0eb37c20e4
    val authorization = context.request().getHeader("Authorization")?: ""
    val (signVersion, signs) = authorization.split(" ")
    val (keyId, scope, signature) = signs.split(":")

    val request = context.request()
    val localSigned = SignatureUtil.generateSign(
      request.method().name.toUpperCase(),
//      "/xiao-ai" + request.path(),
      request.path(),
      request.query() ?: "",
      request.getHeader("X-Xiaomi-Date") ?: "",
      request.getHeader("Host") ?: "",
      request.getHeader("Content-Type") ?: "",
      request.getHeader("Content-MD5") ?: "",
      secret
    )

    val res = signature == localSigned

    if (!res) {
      log.warn("Signature not equal {} != {}", signature, localSigned)
    }

    return signature == localSigned
  }

  suspend fun handle(routingContext: RoutingContext) {

    log.info("{} {} : {}", routingContext.request().method(), routingContext.request().uri(), routingContext.bodyAsString)

    val request = routingContext.request()
    val md5 = request.getHeader("Content-MD5") ?: ""

    if (md5.isEmpty() || md5 != Base64.encodeBase64String(DigestUtils.md5(routingContext.bodyAsString))) {
      log.info("MD5 not equal {}", Base64.encodeBase64String(DigestUtils.md5(routingContext.bodyAsString)))
      routingContext.fail(HttpResponseStatus.FORBIDDEN.code(), Exception("MD5 mismatch"))
      return
    }

    routingContext.request().headers().forEach {
      log.debug(" -- {} : {}", it.key, it.value)
    }

    if (!checkSign(routingContext, Config.APP_SECRET)) {
      routingContext.fail(HttpResponseStatus.FORBIDDEN.code(), Exception("Signature mismatch"))
      return
    }

    val requestBody = routingContext.bodyAsJson

    val query = requestBody.getString("query") ?: "unknown"
    val session = requestBody.get<JsonObject>("session")
    var accessToken = session.getJsonObject("user").getString("access_token") ?: ""
    val sessionId = session.getString("session_id")


    try {
      if (accessToken.isEmpty()) {
        accessToken = getAccessTokenBySessionId(sessionId)
      }

      val user = AuthUserRepository.getUserByMiToken(accessToken)
      log.info("user: {} -> {}", accessToken, user)

      user?.let {
        ActionRepository.getUserAction(user.id, query)?.let { action ->

          val start = System.currentTimeMillis()

          when (action.type) {
            Action.ActionType.SSH -> {
              val cmd = SshCommand(action.command)
              log.info("RUN -> {}", cmd)
              val result = commandRunner.run(cmd)
              log.info("RESULT -> {}", result)
            }
            Action.ActionType.HTTP -> {
              val cmd = HttpCommand(action.command)
              log.info("RUN -> {}", cmd)
              val result = commandRunner.run(cmd)
              log.info("RESULT -> {}", result)
            }
            else -> throw Exception("Unknown action type = ${action.type}")
          }

          log.info("Command Time: {}ms", System.currentTimeMillis() - start)
        }
      }

    } catch (e: Exception) {
      log.error(e.message, e)
    }

    routingContext.response().end(response())
  }


  private val RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"

  suspend fun getAccessTokenBySessionId(sid: String) : String = withContext(Dispatchers.IO){

    val url = "https://api-preview.ai.xiaomi.com/aisk/v1/settings/accountlink?sid=$sid"
    val sdf = SimpleDateFormat(RFC1123_PATTERN)
    sdf.timeZone = TimeZone.getTimeZone("GMT")
    val date = sdf.format(Date())

    val scope = ""
    val sign = SignatureUtil.generateSign(
      "GET",
      "/aisk/v1/settings/accountlink",
      "sid=$sid",
      date,
      "api-preview.ai.xiaomi.com",
      "application/json",
      "",
      Config.APP_SECRET
    )

    val response = httpClient!!.requestAbs(HttpMethod.GET, url)
      .putHeader("Content-Type", "application/json")
      .putHeader("Date", date)
      .putHeader("Authorization", "MIAI-HmacSHA256-V1 ${Config.APP_KEY_ID}:$scope:$sign")
      .sendAwait()

    log.info("Response [{}] {} : {}", response.statusCode(), response.statusMessage(), response.bodyAsString())
    response.headers().forEach {
      log.info(" ---> {} : {}", it.key, it.value)
    }
    return@withContext when (response.statusCode()) {
      HttpResponseStatus.OK.code() -> response.bodyAsJsonObject().getString("access_token")
      else -> ""
    }

  }


  fun response(): String {
    val json = """
      {
        "is_session_end": true,
        "version": "1.0",
        "response": {
          "open_mic": false,
          "to_speak": {
            "type": 0,
            "text": "完成"
          },
          "to_display": {
            "text": "完成",
            "type": 0,
            "log_info": {}
          }
        }
      }
""".trimIndent()

    return json
  }
}
