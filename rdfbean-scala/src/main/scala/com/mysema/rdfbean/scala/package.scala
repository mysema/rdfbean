package com.mysema.rdfbean

package object scala {

  import annotation.target.field

  type ClassMapping = com.mysema.rdfbean.annotations.ClassMapping

  type ComponentType = com.mysema.rdfbean.annotations.ComponentType @field

  type Container = com.mysema.rdfbean.annotations.Container @field

  type ContainerType = com.mysema.rdfbean.annotations.ContainerType

  type Context = com.mysema.rdfbean.annotations.Context

  type Default = com.mysema.rdfbean.annotations.Default @field

  type Defaults = com.mysema.rdfbean.annotations.Defaults @field

  type Id = com.mysema.rdfbean.annotations.Id @field

  type InjectService = com.mysema.rdfbean.annotations.InjectService @field

  type Localized = com.mysema.rdfbean.annotations.Localized @field

  type MapElements = com.mysema.rdfbean.annotations.MapElements @field

  type Mixin = com.mysema.rdfbean.annotations.Mixin @field

  type Path = com.mysema.rdfbean.annotations.Path @field

  type Predicate = com.mysema.rdfbean.annotations.Predicate @field

  type Properties = com.mysema.rdfbean.annotations.Properties @field

  type Required = com.mysema.rdfbean.annotations.Required @field

  type Unique = com.mysema.rdfbean.annotations.Unique @field

}