package cn.hessian.xiaoai

import io.netty.handler.codec.http.QueryStringDecoder
import io.vertx.core.MultiMap
import io.vertx.core.http.impl.headers.VertxHttpHeaders
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.net.URI

class HttpCommand (val url: String,
                        val method: String,
                        val headers: JsonArray,
                        val contentType: String, // default for post is "application/x-www-form-urlencoded"
                        val body: String) {

  constructor(json: JsonObject) : this(
    url = json.getString("url"),
    method = json.getString("method"),
    headers = json.getJsonArray("headers"),
    contentType = json.getString("contentType"),
    body = json.getString("body")
  )


  val formParams: MultiMap
    get() {
      val result = VertxHttpHeaders()
      if (contentType == "application/x-www-form-urlencoded") {
        val queryStringDecoder = QueryStringDecoder(URI("?$body"))
        queryStringDecoder.parameters().forEach { key, value ->
          result.add(key, value)
        }
      }
      return result
    }

}
