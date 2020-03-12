package cn.hessian.xiaoai.controller

import cn.hessian.xiaoai.domain.Domain
import cn.hessian.xiaoai.repository.ActionRepository
import cn.hessian.xiaoai.repository.Repository
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory

class ActionHandler (vertx: Vertx) : CurdController(vertx, ActionRepository as Repository<Domain>) {
  private val log = LoggerFactory.getLogger(this::class.java)
  override val prefix = "/action"
}
