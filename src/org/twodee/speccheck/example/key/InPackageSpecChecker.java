package org.twodee.speccheck.example.key;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.twodee.speccheck.SpecCheck;
import org.twodee.speccheck.SpecCheckTest;
import org.twodee.speccheck.SpecCheckUtilities;

public class InPackageSpecChecker {
  public static void main(String[] args) {
    SpecCheck.testAsStudent(InPackageSpecChecker.class);
  }

  @SpecCheckTest(order=0)
  @Test
  public void testForClasses() throws Exception {
    try {
       Class.forName("org.twodee.speccheck.example.InPackage");
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackage could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackage could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    try {
       Class.forName("org.twodee.speccheck.example.key.InPackageTypeA");
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.key.InPackageTypeA could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.key.InPackageTypeA could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    try {
       Class.forName("org.twodee.speccheck.example.InPackageTypeB");
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackageTypeB could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackageTypeB could not be found. Check case, spelling, and that you created your class in the right package.");
    }
  }
  @SpecCheckTest(order=10)
  @Test
  public void testOrgTwodeeSpeccheckExampleInPackage() throws Exception {
    try {
       Class<?> cls = Class.forName("org.twodee.speccheck.example.InPackage");
       Assert.assertTrue("The modifiers for class org.twodee.speccheck.example.InPackage are not correct. " + SpecCheckUtilities.getModifierDifference(1, cls.getModifiers()), 1 == cls.getModifiers());
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackage could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackage could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    Class<?> cls = Class.forName("org.twodee.speccheck.example.InPackage");
    List<Class<?>> ifaces = java.util.Arrays.asList(cls.getInterfaces());
    Field field = null;
    LinkedList<Field> fields = new LinkedList<Field>();
    for (Field actual : Class.forName("org.twodee.speccheck.example.InPackage").getDeclaredFields()) {
       fields.add(actual);
    }
    for (Field actual : fields) {
      if (Modifier.isStatic(actual.getModifiers())) {
        Assert.assertTrue(String.format("Field org.twodee.speccheck.example.InPackage.%1$s is not in the specification. Any static fields you add should be private.", actual.getName()), Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      } else {
        Assert.assertTrue("Instance variables must be private (or possibly protected). org.twodee.speccheck.example.InPackage." + actual.getName() + " is not. The only public variables should be specified constants, which must be static and final.", Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      }
    }
    List<Class<?>> exceptions = null;
    List<Class<?>> outlawedExceptions = null;
    Constructor<?> ctor = null;
    LinkedList<Constructor<?>> ctors = new LinkedList<Constructor<?>>();
    for (Constructor<?> actual : Class.forName("org.twodee.speccheck.example.InPackage").getDeclaredConstructors()) {
       ctors.add(actual);
    }
    for (Constructor<?> actual : ctors) {
      if (Modifier.isPublic(actual.getModifiers()) && actual.getParameterTypes().length != 0) {
        Assert.fail(String.format("Constructor %1$s(%2$s) is not in the specification. Any constructors you add should be private (or possibly protected).", actual.getName(), SpecCheckUtilities.getTypesList(actual.getParameterTypes()).replaceAll(".class", "")));
      }
    }
    Method method = null;
    try {
      method = Class.forName("org.twodee.speccheck.example.InPackage").getDeclaredMethod("foo", new Class<?>[]{org.twodee.speccheck.example.key.InPackageTypeA.class, org.twodee.speccheck.example.InPackageTypeB.class});
    } catch (NoSuchMethodException e) {
      Assert.fail("You need a foo method in class org.twodee.speccheck.example.InPackage taking 2 arguments, having types org.twodee.speccheck.example.key.InPackageTypeA, org.twodee.speccheck.example.InPackageTypeB.");
    }
    Assert.assertEquals("Your method foo(org.twodee.speccheck.example.key.InPackageTypeA, org.twodee.speccheck.example.InPackageTypeB) in class org.twodee.speccheck.example.InPackage has the wrong return type.", void.class, method.getReturnType());
    Assert.assertTrue("The modifiers for method foo(org.twodee.speccheck.example.key.InPackageTypeA, org.twodee.speccheck.example.InPackageTypeB) in class org.twodee.speccheck.example.InPackage are not correct. " + SpecCheckUtilities.getModifierDifference(9, method.getModifiers()), 9 == method.getModifiers());
    exceptions = java.util.Arrays.asList(method.getExceptionTypes());
    outlawedExceptions = java.util.Arrays.asList(new Class<?>[]{});
    for (Class<?> exception : exceptions) {
      Assert.assertFalse("The specification requires method foo(org.twodee.speccheck.example.key.InPackageTypeA, org.twodee.speccheck.example.InPackageTypeB) in class org.twodee.speccheck.example.InPackage to handle and not throw " + exception.getName() + ".", outlawedExceptions.contains(exception));
    }
    LinkedList<Method> methods = new LinkedList<Method>();
    for (Method m : Class.forName("org.twodee.speccheck.example.InPackage").getDeclaredMethods()) {
       methods.add(m);
    }
    try {
      method = Class.forName("org.twodee.speccheck.example.InPackage").getDeclaredMethod("foo", new Class<?>[]{org.twodee.speccheck.example.key.InPackageTypeA.class, org.twodee.speccheck.example.InPackageTypeB.class});
      methods.remove(method);
    } catch (NoSuchMethodException e) {}
    for (Method m : methods) {
      if (!m.isBridge() && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isProtected(m.getModifiers()) && !m.getName().equals("main")) {
        Assert.fail(String.format("Method org.twodee.speccheck.example.InPackage.%1$s(%2$s) is not in the specification. Any methods you add should be private (or possibly protected).", m.getName(), SpecCheckUtilities.getTypesList(m.getParameterTypes()).replaceAll(".class", "")));
      }
    }
  }
  @SpecCheckTest(order=10)
  @Test
  public void testOrgTwodeeSpeccheckExampleKeyInPackageTypeA() throws Exception {
    try {
       Class<?> cls = Class.forName("org.twodee.speccheck.example.key.InPackageTypeA");
       Assert.assertTrue("The modifiers for class org.twodee.speccheck.example.key.InPackageTypeA are not correct. " + SpecCheckUtilities.getModifierDifference(1, cls.getModifiers()), 1 == cls.getModifiers());
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.key.InPackageTypeA could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.key.InPackageTypeA could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    Field field = null;
    List<Class<?>> exceptions = null;
    List<Class<?>> outlawedExceptions = null;
    Constructor<?> ctor = null;
    Method method = null;
  }
  @SpecCheckTest(order=10)
  @Test
  public void testOrgTwodeeSpeccheckExampleInPackageTypeB() throws Exception {
    try {
       Class<?> cls = Class.forName("org.twodee.speccheck.example.InPackageTypeB");
       Assert.assertTrue("The modifiers for class org.twodee.speccheck.example.InPackageTypeB are not correct. " + SpecCheckUtilities.getModifierDifference(1, cls.getModifiers()), 1 == cls.getModifiers());
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackageTypeB could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackageTypeB could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    try {
       Class<?> cls = Class.forName("org.twodee.speccheck.example.InPackageTypeB");
       Assert.assertEquals("The superclass of class org.twodee.speccheck.example.InPackageTypeB is not correct.", org.twodee.speccheck.example.key.InPackageTypeA.class, cls.getSuperclass());
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackageTypeB could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.InPackageTypeB could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    Class<?> cls = Class.forName("org.twodee.speccheck.example.InPackageTypeB");
    List<Class<?>> ifaces = java.util.Arrays.asList(cls.getInterfaces());
    Field field = null;
    LinkedList<Field> fields = new LinkedList<Field>();
    for (Field actual : Class.forName("org.twodee.speccheck.example.InPackageTypeB").getDeclaredFields()) {
       fields.add(actual);
    }
    for (Field actual : fields) {
      if (Modifier.isStatic(actual.getModifiers())) {
        Assert.assertTrue(String.format("Field org.twodee.speccheck.example.InPackageTypeB.%1$s is not in the specification. Any static fields you add should be private.", actual.getName()), Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      } else {
        Assert.assertTrue("Instance variables must be private (or possibly protected). org.twodee.speccheck.example.InPackageTypeB." + actual.getName() + " is not. The only public variables should be specified constants, which must be static and final.", Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      }
    }
    List<Class<?>> exceptions = null;
    List<Class<?>> outlawedExceptions = null;
    Constructor<?> ctor = null;
    LinkedList<Constructor<?>> ctors = new LinkedList<Constructor<?>>();
    for (Constructor<?> actual : Class.forName("org.twodee.speccheck.example.InPackageTypeB").getDeclaredConstructors()) {
       ctors.add(actual);
    }
    for (Constructor<?> actual : ctors) {
      if (Modifier.isPublic(actual.getModifiers()) && actual.getParameterTypes().length != 0) {
        Assert.fail(String.format("Constructor %1$s(%2$s) is not in the specification. Any constructors you add should be private (or possibly protected).", actual.getName(), SpecCheckUtilities.getTypesList(actual.getParameterTypes()).replaceAll(".class", "")));
      }
    }
    Method method = null;
    LinkedList<Method> methods = new LinkedList<Method>();
    for (Method m : Class.forName("org.twodee.speccheck.example.InPackageTypeB").getDeclaredMethods()) {
       methods.add(m);
    }
    for (Method m : methods) {
      if (!m.isBridge() && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isProtected(m.getModifiers()) && !m.getName().equals("main")) {
        Assert.fail(String.format("Method org.twodee.speccheck.example.InPackageTypeB.%1$s(%2$s) is not in the specification. Any methods you add should be private (or possibly protected).", m.getName(), SpecCheckUtilities.getTypesList(m.getParameterTypes()).replaceAll(".class", "")));
      }
    }
  }
}
