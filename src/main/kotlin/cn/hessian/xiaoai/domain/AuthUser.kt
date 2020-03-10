package cn.hessian.xiaoai.domain

import cn.hessian.xiaoai.Config
import java.time.LocalDateTime

data class AuthUser(
  var id: Int = 0,
  val username: String,
  val email: String,
  val password: String,
  val miToken: String,
  val status: UserStatus,
  val createdAt: String,
  val modifiedAt: String
) {
  enum class UserStatus { DRAFT, ENABLED, DISABLED, DELETED }
}
