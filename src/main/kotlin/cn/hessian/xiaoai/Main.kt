package cn.hessian.xiaoai

import io.vertx.core.Vertx


fun main(args: Array<String>) {
  System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
  Vertx.vertx().deployVerticle("cn.hessian.xiaoai.MainVerticle")
}
