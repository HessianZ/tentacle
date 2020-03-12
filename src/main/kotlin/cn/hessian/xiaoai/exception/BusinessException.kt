package cn.hessian.xiaoai.exception

class BusinessException(val code: Int, override val message: String) : Exception(message) {
  constructor(error: ApiBusinessError) : this(error.code, error.message)
}

interface ApiBusinessError {
  val code: Int
  val message: String
  val throwable: Throwable
    get() = BusinessException(code, message)

  fun throwException() {
    throw throwable
  }
}

enum class GenericError(override val code: Int, override val message: String) : ApiBusinessError {
  ERROR(100, "错误"),
  ILLEGAL_ARGUMENT(101, "参数错误"),
  RESOURCE_NOT_FOUND(404, "资源未找到"),
}

enum class AuthError(override val code: Int, override val message: String) : ApiBusinessError {
  USER_NOT_FOUND(1000, "用户不存在"),
  WRONG_PASSWORD(1001, "密码错误");
}
