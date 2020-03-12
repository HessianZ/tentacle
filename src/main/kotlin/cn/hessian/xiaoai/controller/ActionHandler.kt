package cn.hessian.xiaoai.controller

import cn.hessian.xiaoai.domain.Action
import cn.hessian.xiaoai.domain.AuthUser
import cn.hessian.xiaoai.domain.Domain
import cn.hessian.xiaoai.repository.ActionRepository
import cn.hessian.xiaoai.repository.Repository
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

class ActionHandler (vertx: Vertx) : CurdController(vertx, ActionRepository as Repository<Domain>) {
  private val log = LoggerFactory.getLogger(this::class.java)
  override val prefix = "/action"

  override suspend fun create(routingContext: RoutingContext) {
    val obj = routingContext.bodyAsJson.mapTo(Action::class.java)

    obj.userId = (routingContext.user() as AuthUser).id

    repository.insert(obj)
  }
}
