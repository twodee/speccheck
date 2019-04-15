package org.twodee.speccheck

object Generator {
  @JvmStatic
  fun main(args: Array<String>) {
    println(generate(args[0], args[1], args[2], Integer.parseInt(args[3]), *args.drop(4).map { Class.forName(it) }.toTypedArray()))
  }

  fun generate(tag: String, semester: String, course: String, version: Int, vararg clazzes: Class<*>): String {
    return Utilities.gson.toJson(ProjectSpecification(tag, semester, course, version).apply {
      classes = clazzes.map { c -> getSpecification(c) }
    })
  }

//  fun generateJson(vararg clazzes: Class<*>): String {
//    val specs =
//    return Utilities.gson.toJson(specs)
//  }

  private fun getSpecification(clazz: Class<*>): ClassSpecification {
    val maybeAnnotation: SpecifiedClass? = clazz.getAnnotation(SpecifiedClass::class.java) as SpecifiedClass
    val specification = ClassSpecification(clazz.name)

    maybeAnnotation?.let { classAnnotation ->
      specification.maxInstanceVariables = classAnnotation.maxInstanceVariables
      specification.modifiers = clazz.modifiers
      specification.interfaces = classAnnotation.mustImplement.map { iface -> iface.qualifiedName!! }
      specification.unitTesters = classAnnotation.unitTesters.map { iface -> iface.qualifiedName!! }
      specification.allowUnspecifiedDefaultCtor = classAnnotation.allowUnspecifiedDefaultCtor
      specification.allowUnspecifiedConstants = classAnnotation.allowUnspecifiedConstants
      specification.allowUnspecified = classAnnotation.allowUnspecified

      if (classAnnotation.isSuperclassChecked) {
        specification.superclass = clazz.superclass.name
      }

      specification.constructors = clazz.specifiedConstructors.map { ctor ->
        val annotation = ctor.getAnnotation(SpecifiedConstructor::class.java)
        // Constructor name is fully qualified for some reason. We strip path.
        ConstructorSpecification(ctor.name.replace("^.*\\.".toRegex(), "")).apply {
          modifiers = ctor.modifiers
          parameters = ctor.parameters.map { it.type.name }
          mustExceptions = annotation.mustThrow.map { it.qualifiedName!! }
          mustNotExceptions = annotation.mustNotThrow.map { it.qualifiedName!! }
        }
      }

      specification.methods = clazz.specifiedMethods.map { method ->
        val annotation = method.getAnnotation(SpecifiedMethod::class.java)
        MethodSpecification(method.name, method.returnType.name).apply {
          modifiers = method.modifiers
          parameters = method.parameters.map { it.type.name }
          mustExceptions = annotation.mustThrow.map { it.qualifiedName!! }
          mustNotExceptions = annotation.mustNotThrow.map { it.qualifiedName!! }
        }
      }

      specification.fields = clazz.specifiedFields.map { field ->
        FieldSpecification(field.name, field.type.name, field.modifiers)
      }
    }

    return specification
  }
}
