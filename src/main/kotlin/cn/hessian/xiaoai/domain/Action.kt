package cn.hessian.xiaoai.domain

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime

data class Action(
  val id: Int,
  val userId: Int,
  val name: String,
  val memo: String?,
  val status: ActionStatus,
  val type: ActionType,
  val command: JsonObject,
  val createdAt: String,
  val modifiedAt: String
) {

  enum class ActionStatus { DRAFT, ENABLED, DISABLED, DELETED }
  enum class ActionType { NONE, SSH, HTTP }
}
