package cn.hessian.xiaoai.controller

import cn.hessian.xiaoai.domain.Domain
import cn.hessian.xiaoai.repository.AuthUserRepository
import cn.hessian.xiaoai.repository.Repository
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory

class UserManageHandler (vertx: Vertx) : CurdController(vertx, AuthUserRepository as Repository<Domain>) {
  private val log = LoggerFactory.getLogger(this::class.java)
  override val prefix = "/user-manage"
}
