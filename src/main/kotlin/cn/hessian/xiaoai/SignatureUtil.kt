package cn.hessian.xiaoai

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 小米小爱开放平台签名验证机制
 * 算法版本：MIAI-HmacSHA256-V1
 * Dependencies:
 *    implementation 'commons-codec:commons-codec:1.14'
 *
 * @see https://xiaoai.mi.com/documents/Home?type=/api/doc/render_markdown/SkillAccess/SkillDocument/CustomSkills/Signature
 */
object SignatureUtil {
  val log = LoggerFactory.getLogger(MainVerticle::class.java)

  fun generateSign(method: String, urlPath: String, urlParamStr: String, miDate: String, originalHost: String,
                   contentType: String, contentMd5: String, secret: String) : String {

    val sb = StringBuffer()

    sb.append(method).append('\n')
    sb.append(urlPath).append('\n')
    sb.append(urlParamStr).append('\n')
    sb.append(miDate).append('\n')
    sb.append(originalHost).append('\n')
    sb.append(contentType).append('\n')
    sb.append(contentMd5).append('\n')

    log.debug("TextToSign \"{}\"" , sb)

    val signed = encodeByHmacSHA256(sb.toString(), secret)

    log.debug("Signed = {}", signed)

    return signed
  }

  fun encodeByHmacSHA256(textToSign: String, secretKey: String): String {
    val signingKey = SecretKeySpec(Base64.decodeBase64(secretKey), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(signingKey)
    val bytes = mac.doFinal(textToSign.toByteArray(Charsets.UTF_8))
    return Hex.encodeHexString(bytes)
  }

}
