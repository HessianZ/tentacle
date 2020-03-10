package cn.hessian.xiaoai.repository

import cn.hessian.xiaoai.Config
import cn.hessian.xiaoai.domain.AuthUser
import io.vertx.sqlclient.Row
import org.apache.commons.codec.digest.DigestUtils

object AuthUserRepository : Repository<AuthUser>() {

  override val tableName = "auth_users"

  override fun fromRow(row: Row) : AuthUser {
    return AuthUser(
      id = row.getInteger("id"),
      username = row.getString("username"),
      email = row.getString("email"),
      password = "",
      miToken = row.getString("mi_token"),
      status = AuthUser.UserStatus.values()[row.getInteger("status")],
      createdAt = row.getLocalDateTime("created_at").format(Config.DEFAULT_DATETIME_FORMATTER),
      modifiedAt = row.getLocalDateTime("modified_at").format(Config.DEFAULT_DATETIME_FORMATTER)
    )
  }

  suspend fun getUserByMiToken(token: String) : AuthUser? {
    return getByColumn("mi_token", token)
  }

  suspend fun passwordHash(password: String) : String {
    return DigestUtils.md5Hex("$password-${Config.PASSWORD_SALT}")
  }
}
