package cn.hessian.xiaoai

import org.slf4j.LoggerFactory
import java.util.*
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
    val signingKey = SecretKeySpec(Base64.getDecoder().decode(secretKey), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(signingKey)
    val bytes = mac.doFinal(textToSign.toByteArray(Charsets.UTF_8))
    return String(encodeHex(bytes, DIGITS_LOWER))
  }

  /**
   * Used to build output as Hex
   */
  private val DIGITS_LOWER = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
    'e', 'f'
  )

  /**
   * Used to build output as Hex
   */
  private val DIGITS_UPPER = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
    'E', 'F'
  )

  fun encodeHex(data: ByteArray, toDigits: CharArray): CharArray {
    val l = data.size
    val out = CharArray(l shl 1)
    // two characters form the hex value.
    var i = 0
    var j = 0
    while (i < l) {
      out[j++] = toDigits[0xF0 and data[i].toInt() ushr 4]
      out[j++] = toDigits[0x0F and data[i].toInt()]
      i++
    }
    return out
  }
}
