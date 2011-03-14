package com.mysema.rdfbean.scala

import annotation.target.field

object Annotations {
  type ClassMapping = com.mysema.rdfbean.annotations.ClassMapping
  type Id = com.mysema.rdfbean.annotations.Id @field
  type Predicate = com.mysema.rdfbean.annotations.Predicate @field
}