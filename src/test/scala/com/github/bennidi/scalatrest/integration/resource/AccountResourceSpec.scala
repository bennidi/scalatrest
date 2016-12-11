package com.github.bennidi.scalatrest.integration.resource

import com.github.bennidi.scalatrest.api.core.RestResourceInfo
import com.github.bennidi.scalatrest.api.resources.AccountResource
import com.github.bennidi.scalatrest.api.model.{Account, AccountRepository}
import com.github.bennidi.scalatrest.base.GenericCRUDSpec
import org.scalatest.DoNotDiscover

@DoNotDiscover
class AccountResourceSpec extends GenericCRUDSpec[Account]{

  lazy val resourceStatusResource = inject[AccountResource]
  lazy val repository = inject[AccountRepository]
  lazy val resourceInfo: RestResourceInfo = inject[RestResourceInfo]("accounts")

  addServlet( resourceStatusResource, endPointBaseUrl + "/*" )

}
