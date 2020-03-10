package cn.hessian.xiaoai

import io.vertx.core.json.JsonObject

class SshCommand(val host: String, val port: Int = 22, val username: String, val password: String? = null,
                 val privateKey: String? = null, val passphrase: String? = null, val command : String
) {

  constructor(json: JsonObject) : this(
    host = json.getString("host", ""),
    port = json.getInteger("port", 22),
    username = json.getString("username", ""),
    password = json.getString("password", ""),
    privateKey = json.getString("privateKey", ""),
    passphrase = json.getString("passphrase", ""),
    command = json.getString("command", "")
  )

  override fun toString(): String {
    return "SshClient(host='$host', port=$port, username='$username', password=$password, privateKey=..., passphrase=$passphrase)"
  }

  data class CmdResult (val exitCode: Int, val out: String, val err: String) {
    override fun toString(): String {
      return "CmdResult(exitCode=$exitCode, err='$err') out='$out'"
    }
  }
}
