package org.twodee.speccheck

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

class ClassSpecification(val name: String) {
  var maxInstanceVariables: Int = 0
  var modifiers: Int = 0
  var superclass: String? = null
  var unitTesters: List<String> = listOf()

  var allowUnspecified: Boolean = false
  var allowUnspecifiedDefaultCtor: Boolean = false
  var allowUnspecifiedConstants: Boolean = false

  var interfaces: List<String> = listOf()
  var constructors: List<ConstructorSpecification> = listOf()
  var methods: List<MethodSpecification> = listOf()
  var fields: List<FieldSpecification> = listOf()

  fun hasField(field: Field): Boolean = fields.any { field.name == it.name }
  fun hasMethod(method: Method) = methods.any {
    method.name == it.name && method.parameterTypes.map { it.canonicalName } == it.parameters
  }
  fun hasConstructor(ctor: Constructor<*>) = constructors.any {
    ctor.parameterTypes.map { it.canonicalName } == it.parameters
  }
}

class FieldSpecification(val name: String, val type: String, var modifiers: Int)

open class SubroutineSpecification(
  var name: String,
  var modifiers: Int = 0,
  var parameters: List<String> = listOf(),
  var mustExceptions: List<String> = listOf(),
  var mustNotExceptions: List<String> = listOf()
) {
  val signature
    get() = "$name(${parameters.map { Utilities.stringToClass(it).normalizeName }.joinToString(", ")})"
}

class ConstructorSpecification(name: String) : SubroutineSpecification(name)
class MethodSpecification(name: String, var returnType: String) : SubroutineSpecification(name)