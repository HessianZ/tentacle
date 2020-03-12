package cn.hessian.xiaoai.repository

import cn.hessian.xiaoai.domain.AuthUser

object AuthUserRepository : Repository<AuthUser>() {
  override val tableName = "auth_users"
  override val entityClass = AuthUser::class

  suspend fun getUserByMiToken(token: String) : AuthUser? {
    return getByColumn("mi_token", token)
  }
}
