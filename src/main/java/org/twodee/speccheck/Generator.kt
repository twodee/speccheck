package org.twodee.speccheck

object Generator {
  fun generateJson(vararg clazzes: Class<*>): String {
    val specs = clazzes.map { c -> getSpecification(c) }
    return Utilities.gson.toJson(specs)
  }

  private fun getSpecification(clazz: Class<*>): ClassSpecification {
    val maybeAnnotation: SpecifiedClass? = clazz.getAnnotation(SpecifiedClass::class.java) as SpecifiedClass
    val specification = ClassSpecification(clazz.canonicalName)

    maybeAnnotation?.let { classAnnotation ->
      specification.maxInstanceVariables = classAnnotation.maxInstanceVariables
      specification.modifiers = clazz.modifiers
      specification.interfaces = classAnnotation.mustImplement.map { iface -> iface.qualifiedName!! }
      specification.unitTesters = classAnnotation.unitTesters.map { iface -> iface.qualifiedName!! }

      if (classAnnotation.isSuperclassChecked) {
        specification.superclass = clazz.superclass.canonicalName
      }

      specification.constructors = clazz.specifiedConstructors.map { ctor ->
        val annotation = ctor.getAnnotation(SpecifiedConstructor::class.java)
        // Constructor name is fully qualified for some reason. We strip path.
        ConstructorSpecification(ctor.name.replace("^.*\\.".toRegex(), "")).apply {
          modifiers = ctor.modifiers
          parameters = ctor.parameters.map { it.type.canonicalName }
          mustExceptions = annotation.mustThrow.map { it.qualifiedName!! }
          mustNotExceptions = annotation.mustNotThrow.map { it.qualifiedName!! }
        }
      }

      specification.methods = clazz.specifiedMethods.map { method ->
        val annotation = method.getAnnotation(SpecifiedMethod::class.java)
        MethodSpecification(method.name, method.returnType.canonicalName).apply {
          modifiers = method.modifiers
          parameters = method.parameters.map { it.type.canonicalName }
          mustExceptions = annotation.mustThrow.map { it.qualifiedName!! }
          mustNotExceptions = annotation.mustNotThrow.map { it.qualifiedName!! }
        }
      }

      specification.fields = clazz.specifiedFields.map { field ->
        FieldSpecification(field.name, field.type.canonicalName, field.modifiers)
      }
    }

    return specification
  }
}
