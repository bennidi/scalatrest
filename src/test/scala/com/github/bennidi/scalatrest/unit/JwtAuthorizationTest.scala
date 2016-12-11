package com.github.bennidi.scalatrest.unit

import com.github.bennidi.scalatrest.api.core.auth.JwtManager
import com.github.bennidi.scalatrest.base.FlatUnitTest
import com.github.bennidi.scalatrest.core.json.JsonUtils

class JwtAuthorizationTest extends FlatUnitTest{

  val jwtManager = inject[JwtManager]

  "JWT Manager" should "provide a way to encode and decode tokens" in {
    val expiryDate = System.currentTimeMillis() + 9999
    val claims = List(
      s"""{"sub":"bd113898-cfb5-430a-910f-7c527776dcd8","roles":"ROLE_USER","iat":1448658116,"exp":${expiryDate}}""",
      s"""{"sub":"bd113898-cfb5-430a-910f-7c527776dcd8","iat":1448658116,"exp":${expiryDate}}""",
      """{"roles":"ROLE_USER","sub":"bd113898-cfb5-430a-910f-7c527776dcd8"}""",
      """{}"""
    )
    claims.foreach { claim =>
      val token = jwtManager.encode(claim)
      val decoded = jwtManager.decode(token)
      decoded should equal(JsonUtils.parseJson(claim))
    }
  }

}


