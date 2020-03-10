package cn.hessian.xiaoai

import java.time.format.DateTimeFormatter

object Config {
  const val APP_KEY_ID = "2k3SNzRMN8KncT+AoDcjrQ=="
  const val APP_SECRET = "6BEXpOOudC9TfSWAb3As+N23iUjfXBGA0MUJc/ANc7M="
  const val HTTP_USER_AGENT = "HSRC/1.0"
  const val PASSWORD_SALT = "DUC}e5IvG}kyGzm=i<Obb+mO#C"
  val DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}
