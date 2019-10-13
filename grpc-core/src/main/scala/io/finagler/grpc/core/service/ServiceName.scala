package io.finagler.grpc.core.service

import com.twitter.app.GlobalFlag

object ServiceName
  extends GlobalFlag[String]("unknown-service,unknown", "Application full name and short name") {

  private var _fullName: String = "unknown-service"

  private var _shortName: String = "unknown"

  override def apply(): String = {
    val result = super.apply()
    parseNames(result)
  }

  override def parse(raw: String): Unit = {
    super.parse(raw)
    parseNames(raw)
  }

  private def parseNames(nameString: String): String = {
    val names = nameString.split(",")
    if (names.size > 1) {
      _fullName = names(0)
      _shortName = names(1)
    }
    nameString
  }

  def fullName: String = _fullName

  def shortName: String = _shortName

}
