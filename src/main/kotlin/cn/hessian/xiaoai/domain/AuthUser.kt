package cn.hessian.xiaoai.domain

import cn.hessian.xiaoai.Config
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.apache.commons.codec.digest.DigestUtils
import java.time.LocalDateTime

class AuthUser : Domain(), Timestamp, User {
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

  override fun clearCache(): User {
    TODO("not implemented")
  }

  override fun setAuthProvider(authProvider: AuthProvider?) {
    TODO("not implemented")
  }

  override fun isAuthorized(authority: String?, resultHandler: Handler<AsyncResult<Boolean>>?): User {
    TODO("not implemented")
  }

  override fun principal(): JsonObject {
    return json {
      obj {
        "username" to username
      }
    }
  }
}
