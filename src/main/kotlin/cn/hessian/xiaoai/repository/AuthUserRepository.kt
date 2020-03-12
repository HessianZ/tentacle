package cn.hessian.xiaoai.repository

import cn.hessian.xiaoai.domain.AuthUser
import cn.hessian.xiaoai.exception.AuthError
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object AuthUserRepository : Repository<AuthUser>(), AuthProvider, CoroutineScope {
  override val tableName = "auth_users"
  override val entityClass = AuthUser::class

  suspend fun getUserByMiToken(token: String) : AuthUser? {
    return getByColumn("mi_token", token)
  }

  override fun authenticate(authInfo: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
    launch {
      val username = authInfo.getString("username")
      val password = authInfo.getString("password")

      val user = AuthUserRepository.getByColumn("username", username)

      if (user == null) {
        resultHandler.handle(Future.failedFuture(AuthError.USER_NOT_FOUND.throwable))
      } else {
        if (user.password == AuthUser.hashPassword(password)) {
          resultHandler.handle(Future.succeededFuture(user))
        } else {
          resultHandler.handle(Future.failedFuture(AuthError.WRONG_PASSWORD.throwable))
        }
      }
    }
  }

  override val coroutineContext = Vertx.vertx().dispatcher()
}
