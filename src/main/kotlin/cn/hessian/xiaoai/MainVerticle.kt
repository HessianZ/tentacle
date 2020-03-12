package cn.hessian.xiaoai

import cn.hessian.xiaoai.controller.*
import cn.hessian.xiaoai.controller.AuthHandler
import cn.hessian.xiaoai.exception.BusinessException
import cn.hessian.xiaoai.exception.GenericError
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.json.Json
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.*
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.*


class MainVerticle : CoroutineVerticle() {

  val log = LoggerFactory.getLogger(this::class.java)

  /**
   * An extension method for simplifying coroutines usage with Vert.x Web routers
   */
  fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
      launch(ctx.vertx().dispatcher()) {
        try {
          fn(ctx)
        } catch (e: Exception) {
          ctx.fail(e)
        }
      }
    }
  }

  fun Route.apiHandler(fn: suspend (RoutingContext) -> Any?) {
    handler { ctx ->
      launch(ctx.vertx().dispatcher()) {
        ctx.response()
          .putHeader("Content-Type", "application/json")
        lateinit var response : ApiResponse
        try {
          response = ApiResponse.success(fn(ctx))
        } catch (e: BusinessException) {
          response = ApiResponse.fail(e.message, e.code)
        } catch (e: Exception) {
          log.error(e.message, e)
          response = ApiResponse.fail(GenericError.ERROR)
        }
        ctx.response().end(Json.encode(response))
      }
    }
  }

  override suspend fun start() {
    Locale.setDefault(Locale.ENGLISH)

    DatabindCodec.mapper()
      .registerModule(ParameterNamesModule())
      .registerModule(Jdk8Module())
      .registerModule(JavaTimeModule())

    val router = Router.router(vertx)
    val skillApiHandler = SkillApiHandler(vertx)
    val oAuthHandler = OAuthHandler(vertx)
    val authHandler = AuthHandler(vertx)

    router.route()
      .handler(ResponseTimeHandler.create())
      .handler(LoggerHandler.create())
      .handler(BodyHandler.create())
      .failureHandler { event: RoutingContext? ->
        if (event != null) {
          log.warn(event.failure().message, event.failure())
          val reasonPhrase = HttpResponseStatus.valueOf(event.statusCode()).reasonPhrase()
          val message = event.failure().message
          event.response().setStatusMessage("$reasonPhrase - $message")
        }
        event?.next()
      }

    router.route().path("/favicon.ico").handler(FaviconHandler.create())

    // 请求授权页面
//    router.get("/account/authorize").handler(StaticHandler.create("/authorize.html"))

    // https://vertx.io/docs/vertx-web/kotlin/#_templates
    // 实际上并没有放context对象进去
    // 参见：io.vertx.ext.web.handler.impl.TemplateHandlerImpl.handle 72行
    router.get("/oauth/authorize").handler {
      it.data()["context"] = it
      it.next()
    }.handler(TemplateHandler.create(ThymeleafTemplateEngine.create(vertx), "templates/authorize.html", TemplateHandler.DEFAULT_CONTENT_TYPE))

    // 提交登录
    router.post("/oauth/authorize").coroutineHandler(oAuthHandler::authorize)
    router.post("/oauth/access-token").coroutineHandler(oAuthHandler::accessToken)
    router.post("/oauth/refresh-token").coroutineHandler(oAuthHandler::refreshToken)

    router.route().path("/skill-api").coroutineHandler(skillApiHandler::handle)

    router.route("/auth/login").apiHandler(authHandler::login)
    router.route("/auth/register").apiHandler(authHandler::register)

    ActionHandler(vertx).bindRouter(router)
    UserManageHandler(vertx).bindRouter(router)

    val port = config.getInteger("http.port", 8890)

    // Start the server
    vertx.createHttpServer()
      .requestHandler(router)
      .listenAwait(config.getInteger("http.port", port))

    log.info("HTTP server started on port $port")
  }
}
