/*
 SpecCheck - a system for automatically generating tests for interface-conformance
 Copyright (C) 2012 Chris Johnson

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.twodee.speccheck;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  private String unitTests = null;
  private String course;
  private String semester;
  private String tag;
  private int version;
  private ArrayList<String> filesToZip = new ArrayList<String>();
  private String pathToSource = null;
  private Set<String> importLines = new TreeSet<String>();
  private ArrayList<String> extraTypes = new ArrayList<String>();

  public SpecCheckGenerator() {
  }

  public void setPathToSource(String path) {
    pathToSource = path;
  }

  public SpecCheckGenerator(String pathToUnitTests) throws IOException {
    String body = FileUtilities.slurp(pathToUnitTests);
    Pattern pattern = Pattern.compile("^public class .*? \\{(.*)^\\}", Pattern.DOTALL | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(body);
    if (matcher.find()) {
      unitTests = matcher.group(1);
    }

    // Cull imports from unit tests.
    Pattern importPattern = Pattern.compile("^import.*$\\r?\\n", Pattern.MULTILINE);
    Matcher importMatcher = importPattern.matcher(body);
    while (importMatcher.find()) {
      importLines.add(importMatcher.group());
    }
  }

  public void addExtraTypes(String... types) {
    extraTypes.addAll(Arrays.asList(types));
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public void setMeta(String course, String semester, String tag, int version) {
    this.course = course;
    this.semester = semester;
    this.tag = tag;
    this.version = version;
  }

  public void setFilesToZip(String... paths) {
    File[] files = new File[paths.length];
    for (int i = 0; i < paths.length; ++i) {
      files[i] = new File(paths[i]);
    }
    setFilesToZip(files);
  }

  public void setDirectoryToZip(String path) {
    filesToZip.add(path);
  }

  public void setFilesToZip(File... files) {
    for (File f : files) {
      if (!f.getName().equals("speccheck") && !f.getName().equals("CVS")) {
        if (f.isDirectory()) {
          setFilesToZip(f.listFiles());
        } else {
          filesToZip.add(f.getPath());
        }
      }
    }
  }

  private String get(Class<?>... clazzes) throws ClassNotFoundException {
    PrintStream oldOut = System.out;
    String generated = null;
    String preTests = "";
    String interfaceTests = "";

    ByteArrayOutputStream newOut = new ByteArrayOutputStream();
    try {
      System.setOut(new PrintStream(newOut));
      System.out.println();
      generateVersionTest();
      System.out.println();
      SpecCheckGenerator.generateClassExistenceTest(clazzes);
    } finally {
      System.setOut(oldOut);
      preTests = newOut.toString();
    }

    newOut.reset();
    try {
      System.setOut(new PrintStream(newOut));
      for (Class<?> clazz : clazzes) {
        SpecCheckGenerator.generateClassTest(clazz);
      }
    } finally {
      System.setOut(oldOut);
      interfaceTests = newOut.toString();
    }

    try {
      generated = substitute(preTests, interfaceTests);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return generated;
  }

  public void generateInto(String checkerQualifiedName,
                           Class<?>... clazzes) throws ClassNotFoundException {

    String generated = get(clazzes);
    int iLastDot = checkerQualifiedName.lastIndexOf('.');
    String checkerPackageLine = "";
    if (iLastDot >= 0) {
      checkerPackageLine = "package " + checkerQualifiedName.substring(0, iLastDot) + ";";
    }
    String checkerClass = checkerQualifiedName.substring(iLastDot + 1);
    generated = generated.replaceFirst("public class SpecChecker", "public class " + checkerClass);

    StringBuffer buffer = new StringBuffer();
    buffer.append(checkerPackageLine);
    buffer.append(System.lineSeparator());

    for (String importLine : importLines) {
      buffer.append(importLine);
    }

    generated = generated.replaceFirst("package org.twodee.speccheck;", buffer.toString());
    System.out.println(generated);
  }

  private String substitute(String preTests,
                            String interfaceTests) throws IOException {
    System.out.println("/* -----------------");
    System.out.println(preTests);
    System.out.println("______________________");
    System.out.println(interfaceTests);
    System.out.println("*/");

    // Collect up all the source files.
    File packageDirectory = new File(pathToSource, "/src/org/twodee/speccheck");
    File[] sources = packageDirectory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir,
                            String name) {
        return name.endsWith(".java");
      }
    });

    // Read them in.
    HashMap<String, String> namesToSources = new HashMap<String, String>();
    for (File source : sources) {
      namesToSources.put(source.getName(), FileUtilities.slurp(source));
    }
    
    // These two are only needed for generating.
    namesToSources.remove("SpecCheckGenerator.java");
    /* namesToSources.remove("Specified.java"); */

    // We'll inline them all into the SpecChecker class, let's remove that from
    // the collection, since it'll be treated specially.
    String omniSource = namesToSources.remove("SpecChecker.java");

    // Now we flesh out the test suite to include the generated tests and the
    // caller's unit tests.
    String suiteSource = namesToSources.get("SpecCheckTestSuite.java");
    suiteSource = suiteSource.replaceFirst("(?<=class SpecCheckPreTests \\{)", Matcher.quoteReplacement(preTests));
    suiteSource = suiteSource.replaceFirst("(?<=class SpecCheckInterfaceTests \\{)", Matcher.quoteReplacement(interfaceTests));
    if (unitTests != null) {
      suiteSource = suiteSource.replaceFirst("(?<=class SpecCheckUnitTests \\{)", Matcher.quoteReplacement(unitTests));
    }

    if (extraTypes.size() > 0) {
      String types = "";
      for (int i = 0; i < extraTypes.size(); ++i) {
        if (i > 0) {
          types += ", ";
        }
        types += "\"" + extraTypes.get(i) + "\"";
      }
      suiteSource = suiteSource.replaceFirst("// EXTRATYPES", Matcher.quoteReplacement("extraTypes.addAll(Arrays.asList(" + types + "));"));
    }

    namesToSources.put("SpecCheckTestSuite.java", suiteSource);
    
    // Append all the helpers to omni, stripping out the public.
    Pattern lastCurlyPattern = Pattern.compile("(?=}[^}]*\\Z)", Pattern.DOTALL);

    Pattern packagePattern = Pattern.compile("^package.*$\\r?\\n", Pattern.MULTILINE);
    Pattern importPattern = Pattern.compile("^import.*$\\r?\\n", Pattern.MULTILINE);
    Pattern publicPattern = Pattern.compile("^public \\s*", Pattern.MULTILINE);
 
    // Cull imports from overall.
    Matcher importMatcher = importPattern.matcher(omniSource);
    while (importMatcher.find()) {
      importLines.add(importMatcher.group());
    }
    omniSource = importPattern.matcher(omniSource).replaceAll("");

    for (String source : namesToSources.values()) {
      source = packagePattern.matcher(source).replaceFirst("");
      importMatcher = importPattern.matcher(source);
      while (importMatcher.find()) {
        importLines.add(importMatcher.group());
      }
      source = importPattern.matcher(source).replaceAll("");
      source = publicPattern.matcher(source).replaceFirst("public static ");
      omniSource = lastCurlyPattern.matcher(omniSource).replaceFirst(Matcher.quoteReplacement(source));
    }

    // Update some variables referenced by the SpecChecker.
    omniSource = omniSource.replaceFirst("tag = \"hw\"", "tag = \"" + tag + "\"");
    String filesCommaSeparated = "";
    for (String path : filesToZip) {
      filesCommaSeparated += "\"" + path + "\", ";
    }
    omniSource = omniSource.replaceFirst("filesToZip = \\{\\}", "filesToZip = {" + filesCommaSeparated + "}");

    return omniSource;
  }

  void generateVersionTest() {
    System.out.println("@SpecCheckTest(order=0)");
    System.out.println("@Test");
    System.out.println("public void testForVersion() throws Exception {");
    System.out.printf("  assertVersion(\"%s\", \"%s\", \"%s\", %d);%n", course, semester, tag, version);
    System.out.println("}");
  }

  static void generateClassExistenceTest(Class<?>... clazzes) {
    System.out.println("@SpecCheckTest(order=1)");
    System.out.println("@Test");
    System.out.println("public void testForClasses() throws Exception {");
    for (Class<?> clazz : clazzes) {
      System.out.printf("  try {%n" +
                        "     Class.forName(\"%1$s\");%n" +
                        "  } catch (ClassNotFoundException e) {%n" +
                        "     Assert.fail(\"A class by the name of %2$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  } catch (NoClassDefFoundError e) {%n" +
                        "     Assert.fail(\"A class by the name of %2$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  }%n", clazz.getName(), clazz.getCanonicalName());
    }
    System.out.println("}");
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

    Specified annotation = clazz.getAnnotation(Specified.class);

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

    if (clazz.isEnum()) {
      List<Enum<?>> enums = (List<Enum<?>>) Arrays.asList(clazz.getEnumConstants());
      for (Enum<?> e : enums) {
        System.out.println("  try {");
        System.out.printf("    Field f = Class.forName(\"%1$s\").getDeclaredField(\"%2$s\");%n", clazz.getName(), e.name());
        System.out.printf("    Assert.assertTrue(\"Must be an enum.\", f.isEnumConstant());%n");
        System.out.println("  } catch (NoSuchFieldException e) {");
        System.out.printf("    Assert.fail(\"Enum *** is missing.\");%n");
        System.out.println("  }");
      }
    }

    // Issue some tests on the source code.
    generateSourceTests(clazz);

    System.out.println("}");

    // Issue an independent test for catching variable-hungry developers.
    generateFieldCountTest(clazz);
  }

  private static void generateSourceTests(Class<?> clazz) {
    String path = "src/" + clazz.getName().replace('.', '/') + ".java";
    System.out.println("  // Check sourcies");
    System.out.println("  String srcPath = \"" + path + "\";");
    System.out.println("  sources.add(srcPath);");
    System.out.println("  String src = FileUtilities.slurp(srcPath);");
    System.out.println("  Pattern pattern = Pattern.compile(\"^\\\\s*import\\\\s+(?!hw\\\\d+.)(?!org\\\\.twodee\\\\.)(?!java\\\\.)(?!javax\\\\.)(.*?)\\\\s*;\", Pattern.MULTILINE);");
    System.out.println("  Matcher matcher = pattern.matcher(src);");
    System.out.println("  if (matcher.find()) {");
    System.out.println("    Assert.fail(String.format(\"Class " + clazz.getName() + " imports %s. You may only import classes from standard packages (those whose fully-qualified names match \\\"java.*\\\"). Not every machine supports the non-standard packages.\", matcher.group(1)));");
    System.out.println("  }");
    System.out.println();
    System.out.println("  pattern = Pattern.compile(\"(false|true)\\\\s*(==|!=)|(==|!=)\\\\s*(false|true)\", Pattern.MULTILINE);");
    System.out.println("  matcher = pattern.matcher(src);");
    System.out.println("  if (matcher.find()) {");
    System.out.println("    Assert.fail(String.format(\"Class " + clazz.getName() + " contains the comparison \\\"%s\\\". Simplify your code; you never need compare to a boolean literal. Eliminate \\\"== true\\\" and \\\"!= false\\\" altogether. Rewrite \\\"== false\\\" and \\\"!= true\\\" to use the ! operator. With meaningful variable names, your code will be much more readable without these comparisons to boolean literals.\", matcher.group()));");
    System.out.println("  }");
    System.out.println();

    // Carriage return
    System.out.println("  pattern = Pattern.compile(\"\\\\\\\\r\");");
    System.out.println("  matcher = pattern.matcher(src);");
    System.out.println("  if (matcher.find()) {");
    System.out.println("    Assert.fail(String.format(\"Class " + clazz.getName() + " contains a carriage return character (\\\\r). Carriage returns are only valid on the Windows operating system. Please use a cross-platform way of generating linebreaks, such as println, %%n in format strings, or System.lineSeparator().\", matcher.group()));");
    System.out.println("  }");
    System.out.println();

    // Linefeed
    System.out.println("  pattern = Pattern.compile(\"\\\\\\\\n\");");
    System.out.println("  matcher = pattern.matcher(src);");
    System.out.println("  if (matcher.find()) {");
    System.out.println("    Assert.fail(String.format(\"Class " + clazz.getName() + " contains a linefeed character (\\\\n). Linefeeds are not valid on all operating systems. Please use a cross-platform way of generating linebreaks, such as println, %%n in format strings, or System.lineSeparator().\", matcher.group()));");
    System.out.println("  }");
 
    // Windows backslashes
    System.out.println("  pattern = Pattern.compile(\"\\\\\\\\\\\\\\\\\");");
    System.out.println("  matcher = pattern.matcher(src);");
    System.out.println("  if (matcher.find()) {");
    System.out.println("    Assert.fail(String.format(\"Class " + clazz.getName() + " contains what looks like a Windows-only directory separator (\\\\, but escaped). Backslash is only valid on Windows and not other operating systems. Please use a cross-platform way of separating directories, such as forward slash (/), the File(parentDirectory, child) constructor, or File.separator.\", matcher.group()));");
    System.out.println("  }");
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
                        "     Assert.fail(\"A class by the name of %2$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  } catch (NoClassDefFoundError e) {%n" +
                        "     Assert.fail(\"A class by the name of %2$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  }%n", clazz.getName(), clazz.getCanonicalName());
      System.out.println("  int nInstanceVars = 0;");
      System.out.println("  for (Field f : fields) {");
      System.out.println("     if (!Modifier.isStatic(f.getModifiers())) {");
      System.out.println("       ++nInstanceVars;");
      System.out.println("     }");
      System.out.println("  }");
      System.out.printf("  Assert.assertTrue(\"You have a lot of instance variables in class %s. Perhaps some of them should be local variables?\", nInstanceVars <= %d);%n", clazz.getCanonicalName(), anno.maxVariableCount());
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
                      "     Assert.assertTrue(\"The modifiers for class %2$s are not correct. \" + SpecCheckUtilities.getModifierDifference(%3$d, cls.getModifiers()), %3$d == cls.getModifiers());%n" +
                      "  } catch (ClassNotFoundException e) {%n" +
                      "     Assert.fail(\"A class by the name of %2$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                      "  } catch (NoClassDefFoundError e) {%n" +
                      "     Assert.fail(\"A class by the name of %2$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                      "  }%n", clazz.getName(), clazz.getCanonicalName(), modifiers);
  }

  private static void generateClassSuperTest(Class<?> clazz) throws ClassNotFoundException {
    Specified annotation = clazz.getAnnotation(Specified.class);
    if (annotation != null && annotation.checkSuper()) {
      System.out.printf("  try {%n" +
                        "     Class<?> cls = Class.forName(\"%1$s\");%n" +
                        "     Assert.assertEquals(\"The superclass of class %2$s is not correct.\", %3$s.class, cls.getSuperclass());%n" +
                        "  } catch (ClassNotFoundException e) {%n" +
                        "     Assert.fail(\"A class by the name of %2$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  } catch (NoClassDefFoundError e) {%n" +
                        "     Assert.fail(\"A class by the name of %2$s could not be found. Check case, spelling, and that you created your class in the right package.\");%n" +
                        "  }%n", clazz.getName(), clazz.getCanonicalName(), clazz.getSuperclass().getCanonicalName());
    }
  }

  private static void generateClassInterfacesTest(Class<?> clazz) throws ClassNotFoundException {
    Specified annotation = clazz.getAnnotation(Specified.class);
    if (annotation != null) {
      Class<?>[] ifaces = annotation.mustImplement();
      System.out.printf("  Class<?> cls = Class.forName(\"%1$s\");%n" +
                        "  List<Class<?>> ifaces = java.util.Arrays.asList(cls.getInterfaces());%n", clazz.getName());
      for (Class<?> iface : ifaces) {
        System.out.printf("  Assert.assertTrue(\"Class %1$s must implement interface %2$s.\", ifaces.contains(%2$s.class));%n", clazz.getCanonicalName(), iface.getCanonicalName(), iface.getCanonicalName());
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
    System.out.printf("  for (Method m : Class.forName(\"%1$s\").getDeclaredMethods()) {%n", clazz.getName());
    System.out.println("     methods.add(m);");
    System.out.println("  }");

    // Generate code to remove any expected @Specified method from the list of
    // actual methods.
    for (Method m : methods) {
      if (m.isAnnotationPresent(Specified.class) || clazz.isEnum()) {
        Class<?>[] types = m.getParameterTypes();
        String list = SpecCheckUtilities.getTypesList(types);
        System.out.println("  try {");
        System.out.printf("    method = Class.forName(\"%1$s\").getDeclaredMethod(\"%2$s\", new Class<?>[]{%3$s});%n",
                          clazz.getName(), m.getName(), list);
        System.out.println("    methods.remove(method);");
        System.out.println("  } catch (NoSuchMethodException e) {}");
      }
    }

    // Now, if there are any methods left in the list of actual methods, we fail
    // this test -- unless the leftovers are bridge methods. Bridge methods are
    // introduced for generic types. We don't want to worry about them. We also
    // allow for main to be present. Also, if we have private inner classes, a
    // synthetic accessor method is made for those. We allow public synthetic
    // methods.
    System.out.println("  for (Method m : methods) {");
    System.out.printf("    if (!m.isBridge() && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isProtected(m.getModifiers()) && !m.isSynthetic() && !m.getName().equals(\"main\")) {%n", clazz.getCanonicalName());
    System.out.println("      Assert.fail(String.format(\"Method " + clazz.getCanonicalName() + ".%1$s(%2$s) is not in the specification. Any methods you add should be private (or possibly protected).\", m.getName(), SpecCheckUtilities.getTypesList(m.getParameterTypes()).replaceAll(\".class\", \"\")));");
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
    System.out.printf("  for (Constructor<?> actual : Class.forName(\"%1$s\").getDeclaredConstructors()) {%n", clazz.getName());
    System.out.println("     ctors.add(actual);");
    System.out.println("  }");

    // Generate code to remove any expected @Specified method from the list of
    // actual methods.
    for (Constructor<?> ctor : ctors) {
      if (ctor.isAnnotationPresent(Specified.class)) {
        Class<?>[] types = ctor.getParameterTypes();
        String list = SpecCheckUtilities.getTypesList(types);
        System.out.println("  try {");
        System.out.printf("    ctor = Class.forName(\"%1$s\").getDeclaredConstructor(new Class<?>[]{%2$s});%n", clazz.getName(), list);
        System.out.println("    ctors.remove(ctor);");
        System.out.println("  } catch (NoSuchMethodException e) {}");
      }
    }

    // Now, if there are any constructors left in the list of actual
    // constructors, we fail
    // this test.
    System.out.println("  for (Constructor<?> actual : ctors) {");
    System.out.print("    if (Modifier.isPublic(actual.getModifiers()) && !actual.isSynthetic()");

    Specified annotation = clazz.getAnnotation(Specified.class);
    if (annotation == null || annotation.allowUnspecifiedPublicDefaultCtor()) {
      System.out.print(" && actual.getParameterTypes().length != 0");
    }

    System.out.println(") {");
    System.out.println("      Assert.fail(String.format(\"Constructor " + clazz.getCanonicalName() + "(%1$s) is not in the specification. Any constructors you add should be private (or possibly protected).\", SpecCheckUtilities.getTypesList(actual.getParameterTypes()).replaceAll(\".class\", \"\")));");
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
    System.out.printf("  for (Field actual : Class.forName(\"%1$s\").getDeclaredFields()) {%n", clazz.getName());
    System.out.println("     fields.add(actual);");
    System.out.println("  }");

    // Generate code to remove any expected @Specified field from the list of
    // actual fields.
    for (Field f : fields) {
      if (f.isAnnotationPresent(Specified.class)) {
        System.out.println("  try {");
        System.out.printf("    field = Class.forName(\"%1$s\").getDeclaredField(\"%2$s\");%n", clazz.getName(), f.getName());
        System.out.println("    fields.remove(field);");
        System.out.println("  } catch (NoSuchFieldException e) {}");
      }
    }

    // Now, if there are any fields left in the list of actual fields, we fail
    // this test. If we find an instance variable, we assert that it is private
    // or protected and give a targeted message. In general, we assert that no
    // unspecified public or package level variable exists.
    System.out.println("  for (Field actual : fields) {");
    System.out.println("    if (!actual.isEnumConstant()) {");
    System.out.println("      if (Modifier.isStatic(actual.getModifiers())) {");

    Specified annotation = clazz.getAnnotation(Specified.class);
    if (annotation == null || !annotation.allowUnspecifiedPublicConstants()) {
      System.out.println("        Assert.assertTrue(String.format(\"Field " + clazz.getCanonicalName() + ".%1$s is not in the specification. Any static fields you add should be private.\", actual.getName()), Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));");
    }

    System.out.println("      } else {");
    System.out.println("        Assert.assertTrue(\"Instance variables must be private (or possibly protected). " + clazz.getCanonicalName() + ".\" + actual.getName() + \" is not. The only public variables should be specified constants, which must be static and final.\", Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()) || actual.isSynthetic());");
    System.out.println("      }");
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
        System.out.printf("    method = Class.forName(\"%1$s\").getDeclaredMethod(\"%2$s\", new Class<?>[]{%3$s});%n", clazz.getName(), m.getName(), list);
        System.out.println("  } catch (NoSuchMethodException e) {");
        System.out.printf("    Assert.fail(\"You need a %1$s method in class %4$s taking %2$d argument%3$s", m.getName(), types.length, (types.length == 1 ? "" : "s"), clazz.getCanonicalName());
        if (types.length > 0) {
          System.out.printf(", having type%1$s %2$s", types.length == 1 ? "" : "s", readableList);
        }
        System.out.println(".\");");
        System.out.println("  }");

        // Also verify return type and modifiers are correct.
        System.out.printf("  Assert.assertEquals(\"Your method %1$s(%2$s) in class %4$s has the wrong return type.\", %3$s.class, method.getReturnType());%n", m.getName(), readableList, m.getReturnType().getCanonicalName(), clazz.getCanonicalName());
        System.out.printf("  Assert.assertTrue(\"The modifiers for method %1$s(%2$s) in class %4$s are not correct. \" + SpecCheckUtilities.getModifierDifference(%3$d, method.getModifiers()), %3$d == method.getModifiers());%n", m.getName(), readableList, m.getModifiers(), clazz.getCanonicalName());

        Class<?>[] exceptions = m.getAnnotation(Specified.class).mustThrow();
        System.out.println("  exceptions = java.util.Arrays.asList(method.getExceptionTypes());");
        for (Class<?> exception : exceptions) {
          System.out.printf("  Assert.assertTrue(\"The specification requires method %1$s(%2$s) in class %4$s to throw %3$s.\", exceptions.contains(%3$s.class));%n", m.getName(), readableList, exception.getCanonicalName(), clazz.getCanonicalName());
        }

        exceptions = m.getAnnotation(Specified.class).mustNotThrow();
        String blacklist = "";
        for (Class<?> exception : exceptions) {
          blacklist += exception.getCanonicalName() + ".class, ";
        }
        System.out.println("  outlawedExceptions = java.util.Arrays.asList(new Class<?>[]{" + blacklist + "});");
        System.out.println("  for (Class<?> exception : exceptions) {");
        System.out.printf("    Assert.assertFalse(\"The specification requires method %1$s(%2$s) in class %3$s to handle and not throw \" + exception.getName() + \".\", outlawedExceptions.contains(exception));%n", m.getName(), readableList, clazz.getCanonicalName());
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
        System.out.printf("    ctor = Class.forName(\"%1$s\").getDeclaredConstructor(new Class<?>[]{%2$s});%n", clazz.getName(), list);
        System.out.println("  } catch (NoSuchMethodException e) {");
        System.out.printf("    Assert.fail(\"You need a constructor in class %1$s taking %2$d argument%3$s", clazz.getCanonicalName(), types.length, (types.length == 1 ? "" : "s"));
        if (types.length > 0) {
          System.out.printf(", having type%1$s %2$s", types.length == 1 ? "" : "s", readableList);
        }
        System.out.println(".\");");
        System.out.println("  }");

        // Also verify return type and modifiers are correct.
        System.out.printf("  Assert.assertTrue(\"The modifiers for constructor %1$s(%2$s) are not correct. \" + SpecCheckUtilities.getModifierDifference(%3$d, ctor.getModifiers()), %3$d == ctor.getModifiers());%n", ctor.getName(), readableList, ctor.getModifiers());

        Class<?>[] exceptions = ctor.getAnnotation(Specified.class).mustThrow();
        System.out.println("  exceptions = java.util.Arrays.asList(ctor.getExceptionTypes());");
        for (Class<?> exception : exceptions) {
          System.out.printf("  Assert.assertTrue(\"The specification requires constructor %1$s(%2$s) to throw %3$s.\", exceptions.contains(%3$s.class));%n", ctor.getName(), readableList, exception.getCanonicalName());
        }

        exceptions = ctor.getAnnotation(Specified.class).mustNotThrow();
        String blacklist = "";
        for (Class<?> exception : exceptions) {
          blacklist += exception.getCanonicalName() + ".class, ";
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
        System.out.printf("    field = Class.forName(\"%1$s\").getDeclaredField(\"%2$s\");%n", clazz.getName(), f.getName());
        System.out.println("  } catch (NoSuchFieldException e) {");
        System.out.printf("    Assert.fail(\"You need a field named %1$s in class %2$s.\");%n", f.getName(), clazz.getCanonicalName());
        System.out.println("  }");

        System.out.printf("  Assert.assertEquals(\"Field %1$s.%2$s is of the wrong type.\", %3$s.class, field.getType());%n", clazz.getCanonicalName(), f.getName(), f.getType().getCanonicalName());

        // Also verify modifiers are correct.
        System.out.printf("  Assert.assertTrue(\"The modifiers for field %1$s in class %3$s are not correct. \" + SpecCheckUtilities.getModifierDifference(%2$d, field.getModifiers()), %2$d == field.getModifiers());%n", f.getName(), f.getModifiers(), clazz.getCanonicalName());
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
    Matcher m = p.matcher(clazz.getCanonicalName());
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, "");
      sb.append(m.group(2).toUpperCase()); // 2 is the first letter (\\w)
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * Get a comma-separated sequence of stringified class literals (i.e,
   * "String.class, boolean.class") corresponding to the given array of class
   * objects.
   * 
   * @param types
   * Ordered list of classes for which a comma-separated String is created.
   * 
   * @return The comma-separated list of classes.
   */
//  public static String getTypesList(Class<?>[] types) {
//    String list = "";
//    if (types.length > 0) {
//      list += types[0].getCanonicalName() + ".class";
//      for (int i = 1; i < types.length; ++i) {
//        list += ", " + types[i].getCanonicalName() + ".class";
//      }
//    }
//    return list;
//  }
}
