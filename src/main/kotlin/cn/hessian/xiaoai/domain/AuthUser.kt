package cn.hessian.xiaoai.domain

import cn.hessian.xiaoai.Config
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.commons.codec.digest.DigestUtils
import java.time.LocalDateTime

class AuthUser : Domain(), Timestamp {
  override var id: Int? = null
  var username: String? = null
  var email: String? = null
  @JsonIgnore
  var password: String? = null
  var miToken: String? = null
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  var status: UserStatus? = null
  @JsonFormat(pattern = Config.DEFAULT_DATETIME_PATTERN)
  override var createdAt: LocalDateTime? = null
  @JsonFormat(pattern = Config.DEFAULT_DATETIME_PATTERN)
  override var modifiedAt: LocalDateTime? = null

  enum class UserStatus { DRAFT, ENABLED, DISABLED, DELETED }

  companion object {
    suspend fun hashPassword(password: String) : String {
      return DigestUtils.md5Hex("$password-${Config.PASSWORD_SALT}")
    }
  }
}
