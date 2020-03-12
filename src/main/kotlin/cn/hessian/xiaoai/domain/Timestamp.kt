package cn.hessian.xiaoai.domain

import java.time.LocalDateTime

interface Timestamp {
  var createdAt: LocalDateTime?
  var modifiedAt: LocalDateTime?
}
