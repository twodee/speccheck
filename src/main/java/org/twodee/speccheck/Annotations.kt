@file:JvmName("Annotations")

package org.twodee.speccheck

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class SpecifiedClass(
  val allowUnspecified: Boolean = false,
  val allowUnspecifiedDefaultCtor: Boolean = true,
  val allowUnspecifiedConstants: Boolean = false,
  val maxInstanceVariables: Int = -1,
  val isSuperclassChecked: Boolean = false,
  val mustImplement: Array<KClass<*>> = [],
  val unitTesters: Array<KClass<*>> = []
)

@Retention(AnnotationRetention.RUNTIME)
annotation class SpecifiedMethod(
  val mustThrow: Array<KClass<*>> = [],
  val mustNotThrow: Array<KClass<*>> = []
)

@Retention(AnnotationRetention.RUNTIME)
annotation class SpecifiedConstructor(
  val mustThrow: Array<KClass<*>> = [],
  val mustNotThrow: Array<KClass<*>> = []
)

@Retention(AnnotationRetention.RUNTIME)
annotation class SpecifiedField

@Retention(AnnotationRetention.RUNTIME)
annotation class Test(
  val order: Int = 0,
  val points: Int = 0,
  val timeout: Int = 1
)
