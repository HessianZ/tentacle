package cn.hessian.xiaoai.controller

import cn.hessian.xiaoai.exception.ApiBusinessError

data class ApiResponse(
  val code: Int = 0,
  val message: String? = null,
  val data: Any? = null
) {
  companion object {
    fun success(data: Any?) = ApiResponse(data = data)
    fun fail(message: String?, code: Int = 1) = ApiResponse(message = message, code = code)
    fun fail(error: ApiBusinessError) = ApiResponse(message = error.message, code = error.code)
  }
}
