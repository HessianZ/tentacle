package cn.hessian.xiaoai.repository

import cn.hessian.xiaoai.Config
import cn.hessian.xiaoai.domain.Action
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row

object ActionRepository : Repository<Action>() {
  override val tableName = "actions"

  override fun fromRow(row: Row) : Action {
    return Action(
      id = row.getInteger("id"),
      userId = row.getInteger("user_id"),
      name = row.getString("name"),
      memo = row.getString("memo"),
      status = Action.ActionStatus.values()[row.getInteger("status")],
      type = Action.ActionType.values()[row.getInteger("type")],
      command = row.get(JsonObject::class.java, row.getColumnIndex("command")),
      createdAt = row.getLocalDateTime("created_at").format(Config.DEFAULT_DATETIME_FORMATTER),
      modifiedAt = row.getLocalDateTime("modified_at").format(Config.DEFAULT_DATETIME_FORMATTER)
    )
  }

  suspend fun getUserAction(uid: Int, name: String) : Action? {
    return findFirstByWhere("user_id = $uid AND name= '$name'")
  }
}
