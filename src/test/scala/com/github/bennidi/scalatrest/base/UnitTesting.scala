package com.github.bennidi.scalatrest.base

import com.github.bennidi.scalatrest.api.core.LogSupport
import com.github.bennidi.scalatrest.core.di.{DIModules}
import org.scalatest.{FlatSpecLike, Matchers, FlatSpec}

/**
 * Enable specification style tests with matchers and access to all defined
 * dependency injectors
 *
 */
trait FlatUnitTest extends FlatSpecLike with Matchers with DIModules with LogSupport
trait SpecLikeUnitTest extends FeatureSpec with Matchers with DIModules with LogSupport