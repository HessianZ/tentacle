package cn.hessian.xiaoai


fun String.camelToUnderscore() : String {
  if (isEmpty()) {
    return this
  }

  val stringBuffer = StringBuffer(this)

  var offset = 0

  forEachIndexed { index, c ->
    if (c.isUpperCase()) {
      stringBuffer.setCharAt(index + offset, c.toLowerCase())
      stringBuffer.insert(index + offset, '_')
      offset ++
    }
  }

  return stringBuffer.toString()
}
