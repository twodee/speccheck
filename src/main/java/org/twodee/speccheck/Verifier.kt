package org.twodee.speccheck

import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException

object Verifier {
  @JvmStatic
  fun main(args: Array<String>) {
    val tag = args[0]
    verify(File(args[1]).readText())
  }

  fun verify(file: File) {
    try {
      verify(file.readText())
    } catch (e: SpecViolation) {
      System.err.println(e.message)
    }
  }

  fun verify(json: String) {
    val project = Utilities.gson.fromJson(json, ProjectSpecification::class.java)
    verifyProject(project)
  }

  private fun verifyProject(project: ProjectSpecification) {
    verifyClasses(project.classes)
  }

  private fun verifyClasses(clazzes: List<ClassSpecification>) {
    clazzes.forEach { c -> verifyClass(c) }
  }

  private fun verifyClass(classSpecification: ClassSpecification) {
    // Assert existence.
    val clazz = Utilities.stringToClass(classSpecification.name)

    // Assert modifiers.
    if (clazz.modifiers != classSpecification.modifiers) {
      throw SpecViolation("I didn't find the right modifiers for class ${classSpecification.name}. ${Utilities.modifierMessage(classSpecification.modifiers, clazz.modifiers)}")
    }

    // Assert number of instance variables.
    if (classSpecification.maxInstanceVariables >= 0 && clazz.instanceVariableCount > classSpecification.maxInstanceVariables) {
      throw SpecViolation("I found a lot of instance variables in class ${classSpecification.name}. Too many. Perhaps some of them should be local variables?")
    }

    // Assert superclass.
    classSpecification.superclass?.let {
      val superclazz = Utilities.stringToClass(it)
      if (superclazz != clazz.superclass) {
        throw SpecViolation("I didn't find the correct superclass for ${classSpecification.name}.\n  Expected: ${superclazz.normalizeName}\n    Actual: ${clazz.superclass.normalizeName}")
      }
    }

    // Assert interfaces.
    classSpecification.interfaces.forEach { interfaceName ->
      val iface = Utilities.stringToClass(interfaceName)
      if (!clazz.interfaces.contains(iface)) {
        throw SpecViolation("I expected class ${clazz.normalizeName} to implement interface ${iface.normalizeName}, but it doesn't.")
      }
    }

    // Assert methods.
    classSpecification.constructors.forEach { verifyConstructor(clazz, it) }
    classSpecification.methods.forEach { verifyMethod(clazz, it) }
    classSpecification.fields.forEach { verifyField(clazz, it) }

    // Assert no extraneous public things in the user's code.
    if (!classSpecification.allowUnspecified) {
      if (!classSpecification.allowUnspecifiedConstants) {
        clazz.publicFields.forEach { field ->
          if (!classSpecification.hasField(field)) {
            throw SpecViolation("I found an unspecified public field \"${field.name}\" in class ${clazz.normalizeName}. Any fields you add should be private (or protected).")
          }
        }
      }

      // Assert no extraneous public constructors.
      clazz.publicConstructors.forEach { ctor ->
        if (!((ctor.parameterCount == 0 && classSpecification.allowUnspecifiedDefaultCtor) || classSpecification.hasConstructor(ctor))) {
          throw SpecViolation("I found an unspecified public constructor with signature (${ctor.parameterTypes.map { it.normalizeName }.joinToString(", ")}) in class ${clazz.normalizeName}. Any constructors you add should be private (or protected).")
        }
      }

      // Assert no extraneous public methods.
      clazz.publicMethods.forEach { method ->
        if (!classSpecification.hasMethod(method)) {
          throw SpecViolation("I found an unspecified public method ${method.name}(${method.parameterTypes.map { it.normalizeName }.joinToString(", ")}) in class ${clazz.normalizeName}. Any methods you add should be private (or protected).")
        }
      }
    }

    // Assert unit tests.
    classSpecification.unitTesters.forEach { testerName ->
      val testerClazz = Utilities.stringToClass(testerName)
      val instance = testerClazz.getConstructor().newInstance()
      val methods = testerClazz.declaredMethods.filter { it.isAnnotationPresent(Test::class.java) }
      methods.forEach { method ->
        try {
          method.invoke(instance)
        } catch (e: InvocationTargetException) {
          throw e.targetException
        }
      }
    }
  }

  private fun verifyField(clazz: Class<*>, specification: FieldSpecification) {
    val field = try {
      clazz.getField(specification.name)
    } catch (e: NoSuchFieldException) {
      throw SpecViolation("I couldn't find field ${specification.name} in class ${clazz.normalizeName}. Have you added it? Does it have the right name?")
    }

    // Assert modifiers.
    if (field.modifiers != specification.modifiers) {
      throw SpecViolation("I didn't find the right modifiers for field ${specification.name} in ${clazz.normalizeName}. ${Utilities.modifierMessage(specification.modifiers, field.modifiers)}")
    }

    val type = Utilities.stringToClass(specification.type)
    if (type != field.type) {
      throw SpecViolation("I found the wrong type for field ${specification.name} in class ${clazz.normalizeName}.\n  Expected: ${type.normalizeName}\n    Actual: ${field.type.normalizeName}")
    }
  }

  private fun verifyConstructor(clazz: Class<*>, specification: ConstructorSpecification) {
    // Assert name.
    val parameters = specification.parameters.map { typeName -> Utilities.stringToClass(typeName) }.toTypedArray()
    val ctor = try {
      clazz.getConstructor(*parameters)
    } catch (e: NoSuchMethodException) {
      throw SpecViolation("I couldn't find constructor ${specification.signature} in class ${clazz.normalizeName}. Have you written it? Does it have the right parameters?")
    }

    verifySubroutine(clazz, ctor, specification)
  }

  private fun verifyMethod(clazz: Class<*>, specification: MethodSpecification) {
    // Assert name.
    val parameters = specification.parameters.map { typeName -> Utilities.stringToClass(typeName) }.toTypedArray()
    val method = try {
      clazz.getMethod(specification.name, *parameters)
    } catch (e: NoSuchMethodException) {
      throw SpecViolation("I couldn't find method ${specification.signature} in class ${clazz.normalizeName}. Have you written it? Does it have the right name? The right parameters?")
    }

    // Assert return type.
    val returnType = Utilities.stringToClass(specification.returnType)
    if (returnType != method.returnType) {
      throw SpecViolation("I found the wrong return type for ${specification.signature} in class ${clazz.normalizeName}.\n  Expected: ${returnType.normalizeName}\n    Actual: ${method.returnType.normalizeName}")
    }

    verifySubroutine(clazz, method, specification)
  }

  private fun verifySubroutine(clazz: Class<*>, subroutine: Executable, specification: SubroutineSpecification) {
    // Assert modifiers.
    if (subroutine.modifiers != specification.modifiers) {
      throw SpecViolation("I didn't find the right modifiers for ${specification.signature} in ${clazz.normalizeName}. ${Utilities.modifierMessage(specification.modifiers, subroutine.modifiers)}")
    }

    // Assert throws.
    specification.mustExceptions.map { typeName ->
      Utilities.stringToClass(typeName)
    }.forEach { exceptionType ->
      if (!subroutine.exceptionTypes.contains(exceptionType)) {
        throw SpecViolation("I expected ${specification.signature} in ${clazz.normalizeName} to throw ${exceptionType.normalizeName} but it doesn't.")
      }
    }

    // Assert not throws.
    specification.mustNotExceptions.map { typeName ->
      Utilities.stringToClass(typeName)
    }.forEach { exceptionType ->
      if (subroutine.exceptionTypes.contains(exceptionType)) {
        throw SpecViolation("I expected ${specification.signature} in ${clazz.normalizeName} to not throw ${exceptionType.normalizeName} but it does.")
      }
    }
  }
}
