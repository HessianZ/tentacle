package cn.hessian.xiaoai.controller

import cn.hessian.xiaoai.domain.Domain
import cn.hessian.xiaoai.exception.BusinessException
import cn.hessian.xiaoai.exception.GenericError
import cn.hessian.xiaoai.repository.Repository
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

abstract class CurdController(val vertx: Vertx, val repository: Repository<Domain>) : CoroutineScope {

  private val log = LoggerFactory.getLogger(this::class.java)

  abstract val prefix : String

  val validPageRange = 0 .. Int.MAX_VALUE
  val validPageSizeRange = 1 .. 1000

  val router = Router.router(vertx)

  override val coroutineContext: CoroutineContext
    get() = vertx.dispatcher()

  init {
    router.apply {
      get().apiHandler(::detail)
      post().apiHandler(::create)
      delete().apiHandler(::delete)
      get("/search").apiHandler(::search)
    }
  }

  private fun Route.apiHandler(fn: suspend (RoutingContext) -> Any?): Route? {
    return handler { ctx ->
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


  fun bindRouter(rootRouter: Router) {
    rootRouter.mountSubRouter(prefix, router)
  }

  suspend fun create(routingContext: RoutingContext) {
    val obj = routingContext.bodyAsJson.mapTo(repository.entityClass.java)
    repository.insert(obj)
  }

  suspend fun update(routingContext: RoutingContext) {
    val obj = routingContext.bodyAsJson.mapTo(repository.entityClass.java)
    repository.update(obj)
  }

  suspend fun detail(routingContext: RoutingContext): Domain {
    return repository.getById(routingContext.request().getParam("id").toInt()) ?: throw GenericError.RESOURCE_NOT_FOUND.throwable
  }

  suspend fun delete(routingContext: RoutingContext): Int {
    return repository.delete(routingContext.request().getParam("id").toInt())
  }

  suspend fun search(routingContext: RoutingContext): List<Domain> {
    val where = routingContext.request().getParam("where")
    val order = routingContext.request().getParam("order")
    val page = routingContext.request().getParam("page")?.toInt() ?: 0
    val pageSize = routingContext.request().getParam("pageSize")?.toInt() ?: 20

    if (!validPageRange.contains(page) || !validPageSizeRange.contains(pageSize)) {
      throw GenericError.ILLEGAL_ARGUMENT.throwable
    }

    val offset = page * pageSize
    return repository.findByWhere(where, order, offset, pageSize)
  }
}
