package com.github.bennidi.scalatrest.integration

import com.github.bennidi.scalatrest.base.IntegrationTests
import com.github.bennidi.scalatrest.integration.base.{InterServiceCallTest, JwtAuthorizationTest, DependencyInjectionTest}
import com.github.bennidi.scalatrest.integration.persistence.AccountRepositoryTest
import com.github.bennidi.scalatrest.integration.resource.AccountResourceSpec
import org.scalatest.Suite

object AllSuites{
  val Template:Seq[Suite] = List(new DependencyInjectionTest,new JwtAuthorizationTest, new InterServiceCallTest,
    new AccountRepositoryTest, new AccountResourceSpec)
  val Project:Seq[Suite] = ProjectIntegrationTests.integrationTests
}
class AllIntegrationTests extends IntegrationTests(AllSuites.Template ++ AllSuites.Project )
