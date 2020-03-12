package cn.hessian.xiaoai.repository

import cn.hessian.xiaoai.domain.Action

object ActionRepository : Repository<Action>() {
  override val tableName = "actions"
  override val entityClass = Action::class

  suspend fun getUserAction(uid: Int, name: String) : Action? {
    return findFirstByWhere("user_id = $uid AND name= '$name'")
  }
}
