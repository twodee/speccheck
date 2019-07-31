package org.twodee.speccheck

import java.io.File
import java.lang.NullPointerException
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Parameter
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashSet

object Verifier {
  @JvmStatic fun main(args: Array<String>) {
    val tag = args[0]
    verify(File(args[1]).readText())
    //    DialogUtilities.isListOkay("FOO", "hi", arrayOf("a", "b", "c", "d"))
  }

  fun verify(file: File) {
    try {
      verify(file.readText())
    } catch (e: SpecViolation) {
      System.err.println(e.message)
    }
  }

  fun verify(json: String) {
    try {
      val project = Utilities.gson.fromJson(json, ProjectSpecification::class.java)
      verifyProject(project)
    } catch (e: SpecViolation) {
      System.err.println(e.message)
    }
  }

  private fun verifyProject(project: ProjectSpecification) {
    verifyVersion(project)
    verifyClasses(project.classes)
    verifyIdentifiers(project.classes.map { "src/${it.name.replace('.', '/')}.java" })
    verifyChecklist(project)
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
      if (!classSpecification.allowUnspecifiedDefaultCtor) {
        clazz.publicConstructors.find { ctor ->
          ctor.parameterCount == 0 && !classSpecification.hasConstructor(ctor)
        }?.let { ctor ->
          throw SpecViolation("I found an unspecified public constructor with signature (${ctor.parameterTypes.map { it.normalizeName }.joinToString(", ")}) in class ${clazz.normalizeName}. Any constructors you add should be private (or protected).")
        }
      }

      // Assert no extraneous public methods.
      clazz.publicMethods.find { method ->
        !classSpecification.hasMethod(method) && !method.isMain
      }?.let { method ->
        throw SpecViolation("I found an unspecified public method ${method.name}(${method.parameterTypes.map { it.normalizeName }.joinToString(", ")}) in class ${clazz.normalizeName}. Any methods you add should be private (or protected).")
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
          try {
            throw e.targetException
          } catch (e: NullPointerException) {
            val stackTrace = e.stackTrace.take(5).joinToString("\n") {
              "  Class ${it.className}, method ${it.methodName} (${it.fileName}:${it.lineNumber})"
            }
            throw SpecViolation("I hit a NullPointerException when trying to test your code. Here is a snapshot of the stack trace showing the lines that led up to the trouble:\n$stackTrace")
          }
        }
      }
    }

    verifyClassSource(classSpecification)
  }

  private fun verifyClassSource(classSpecification: ClassSpecification) {
    val src = Utilities.slurp("src/${classSpecification.name.replace('.', '/')}.java")

    var pattern = Pattern.compile("^\\s*import\\s+(?!hw\\d+.)(?!org\\.twodee\\.)(?!java\\.)(?!javafx\\.)(?!javax\\.)(.*?)\\s*;", Pattern.MULTILINE)
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

    pattern = Pattern.compile("\\\\\\\\")
    matcher = pattern.matcher(src)
    if (matcher.find()) {
      Assert.fail("Class ${classSpecification.name} contains what looks like a Windows-only directory separator (\\, but escaped). Backslash is only valid on Windows and not other operating systems. Please use a cross-platform way of separating directories, such as forward slash (/), the File(parentDirectory, child) constructor, or File.separator.")
    }
  }

  private fun verifyChecklist(project: ProjectSpecification) {
    if (project.hasChecklist) {
      return
    }

    val messages = arrayOf("I have eliminated all compilation errors from my code. In the Package Explorer, there are no red icons on any of my files and no red exclamation point on my project.", "I have committed my work to my local repository. In the Package Explorer, there are no files with greater-than signs (>) preceding their names.", "I have pushed my work to GitLab. In the Package Explorer, there are no up or down arrows followed by numbers after my project name.", "I have verified that my work is in my remote repository at http://gitlab.com.")
    if (!DialogUtilities.isAllChecked("Final Steps", *messages)) {
      Assert.fail("Not all items on your final steps checklist have been completed.")
    }
  }

  private fun verifyIdentifiers(sourcePaths: List<String>) {
    val types = HashSet<String>()
    types.addAll(Arrays.asList("var", "double", "char", "boolean", "float", "short", "long", "int", "byte", "Scanner", "String", "Random", "File", "BufferedImage", "Date", "GregorianCalendar", "ArrayList", "Double", "Character", "Integer", "Boolean", "PrintWriter"))

    // Exclude types associated with files.
    sourcePaths.forEach { sourcePath ->
      val pattern = Pattern.compile("(\\w+)\\.java$")
      val matcher = pattern.matcher(sourcePath)
      if (matcher.find()) {
        types.add(matcher.group(1))
      }
    }

    // TODO
    //    types.addAll(extraTypes)

    val typePattern = "(?:${types.joinToString("|")})"
    val pattern = Pattern.compile("\\b$typePattern(?:\\s*\\[\\s*\\])*\\s+(\\w+)\\s*(?:=(?!=)|,|;|\\))", Pattern.MULTILINE)

    val ids = HashSet<String>()
    sourcePaths.forEach { sourcePath ->
      val source = Utilities.slurp(sourcePath)
      val matcher = pattern.matcher(source)
      while (matcher.find()) {
        ids.add(matcher.group(1))
      }
    }

    if (ids.size > 0 && !DialogUtilities.isListOkay("Identifiers", "Variable names are important. Bad names mislead, confuse, and frustrate. Good names accurately describe the data they hold, are readable and pronounceable, follow camelCaseConventions, and will still make sense in a week's time. Following are some variable names from your code. Are they good names?", ids.toTypedArray())) {
      Assert.fail("Some of your variable names need improvement.")
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

  private fun verifyVersion(project: ProjectSpecification) {
    try {
      val url = URL(String.format("https://twodee.org/teaching/vspec.php?course=%s&semester=%s&homework=%s", project.course, project.semester, project.tag))
      val connection = url.openConnection()
      val inputStream = connection.getInputStream()
      val scanner = Scanner(inputStream)

      var expectedVersion = project.version
      if (scanner.hasNext()) {
        expectedVersion = scanner.nextInt()
      } else {
        System.err.println("Homework was not registered with the server. Unable to validate SpecChecker version.")
      }

      scanner.close()

      if (expectedVersion != project.version) {
        Assert.fail("You are running a SpecChecker that is out of date. Please pull down the latest version from the template remote.")
      }

    } catch (e: UnknownHostException) {
      System.err.println("Host www.twodee.org was inaccessible. Unable to validate SpecChecker version.")
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
