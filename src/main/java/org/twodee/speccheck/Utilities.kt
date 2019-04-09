package org.twodee.speccheck

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier

object Utilities {
  val gson: Gson
    get() {
      val builder = GsonBuilder()
      builder.setPrettyPrinting()
      val gson = builder.create()
      return gson
    }

  fun modifierMessage(expected: Int, actual: Int): String {
    var msg = ""

    if (Modifier.isStatic(expected) != Modifier.isStatic(actual)) {
      msg += "It should ${if (Modifier.isStatic(actual)) "not " else ""}be static. "
    }

    if (Modifier.isPublic(expected) != Modifier.isPublic(actual)) {
      msg += "It should ${if (Modifier.isPublic(actual)) "not " else ""}be public. "
    }

    if (Modifier.isProtected(expected) != Modifier.isProtected(actual)) {
      msg += "It should ${if (Modifier.isProtected(actual)) "not " else ""}be protected. "
    }

    if (Modifier.isPrivate(expected) != Modifier.isPrivate(actual)) {
      msg += "It should ${if (Modifier.isPrivate(actual)) "not " else ""}be private. "
    }

    if (Modifier.isFinal(expected) != Modifier.isFinal(actual)) {
      msg += "It should ${if (Modifier.isFinal(actual)) "not " else ""}be final. "
    }

    // For interfaces, we don't care if they are marked abstract.
    if (Modifier.isInterface(expected) != Modifier.isInterface(actual)) {
      msg += "It should ${if (Modifier.isInterface(actual)) "not " else ""}be an interface. "
    } else if (Modifier.isAbstract(expected) != Modifier.isAbstract(actual)) {
      msg += "It should ${if (Modifier.isAbstract(actual)) "not " else ""}be abstract. "
    }

    return msg
  }

  fun stringToClass(name: String) =
    try {
      when (name) {
        "char" -> Char::class.java

        "boolean" -> Boolean::class.java

        "float" -> Float::class.java
        "double" -> Double::class.java

        "byte" -> Byte::class.java
        "short" -> Short::class.java
        "int" -> Int::class.java
        "long" -> Long::class.java

        "void" -> Void.TYPE

        else -> Class.forName(name)
      }
    } catch (e: ClassNotFoundException) {
      throw SpecViolation("I couldn't find a class by the name of \"${name}\". Check CamelCase, spelling, and that you created your class in the right package.")
    }
}

val Class<*>.specifiedFields
  get() = declaredFields.filter { it.isAnnotationPresent(SpecifiedField::class.java) }

val Class<*>.specifiedMethods
  get() = declaredMethods.filter { it.isAnnotationPresent(SpecifiedMethod::class.java) }

val Class<*>.specifiedConstructors
  get() = declaredConstructors.filter { it.isAnnotationPresent(SpecifiedConstructor::class.java) }

val Class<*>.publicFields
  get() = declaredFields.filter { Modifier.isPublic(it.modifiers) }

val Class<*>.publicMethods
  get() = declaredMethods.filter { Modifier.isPublic(it.modifiers) }

val Class<*>.publicConstructors
  get() = declaredConstructors.filter { Modifier.isPublic(it.modifiers) }

val Class<*>.instanceVariableCount
  get() = fields.count { !Modifier.isStatic(it.modifiers) }

val Class<*>.normalizeName
  get() = canonicalName.removePrefix("java.lang.")

val dummy = """
[
  {
    "maxInstanceVariables": 3,
    "modifiers": 1,
    "superclass": "java.util.ArrayList",
    "unitTesters": [
      "org.twodee.speccheck.test.WorksTest"
    ],
    "allowUnspecified": false,
    "allowUnspecifiedDefaultCtor": false,
    "allowUnspecifiedConstants": false,
    "interfaces": [
      "java.awt.event.ActionListener"
    ],
    "constructors": [
      {
        "name": "Works",
        "modifiers": 1,
        "parameters": [
          "java.lang.String",
          "int",
          "double"
        ],
        "mustExceptions": [],
        "mustNotExceptions": []
      }
    ],
    "methods": [
      {
        "returnType": "void",
        "name": "actionPerformed",
        "modifiers": 1,
        "parameters": [
          "java.awt.event.ActionEvent"
        ],
        "mustExceptions": [],
        "mustNotExceptions": []
      },
      {
        "returnType": "int",
        "name": "foo",
        "modifiers": 1,
        "parameters": [
          "int"
        ],
        "mustExceptions": [
          "java.io.IOException"
        ],
        "mustNotExceptions": [
          "java.io.FileNotFoundException"
        ]
      }
    ],
    "fields": [
      {
        "name": "a",
        "type": "int",
        "modifiers": 1
      }
    ],
    "name": "org.twodee.speccheck.test.Works"
  }
]
"""