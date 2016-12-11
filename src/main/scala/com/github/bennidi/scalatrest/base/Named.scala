package com.github.bennidi.scalatrest.model.base


trait Named {
  val name:String
}

object Named {
  val names : PartialFunction[Named, String] =
  { case named => named.name }

}


