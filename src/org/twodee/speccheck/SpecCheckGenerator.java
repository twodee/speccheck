package org.twodee.speccheck;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;

/**
 * A generator which produces JUnit tests that ensure classes meet certain
 * specifications. The tests are produced from an existing implementation which
 * has been annotated with @Specified. Tests are issued that check for fields,
 * methods, and constructors that have this annotation. These tests can be
 * distributed and applied to other, unannotated implementations to confirm
 * their conformance to the specification.
 * 
 * Throughout this class the classes which have been annotated and from which
 * tests are derived define the "expected" specification. The classes to which
 * these tests are eventually applied are the "actual" implementations.
 * 
 * @author cjohnson
 */
public class SpecCheckGenerator {
  /**
   * The modifiers that we examine when comparing methods, classes, and member
   * variables.
   */
  private enum ModifierName {
    Public,
    Private,
    Protected,
    Final,
    Interface,
    Abstract,
    Static
  };
  
  static void generateClassExistenceTest(Class<?>... clazzes) {
    System.out.println("@SpecCheckTest(order=0)");
    System.out.println("@Test");
    System.out.println("public void testForClasses() throws Exception {");
    for (Class<?> clazz : clazzes) {
      System.out.printf("  try {%n" +
                        "     Class.forName(\"%1$s\");%n" +
                        "  } catch (ClassNotFoundException e) {%n" +
                        "     Assert.fail(\"A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  } catch (NoClassDefFoundError e) {%n" +
                        "     Assert.fail(\"A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  }%n", getRequestedClassName(clazz));
    }
    System.out.println("}");
  }
  
  private static String getRequestedClassName(Class<?> clazz) {
    Specified anno = clazz.getAnnotation(Specified.class);
    if (anno == null || anno.inPackage().isEmpty()) {
      return clazz.getName();
    } else {
      return anno.inPackage() + "." + clazz.getSimpleName();
    }
  }

  /**
   * Generate tests for the specified class.
   * 
   * @param c
   * The class for which tests are generated.
   * 
   * @throws ClassNotFoundException
   */
  static void generateClassTest(Class<?> clazz) throws ClassNotFoundException {
    System.out.println("@SpecCheckTest(order=10)");
    System.out.println("@Test");
    System.out.println("public void test" + getReadableClassName(clazz) + "() throws Exception {");

    generateClassModifiersTest(clazz);
    generateClassSuperTest(clazz);
    generateClassInterfacesTest(clazz);

    Specified annotation = clazz.getAnnotation(org.twodee.speccheck.Specified.class);

    Field[] fields = clazz.getDeclaredFields();
    generateSpecifiedFieldsTests(clazz, fields);
    if (annotation != null && !annotation.allowUnspecifiedPublicStuff()) {
      generateUnspecifiedFieldsTests(clazz, fields);
    }

    System.out.println("  List<Class<?>> exceptions = null;");
    System.out.println("  List<Class<?>> outlawedExceptions = null;");

    Constructor<?>[] ctors = clazz.getDeclaredConstructors();
    generateSpecifiedConstructorsTests(clazz, ctors);
    if (annotation != null && !annotation.allowUnspecifiedPublicStuff()) {
      generateUnspecifiedConstructorsTests(clazz, ctors);
    }

    Method[] methods = clazz.getDeclaredMethods();
    generateSpecifiedMethodsTests(clazz, methods);
    if (annotation != null && !annotation.allowUnspecifiedPublicStuff()) {
      generateUnspecifiedMethodsTests(clazz, methods);
    }

    System.out.println("}");

    // Issue an independent test for catching variable-hungry developers.
    generateFieldCountTest(clazz);
  }

  /**
   * Issue a test to keep the number of member variables within reason,
   * promoting local variables.
   * 
   * @param c
   * The name of the class for which the test is generated.
   * @throws ClassNotFoundException
   * 
   * @todo I should not count constants. I should allow this number to be
   * parameterizable.
   */
  private static void generateFieldCountTest(Class<?> clazz) throws ClassNotFoundException {
    Specified anno = clazz.getAnnotation(Specified.class);

    if (anno != null && anno.maxVariableCount() >= 0) {
      System.out.println("@SpecCheckTest(order=5)");
      System.out.println("@Test");
      System.out.println("public void test" + getReadableClassName(clazz) + "FieldCount() throws Exception {");
      System.out.printf("  Field[] fields = null;%n");
      System.out.printf("  try {%n" +
                        "     fields = Class.forName(\"%1$s\").getDeclaredFields();%n" +
                        "  } catch (ClassNotFoundException e) {%n" +
                        "     Assert.fail(\"A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  } catch (NoClassDefFoundError e) {%n" +
                        "     Assert.fail(\"A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  }%n", getRequestedClassName(clazz));
      System.out.println("  int nInstanceVars = 0;");
      System.out.println("  for (Field f : fields) {");
      System.out.println("     if (!Modifier.isStatic(f.getModifiers())) {");
      System.out.println("       ++nInstanceVars;");
      System.out.println("     }");
      System.out.println("  }");
      System.out.printf("  Assert.assertTrue(\"You have a lot of instance variables in class %s. Perhaps some of them should be local variables?\", nInstanceVars <= %d);%n", getRequestedClassName(clazz), anno.maxVariableCount());
      System.out.println("}");
    }
  }

  /**
   * Issue a test that compares the expected modifiers of the class to the
   * actual. If the actual class can not be found, the exception is gracefully
   * handled with an informative failure message.
   * 
   * @param c
   * The name of the class for which the test is generated.
   * 
   * @throws ClassNotFoundException
   */
  private static void generateClassModifiersTest(Class<?> clazz) throws ClassNotFoundException {
    // Test class modifiers
    int modifiers = clazz.getModifiers();
    System.out.printf("  try {%n" +
                      "     Class<?> cls = Class.forName(\"%1$s\");%n" +
                      "     Assert.assertTrue(\"The modifiers for class %1$s are not correct. \" + SpecCheckUtilities.getModifierDiff(%2$d, cls.getModifiers()), %2$d == cls.getModifiers());%n" +
                      "  } catch (ClassNotFoundException e) {%n" +
                      "     Assert.fail(\"A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                      "  } catch (NoClassDefFoundError e) {%n" +
                      "     Assert.fail(\"A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                      "  }%n", getRequestedClassName(clazz), modifiers);
  }

  private static void generateClassSuperTest(Class<?> clazz) throws ClassNotFoundException {
    Specified annotation = clazz.getAnnotation(org.twodee.speccheck.Specified.class);
    if (annotation != null && annotation.checkSuper()) {
      System.out.printf("  try {%n" +
                        "     Class<?> cls = Class.forName(\"%1$s\");%n" +
                        "     Assert.assertEquals(\"The superclass of class %1$s is not correct.\", %2$s.class, cls.getSuperclass());%n" +
                        "  } catch (ClassNotFoundException e) {%n" +
                        "     Assert.fail(\"A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  } catch (NoClassDefFoundError e) {%n" +
                        "     Assert.fail(\"A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  }%n", getRequestedClassName(clazz), getRequestedClassName(clazz.getSuperclass()));
    }
  }

  private static void generateClassInterfacesTest(Class<?> clazz) throws ClassNotFoundException {
    Specified annotation = clazz.getAnnotation(org.twodee.speccheck.Specified.class);
    if (annotation != null) {
      Class<?>[] ifaces = annotation.mustImplement();
      System.out.printf("  Class<?> cls = Class.forName(\"%1$s\");%n" +
                        "  List<Class<?>> ifaces = java.util.Arrays.asList(cls.getInterfaces());%n", getRequestedClassName(clazz));
      for (Class<?> iface : ifaces) {
        System.out.printf("  Assert.assertTrue(\"Class %1$s must implement interface %2$s.\", ifaces.contains(%2$s.class));%n", getRequestedClassName(clazz), getRequestedClassName(iface), getRequestedClassName(iface));
      }
    }
  }

  /**
   * Issue a test for checking for unspecified public methods. These represent
   * entry points that the developer may be relying on that our client code may
   * know nothing about. Assumes non-precious variable named method has been
   * declared.
   * 
   * @param c
   * The name of the class for which the test is generated.
   * 
   * @param methods
   * All methods in the class. Those that are @Specified will be checked for the
   * in the actual code.
   */
  private static void generateUnspecifiedMethodsTests(Class<?> clazz,
                                                      Method[] methods) {
    // Get a list of all methods in the actual code.
    System.out.println("  LinkedList<Method> methods = new LinkedList<Method>();");
    System.out.printf("  for (Method m : Class.forName(\"%1$s\").getDeclaredMethods()) {%n", getRequestedClassName(clazz));
    System.out.println("     methods.add(m);");
    System.out.println("  }");

    // Generate code to remove any expected @Specified method from the list of
    // actual methods.
    for (Method m : methods) {
      if (m.isAnnotationPresent(Specified.class)) {
        Class<?>[] types = m.getParameterTypes();
        String list = SpecCheckUtilities.getTypesList(types);
        System.out.println("  try {");
        System.out.printf("    method = Class.forName(\"%1$s\").getDeclaredMethod(\"%2$s\", new Class<?>[]{%3$s});%n",
                          getRequestedClassName(clazz), m.getName(), list);
        System.out.println("    methods.remove(method);");
        System.out.println("  } catch (NoSuchMethodException e) {}");
      }
    }

    // Now, if there are any methods left in the list of actual methods, we fail
    // this test -- unless the leftovers are bridge methods. Bridge methods are
    // introduced for generic types. We don't want to worry about them. We also
    // allow for main to be present.
    System.out.println("  for (Method m : methods) {");
    System.out.println("    if (!m.isBridge() && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isProtected(m.getModifiers()) && !m.getName().equals(\"main\")) {");
    System.out.println("      Assert.fail(String.format(\"Method " + getRequestedClassName(clazz) + ".%1$s(%2$s) is not in the specification. Any methods you add should be private (or possibly protected).\", m.getName(), SpecCheckUtilities.getTypesList(m.getParameterTypes()).replaceAll(\".class\", \"\")));");
    System.out.println("    }");
    System.out.println("  }");
  }

  /**
   * Issue a test for checking for unspecified public constructors. These
   * represent entry points that the developer may be relying on that our client
   * code may know nothing about. Assumes non-precious variable named ctor has
   * been declared.
   * 
   * @param c
   * The name of the class for which the test is generated.
   * 
   * @param methods
   * All constructors in the class. Those that @Specified will be removed from
   * consideration. Public constructors remaining the actual code will cause the
   * test to fail.
   * @throws ClassNotFoundException
   */
  private static void generateUnspecifiedConstructorsTests(Class<?> clazz,
                                                           Constructor<?>[] ctors) throws ClassNotFoundException {
    // Get a list of all methods in the actual code.
    System.out.println("  LinkedList<Constructor<?>> ctors = new LinkedList<Constructor<?>>();");
    System.out.printf("  for (Constructor<?> actual : Class.forName(\"%1$s\").getDeclaredConstructors()) {%n", getRequestedClassName(clazz));
    System.out.println("     ctors.add(actual);");
    System.out.println("  }");

    // Generate code to remove any expected @Specified method from the list of
    // actual methods.
    for (Constructor<?> ctor : ctors) {
      if (ctor.isAnnotationPresent(Specified.class)) {
        Class<?>[] types = ctor.getParameterTypes();
        String list = SpecCheckUtilities.getTypesList(types);
        System.out.println("  try {");
        System.out.printf("    ctor = Class.forName(\"%1$s\").getDeclaredConstructor(new Class<?>[]{%2$s});%n", getRequestedClassName(clazz), list);
        System.out.println("    ctors.remove(ctor);");
        System.out.println("  } catch (NoSuchMethodException e) {}");
      }
    }

    // Now, if there are any constructors left in the list of actual
    // constructors, we fail
    // this test.
    System.out.println("  for (Constructor<?> actual : ctors) {");
    System.out.print("    if (Modifier.isPublic(actual.getModifiers())");

    Specified annotation = clazz.getAnnotation(org.twodee.speccheck.Specified.class);
    if (annotation == null || annotation.allowUnspecifiedPublicDefaultCtor()) {
      System.out.print(" && actual.getParameterTypes().length != 0");
    }

    System.out.println(") {");
    System.out.println("      Assert.fail(String.format(\"Constructor %1$s(%2$s) is not in the specification. Any constructors you add should be private (or possibly protected).\", actual.getName(), SpecCheckUtilities.getTypesList(actual.getParameterTypes()).replaceAll(\".class\", \"\")));");
    System.out.println("    }");
    System.out.println("  }");
  }

  /**
   * Issue a test for checking for unspecified public fields. These represent
   * entry points that the developer may be relying on that our client code may
   * know nothing about. Assumes non-precious variable named field has been
   * declared.
   * 
   * @param c
   * The name of the class for which the test is generated.
   * 
   * @param fields
   * All fields in the class. Those that @Specified will be removed from
   * consideration. Public fields remaining the actual code will cause the test
   * to fail.
   * @throws ClassNotFoundException
   */
  private static void generateUnspecifiedFieldsTests(Class<?> clazz,
                                                     Field[] fields) throws ClassNotFoundException {
    // Get a list of all fields in the actual code.
    System.out.println("  LinkedList<Field> fields = new LinkedList<Field>();");
    System.out.printf("  for (Field actual : Class.forName(\"%1$s\").getDeclaredFields()) {%n", getRequestedClassName(clazz));
    System.out.println("     fields.add(actual);");
    System.out.println("  }");

    // Generate code to remove any expected @Specified field from the list of
    // actual fields.
    for (Field f : fields) {
      if (f.isAnnotationPresent(Specified.class)) {
        System.out.println("  try {");
        System.out.printf("    field = Class.forName(\"%1$s\").getDeclaredField(\"%2$s\");%n", getRequestedClassName(clazz), f.getName());
        System.out.println("    fields.remove(field);");
        System.out.println("  } catch (NoSuchFieldException e) {}");
      }
    }

    // Now, if there are any fields left in the list of actual fields, we fail
    // this test. If we find an instance variable, we assert that it is private
    // or protected and give a targeted message. In general, we assert that no
    // unspecified public or package level variable exists.
    System.out.println("  for (Field actual : fields) {");
    System.out.println("    if (Modifier.isStatic(actual.getModifiers())) {");

    Specified annotation = clazz.getAnnotation(org.twodee.speccheck.Specified.class);
    if (annotation == null || !annotation.allowUnspecifiedPublicConstants()) {
      System.out.println("      Assert.assertTrue(String.format(\"Field " + getRequestedClassName(clazz) + ".%1$s is not in the specification. Any static fields you add should be private.\", actual.getName()), Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));");
    }

    System.out.println("    } else {");
    System.out.println("      Assert.assertTrue(\"Instance variables must be private (or possibly protected). " + getRequestedClassName(clazz) + ".\" + actual.getName() + \" is not. The only public variables should be specified constants, which must be static and final.\", Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));");
    System.out.println("    }");
    System.out.println("  }");
  }

  /**
   * Issue a test ensuring that any expected specified methods is present, named
   * correctly, has correct arguments, and returns a value of the correct type.
   * 
   * @param c
   * The name of the class for which the test is generated.
   * 
   * @param methods
   * All methods in the class. Those that are @Specified will be checked for the
   * in the actual code.
   */
  private static void generateSpecifiedMethodsTests(Class<?> clazz,
                                                    Method[] methods) {
    System.out.println("  Method method = null;");

    // Look for each expected @Specified method in the actual methods.
    for (Method m : methods) {
      if (m.isAnnotationPresent(Specified.class)) {
        Class<?>[] types = m.getParameterTypes();
        String list = SpecCheckUtilities.getTypesList(types);
        String readableList = list.replaceAll(".class", "");

        // Look for expected method in actual class. If we fail, then the method
        // may
        // be named wrong or the arguments might be wrong.
        System.out.println("  try {");
        System.out.printf("    method = Class.forName(\"%1$s\").getDeclaredMethod(\"%2$s\", new Class<?>[]{%3$s});%n", getRequestedClassName(clazz), m.getName(), list);
        System.out.println("  } catch (NoSuchMethodException e) {");
        System.out.printf("    Assert.fail(\"You need a %1$s method in class %4$s taking %2$d argument%3$s", m.getName(), types.length, (types.length == 1 ? "" : "s"), getRequestedClassName(clazz));
        if (types.length > 0) {
          System.out.printf(", having type%1$s %2$s", types.length == 1 ? "" : "s", readableList);
        }
        System.out.println(".\");");
        System.out.println("  }");

        // Also verify return type and modifiers are correct.
        System.out.printf("  Assert.assertEquals(\"Your method %1$s(%2$s) in class %4$s has the wrong return type.\", %3$s.class, method.getReturnType());%n", m.getName(), readableList, m.getReturnType().getCanonicalName(), getRequestedClassName(clazz));
        System.out.printf("  Assert.assertTrue(\"The modifiers for method %1$s(%2$s) in class %4$s are not correct. \" + SpecCheckUtilities.getModifierDiff(%3$d, method.getModifiers()), %3$d == method.getModifiers());%n", m.getName(), readableList, m.getModifiers(), getRequestedClassName(clazz));

        Class<?>[] exceptions = m.getAnnotation(Specified.class).mustThrow();
        System.out.println("  exceptions = java.util.Arrays.asList(method.getExceptionTypes());");
        for (Class<?> exception : exceptions) {
          System.out.printf("  Assert.assertTrue(\"The specification requires method %1$s(%2$s) in class %4$s to throw %3$s.\", exceptions.contains(%3$s.class));%n", m.getName(), readableList, getRequestedClassName(exception), getRequestedClassName(clazz));
        }

        exceptions = m.getAnnotation(Specified.class).mustNotThrow();
        String blacklist = "";
        for (Class<?> exception : exceptions) {
          blacklist += getRequestedClassName(exception) + ".class, ";
        }
        System.out.println("  outlawedExceptions = java.util.Arrays.asList(new Class<?>[]{" + blacklist + "});");
        System.out.println("  for (Class<?> exception : exceptions) {");
        System.out.printf("    Assert.assertFalse(\"The specification requires method %1$s(%2$s) in class %3$s to handle and not throw \" + exception.getName() + \".\", outlawedExceptions.contains(exception));%n", m.getName(), readableList, getRequestedClassName(clazz));
        System.out.println("  }");
      }
    }
  }

  /**
   * Issue a test ensuring that expected specified constructors are present,
   * named correctly, having correct arguments, and returning values of the
   * correct type.
   * 
   * @param c
   * The name of the class for which the test is generated.
   * 
   * @param ctors
   * All constructors in the class. Those that are @Specified will be checked
   * for the in the actual code.
   */
  private static void generateSpecifiedConstructorsTests(Class<?> clazz,
                                                         Constructor<?>[] ctors) {
    System.out.println("  Constructor<?> ctor = null;");

    // Look for each expected @Specified method in the actual methods.
    for (Constructor<?> ctor : ctors) {
      if (ctor.isAnnotationPresent(Specified.class)) {
        Class<?>[] types = ctor.getParameterTypes();
        String list = SpecCheckUtilities.getTypesList(types);
        String readableList = list.replaceAll(".class", "");

        // Look for expected method in actual class. If we fail, then the method
        // may
        // be named wrong or the arguments might be wrong.
        System.out.println("  try {");
        System.out.printf("    ctor = Class.forName(\"%1$s\").getDeclaredConstructor(new Class<?>[]{%2$s});%n", getRequestedClassName(clazz), list);
        System.out.println("  } catch (NoSuchMethodException e) {");
        System.out.printf("    Assert.fail(\"You need a constructor in class %1$s taking %2$d argument%3$s", getRequestedClassName(clazz), types.length, (types.length == 1 ? "" : "s"));
        if (types.length > 0) {
          System.out.printf(", having type%1$s %2$s", types.length == 1 ? "" : "s", readableList);
        }
        System.out.println(".\");");
        System.out.println("  }");

        // Also verify return type and modifiers are correct.
        System.out.printf("  Assert.assertTrue(\"The modifiers for constructor %1$s(%2$s) are not correct. \" + SpecCheckUtilities.getModifierDiff(%3$d, ctor.getModifiers()), %3$d == ctor.getModifiers());%n", ctor.getName(), readableList, ctor.getModifiers());

        Class<?>[] exceptions = ctor.getAnnotation(Specified.class).mustThrow();
        System.out.println("  exceptions = java.util.Arrays.asList(ctor.getExceptionTypes());");
        for (Class<?> exception : exceptions) {
          System.out.printf("  Assert.assertTrue(\"The specification requires constructor %1$s(%2$s) to throw %3$s.\", exceptions.contains(%3$s.class));%n", ctor.getName(), readableList, getRequestedClassName(exception));
        }

        exceptions = ctor.getAnnotation(Specified.class).mustNotThrow();
        String blacklist = "";
        for (Class<?> exception : exceptions) {
          blacklist += getRequestedClassName(exception) + ".class, ";
        }
        System.out.println("  outlawedExceptions = java.util.Arrays.asList(new Class<?>[]{" + blacklist + "});");
        System.out.println("  for (Class<?> exception : exceptions) {");
        System.out.printf("    Assert.assertFalse(\"The specification requires constructor %1$s(%2$s) to handle and not throw \" + exception.getName() + \".\", outlawedExceptions.contains(exception));%n", ctor.getName(), readableList);
        System.out.println("  }");
      }
    }
  }

  /**
   * Issue a test ensuring that any expected specified methods is present, named
   * correctly, has correct arguments, and returns a value of the correct type.
   * 
   * @param c
   * The name of the class for which the test is generated.
   * 
   * @param methods
   * All methods in the class. Those that are @Specified will be checked for the
   * in the actual code.
   */
  private static void generateSpecifiedFieldsTests(Class<?> clazz,
                                                   Field[] fields) {
    System.out.println("  Field field = null;");

    // Look for each expected @Specified method in the actual methods.
    for (Field f : fields) {
      if (f.isAnnotationPresent(Specified.class)) {

        // Look for expected field in actual class. If we fail, then the field
        // may be named wrong.
        System.out.println("  try {");
        System.out.printf("    field = Class.forName(\"%1$s\").getDeclaredField(\"%2$s\");%n", getRequestedClassName(clazz), f.getName());
        System.out.println("  } catch (NoSuchFieldException e) {");
        System.out.printf("    Assert.fail(\"You need a field named %1$s in class %2$s.\");%n", f.getName(), getRequestedClassName(clazz));
        System.out.println("  }");
        
        System.out.printf("  Assert.assertEquals(\"Field %1$s.%2$s is of the wrong type.\", %3$s.class, field.getType());%n", getRequestedClassName(clazz), f.getName(), f.getType().getCanonicalName());

        // Also verify modifiers are correct.
        System.out.printf("  Assert.assertTrue(\"The modifiers for field %1$s in class %3$s are not correct. \" + SpecCheckUtilities.getModifierDiff(%2$d, field.getModifiers()), %2$d == field.getModifiers());%n", f.getName(), f.getModifiers(), getRequestedClassName(clazz));
      }
    }
  }

  /**
   * Get a class name in which all package-separating periods (.) have been
   * removed. All names of packages are given initial capital letters. For
   * instance, java.lang.String becomes javaLangString. Why is this useful?
   * Because the returned name can be used as a meaningful and unique Java
   * identifier.
   * 
   * @param name
   * The name of the class to transform. May contain package qualifications.
   * 
   * @return A camelCase name of the class, without periods and with internal
   * capitalized words.
   */
  private static String getReadableClassName(Class<?> clazz) {
    Pattern p = Pattern.compile("(\\.|^)(\\w)");
    Matcher m = p.matcher(getRequestedClassName(clazz));
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, "");
      sb.append(m.group(2).toUpperCase()); // 2 is the first letter (\\w)
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
