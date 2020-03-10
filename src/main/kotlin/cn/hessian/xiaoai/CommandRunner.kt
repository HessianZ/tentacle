package cn.hessian.xiaoai

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.web.client.sendBufferAwait
import io.vertx.kotlin.ext.web.client.sendFormAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class CommandRunner (val vertx: Vertx)  {

  private val log = LoggerFactory.getLogger(this::class.java)

  private val httpClient = WebClient.create(vertx, WebClientOptions(json {
    obj(
      "userAgent" to Config.HTTP_USER_AGENT,
      "keepAlive" to false
    )
  }))

  suspend fun run(cmd: HttpCommand) : HttpResponse<Buffer> = withContext(Dispatchers.IO) {
    log.debug("Run HttpCommand {}", cmd)

    val request = httpClient.requestAbs(HttpMethod.valueOf(cmd.method), cmd.url);

    cmd.headers.forEach { header ->
      val (key, value) = (header as String).split("=", limit = 2)
      request.putHeader(key, value)
    }

    return@withContext if (cmd.contentType == "x-www-form-urlencoded") {
        request.sendFormAwait(cmd.formParams)
      } else {
        request.sendBufferAwait(Buffer.buffer(cmd.body))
      }
  }


  suspend fun run(cmd: SshCommand) : SshCommand.CmdResult = withContext(Dispatchers.IO) {

    log.debug("Run SshCommand {}", cmd)

    val jsch = JSch()
    var channel: ChannelExec? = null
    var session: Session? = null

    try {
      session = jsch.getSession(cmd.username, cmd.host, cmd.port)
      session.setConfig("StrictHostKeyChecking", "no")
      if (cmd.privateKey != null) {
        jsch.addIdentity("key", cmd.privateKey?.toByteArray(), null,  cmd.passphrase?.toByteArray())
      } else if (cmd.password != null) {
        session.setPassword(cmd.password)
      } else {
        throw Exception("Need password or private key")
      }
      session.connect()
      channel = session.openChannel("exec") as ChannelExec

      val errStream = channel.errStream
      val stdoutStream = channel.inputStream
      val stdoutBuffer = StringBuilder() //执行SSH返回的结果
      val stderrBuffer = StringBuilder() //执行SSH返回的结果
      var exitCode: Int


      channel.setCommand(cmd.command)
      channel.connect()

      val tmp = ByteArray(1024)
      while (true) {
        while (stdoutStream.available() > 0) {
          val i: Int = stdoutStream.read(tmp, 0, 1024)
          if (i < 0) break
          stdoutBuffer.append(String(tmp, 0, i))
        }

        while (errStream.available() > 0) {
          val i: Int = errStream.read(tmp, 0, 1024)
          if (i < 0) break
          stderrBuffer.append(String(tmp, 0, i))
        }

        if (channel.isClosed) {
          exitCode = channel.exitStatus
          break
        }
      }

      errStream.close()
      stdoutStream.close()

      return@withContext SshCommand.CmdResult(exitCode, stdoutBuffer.toString(), stderrBuffer.toString())
    } finally {
      channel?.disconnect()
      session?.disconnect()
    }
  }
}
