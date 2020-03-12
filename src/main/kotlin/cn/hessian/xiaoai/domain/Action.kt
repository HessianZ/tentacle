package cn.hessian.xiaoai.domain

import cn.hessian.xiaoai.Config
import com.fasterxml.jackson.annotation.JsonFormat
import io.vertx.core.json.JsonObject
import java.time.LocalDateTime

class Action : Domain(), Timestamp {
  override var id: Int? = null
  var userId: Int? = null
  var name: String? = null
  var memo: String? = null
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  var status: ActionStatus? = null
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  var type: ActionType? = null
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  var command: JsonObject? = null
  @JsonFormat(pattern = Config.DEFAULT_DATETIME_PATTERN)
  override var createdAt: LocalDateTime? = null
  @JsonFormat(pattern = Config.DEFAULT_DATETIME_PATTERN)
  override var modifiedAt: LocalDateTime? = null

  enum class ActionStatus { DRAFT, ENABLED, DISABLED, DELETED }
  enum class ActionType { NONE, SSH, HTTP }
}
