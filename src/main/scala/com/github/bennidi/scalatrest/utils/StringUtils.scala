package com.github.bennidi.scalatrest.utils


trait StringUtils {

  val ordinary=(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ Seq('-','_')).toSet

  def containsRegexCharacters(s:String) = {
    !s.forall(ordinary.contains(_))
  }
}
