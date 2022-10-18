package org.twodee.speccheck

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier
import javax.swing.*
import java.io.File
import java.io.InputStream
import java.lang.reflect.Method

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

  fun stringToClass(name: String): Class<*> = try {
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
    throw SpecViolation("I couldn't find a class by the name of \"$name\". Check CamelCase, spelling, and that you created your class in the right package.")
  }

  fun wrap(s: String, nChars: Int): String {
    val wrapPattern = "(.{1,$nChars})( +|\n|\\Z)"
    val sWithBreaks = s.replace(wrapPattern.toRegex(), String.format("$1%n"))
    return sWithBreaks.substring(0, sWithBreaks.length - 1)
  }

  fun slurp(path: String): String {
    return File(path).readText()
  }

  fun slurp(stream: InputStream): String {
    return stream.bufferedReader().use { it.readText() }
  }
}

val Class<*>.specifiedFields
  get() = declaredFields.filter {
    it.isAnnotationPresent(SpecifiedField::class.java)
  }

val Class<*>.specifiedMethods
  get() = declaredMethods.filter {
    it.isAnnotationPresent(SpecifiedMethod::class.java)
  }

val Class<*>.specifiedConstructors
  get() = declaredConstructors.filter {
    it.isAnnotationPresent(SpecifiedConstructor::class.java)
  }

val Class<*>.publicFields
  get() = declaredFields.filter {
    Modifier.isPublic(it.modifiers)
  }

val Class<*>.publicMethods
  get() = declaredMethods.filter {
    Modifier.isPublic(it.modifiers)
  }

val Class<*>.publicConstructors
  get() = declaredConstructors.filter {
    Modifier.isPublic(it.modifiers)
  }

val Class<*>.instanceVariableCount
  get() = fields.count {
    !Modifier.isStatic(it.modifiers)
  }

val Class<*>.normalizeName
  get() = canonicalName.removePrefix("java.lang.")

val Method.isMain
  get() = name == "main" && Modifier.isStatic(modifiers) && parameterCount == 1 && parameterTypes[0] == Array<String>::class.java
