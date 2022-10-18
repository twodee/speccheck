package org.twodee.speccheck

import java.io.File
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.regex.Pattern
import kotlin.system.exitProcess
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.PrintStream
import java.io.PrintWriter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object GradescopeVerifier {
  fun verify(inFile: File, outFile: File) {
    verify(inFile.readText(), outFile)
  }

  fun verify(json: String, outFile: File) {
    val report = GradescopeReport()
    try {
      val project = Utilities.gson.fromJson(json, ProjectSpecification::class.java)
      verifyProject(report, project)
    } catch (e: SpecViolation) {
      val testResult = TestResult("Interface Tests")
      testResult.score = 0
      testResult.output = e.message ?: "exception had no message"
      report.tests.add(testResult)
    }
    val gsonner = GsonBuilder().setPrettyPrinting().create()
    val sum = report.tests.sumOf { it.score }
    System.err.printf("TOTAL: %d%n", sum)
    val reportJson = gsonner.toJson(report)

    val out = PrintWriter(outFile)
    out.println(reportJson)
    out.close()
  }

  private fun verifyProject(report: GradescopeReport, project: ProjectSpecification) {
    verifyClasses(report, project.tag, project.classes)

    if (project.isStyleChecked) {
      verifyStyle(report, project.classes)
    }
  }

  private fun verifyStyle(report: GradescopeReport, clazzes: List<ClassSpecification>) {
    clazzes.forEach { classSpecification ->
      val sourcePath = "src/${classSpecification.name.replace('.', '/')}.java"
      val builder = ProcessBuilder().command("java", "-jar", "source/checkstyle.jar", "-c", "source/checkstyle.xml", sourcePath)
      val process = builder.start()
      val exitCode = process.waitFor()
      val output = String(process.inputStream.readAllBytes())
      if (exitCode != 0) {
        val testResult = TestResult("CheckStyle")
        testResult.score = -5
        testResult.status = "failed"
        testResult.output = String.format("I tried running CheckStyle on the source for ${classSpecification.name}, but it failed with this output:%n%n$output")
        report.tests.add(testResult)
        return
      }
    }
  }

  private fun verifyClasses(report: GradescopeReport, tag: String, clazzes: List<ClassSpecification>) {
    clazzes.forEach { c -> verifyClass(report, tag, c) }
  }

  private fun verifyClass(report: GradescopeReport, tag: String, classSpecification: ClassSpecification) {
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
      if (!classSpecification.allowUnspecifiedDefaultCtor) {
        clazz.publicConstructors.find { ctor ->
          ctor.parameterCount == 0 && !classSpecification.hasConstructor(ctor)
        }?.let { ctor ->
          throw SpecViolation("I found an unspecified public constructor with signature (${ctor.parameterTypes.joinToString(", ") { it.normalizeName }}) in class ${clazz.normalizeName}. Any constructors you add should be private (or protected).")
        }
      }

      // Assert no extraneous public methods.
      clazz.publicMethods.find { method ->
        !classSpecification.hasMethod(method) && !method.isMain
      }?.let { method ->
        throw SpecViolation("I found an unspecified public method ${method.name}(${method.parameterTypes.joinToString(", ") { it.normalizeName }}) in class ${clazz.normalizeName}. Any methods you add should be private (or protected).")
      }
    }

    // Assert unit tests.
    classSpecification.unitTesters.forEach { testerName ->
      val testerClazz = Utilities.stringToClass(testerName)
      val instance = testerClazz.getConstructor().newInstance()
      val methods = testerClazz.methods.filter { it.isAnnotationPresent(Test::class.java) }.sortedBy { it.getAnnotation(Test::class.java).order }
      methods.forEach { method ->
        val testResult = TestResult("${classSpecification.name}: ${method.name}")
        try {
          method.invoke(instance)
          testResult.score = method.getAnnotation(Test::class.java).points
          testResult.status = "passed"
        } catch (e: InvocationTargetException) {
          testResult.score = 0
          if (e.targetException is SpecViolation) {
            testResult.output = e.targetException.message!!
          } else {
            testResult.output = "I hit an exception of type ${e.targetException.javaClass.simpleName} while running your code."
            if (e.targetException.message != null) {
              testResult.output += " This was it's message: \"${e.targetException.message}\"."
            }
            val stackTrace = e.targetException.stackTrace.joinToString("\n") {
              "  Class ${it.className}, method ${it.methodName} (${it.fileName}:${it.lineNumber})"
            }
            testResult.output += " And this is it's stack trace:${System.lineSeparator()}${System.lineSeparator()}${stackTrace}"
          }
        } catch (e: NullPointerException) {
          val stackTrace = e.stackTrace.joinToString("\n") {
            "  Class ${it.className}, method ${it.methodName} (${it.fileName}:${it.lineNumber})"
          }
          testResult.output = "I hit a NullPointerException when trying to test your code. Here is the stack trace showing the lines that led up to the trouble:\n$stackTrace"
        }
        report.tests.add(testResult)
      }
    }

    try {
      verifyClassSource(tag, classSpecification)
    } catch (e: SpecViolation) {
      val testResult = TestResult("${classSpecification.name} -> Source Checks")
      testResult.score = -2
      testResult.output = e.message ?: "uh oh"
      report.tests.add(testResult)
    }
  }

  private fun verifyClassSource(tag: String, classSpecification: ClassSpecification) {
    val src = Utilities.slurp("src/${classSpecification.name.replace('.', '/')}.java")

    var pattern = Pattern.compile("^\\s*import\\s+(?>(?:static\\s+)?)(?!$tag\\.)(?!org\\.twodee\\.)(?!org\\.junit\\.)(?!java\\.)(?!javafx\\.)(?!javax\\.)(.*?)\\s*;", Pattern.MULTILINE)
    var matcher = pattern.matcher(src)
    if (matcher.find()) {
      Assert.fail("Class ${classSpecification.name} imports ${matcher.group(1)}. You may only import classes from standard packages (those whose fully-qualified names match \"java.*\"). Not every machine supports the non-standard packages.")
    }

    pattern = Pattern.compile("(false|true)\\s*(==|!=)|(==|!=)\\s*(false|true)", Pattern.MULTILINE)
    matcher = pattern.matcher(src)
    if (matcher.find()) {
      Assert.fail(String.format("Class ${classSpecification.name} contains the comparison \"${matcher.group()}\". Simplify your code; you never need compare to a boolean literal. Eliminate \"== true\" and \"!= false\" altogether. Rewrite \"== false\" and \"!= true\" to use the ! operator. With meaningful variable names, your code will be much more readable without these comparisons to boolean literals.", matcher.group()))
    }

    pattern = Pattern.compile("\\\\r")
    matcher = pattern.matcher(src)
    if (matcher.find()) {
      Assert.fail("Class ${classSpecification.name} contains a carriage return character (\\r). Carriage returns are only valid on the Windows operating system. Please use a cross-platform way of generating linebreaks, such as println, %n in format strings, or System.lineSeparator().")
    }

    pattern = Pattern.compile("\\\\n")
    matcher = pattern.matcher(src)
    if (matcher.find()) {
      Assert.fail("Class ${classSpecification.name} contains a linefeed character (\\n). Linefeeds are not valid on all operating systems. Please use a cross-platform way of generating linebreaks, such as println, %n in format strings, or System.lineSeparator().")
    }

//    pattern = Pattern.compile("\\\\\\\\")
//    matcher = pattern.matcher(src)
//    if (matcher.find()) {
//      Assert.fail("Class ${classSpecification.name} contains what looks like a Windows-only directory separator (\\, but escaped). Backslash is only valid on Windows and not other operating systems. Please use a cross-platform way of separating directories, such as forward slash (/), the File(parentDirectory, child) constructor, or File.separator.")
//    }
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

class TestResult(val name: String) {
  var score: Int = 0
  var status: String = "failed"
  var output: String = ""
}

class GradescopeReport {
  var output: String = ""
  var tests = mutableListOf<TestResult>()
}
