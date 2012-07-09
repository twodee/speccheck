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

public class BadReturnTypeSpecChecker {
  public static void main(String[] args) {
    SpecCheck.testAsStudent(BadReturnTypeSpecChecker.class);
  }
  @SpecCheckTest(order=0)
  @Test
  public void testForClasses() throws Exception {
    try {
       Class.forName("org.twodee.speccheck.example.BadReturnType");
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.BadReturnType could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.BadReturnType could not be found. Check case, spelling, and that you created your class in the right package.");
    }
  }
  @SpecCheckTest(order=10)
  @Test
  public void testOrgTwodeeSpeccheckExampleBadReturnType() throws Exception {
    try {
       Class<?> cls = Class.forName("org.twodee.speccheck.example.BadReturnType");
       Assert.assertTrue("The modifiers for class org.twodee.speccheck.example.BadReturnType are not correct. " + SpecCheckUtilities.getModifierDiff(1, cls.getModifiers()), 1 == cls.getModifiers());
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.BadReturnType could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.BadReturnType could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    Class<?> cls = Class.forName("org.twodee.speccheck.example.BadReturnType");
    List<Class<?>> ifaces = java.util.Arrays.asList(cls.getInterfaces());
    Field field = null;
    LinkedList<Field> fields = new LinkedList<Field>();
    for (Field actual : Class.forName("org.twodee.speccheck.example.BadReturnType").getDeclaredFields()) {
       fields.add(actual);
    }
    for (Field actual : fields) {
      if (Modifier.isStatic(actual.getModifiers())) {
        Assert.assertTrue(String.format("Field org.twodee.speccheck.example.BadReturnType.%1$s is not in the specification. Any static fields you add should be private.", actual.getName()), Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      } else {
        Assert.assertTrue("Instance variables must be private (or possibly protected). org.twodee.speccheck.example.BadReturnType." + actual.getName() + " is not. The only public variables should be specified constants, which must be static and final.", Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      }
    }
    List<Class<?>> exceptions = null;
    List<Class<?>> outlawedExceptions = null;
    Constructor<?> ctor = null;
    LinkedList<Constructor<?>> ctors = new LinkedList<Constructor<?>>();
    for (Constructor<?> actual : Class.forName("org.twodee.speccheck.example.BadReturnType").getDeclaredConstructors()) {
       ctors.add(actual);
    }
    for (Constructor<?> actual : ctors) {
      if (Modifier.isPublic(actual.getModifiers()) && actual.getParameterTypes().length != 0) {
        Assert.fail(String.format("Constructor %1$s(%2$s) is not in the specification. Any constructors you add should be private (or possibly protected).", actual.getName(), SpecCheckUtilities.getTypesList(actual.getParameterTypes()).replaceAll(".class", "")));
      }
    }
    Method method = null;
    try {
      method = Class.forName("org.twodee.speccheck.example.BadReturnType").getDeclaredMethod("foo", new Class<?>[]{});
    } catch (NoSuchMethodException e) {
      Assert.fail("You need a foo method in class org.twodee.speccheck.example.BadReturnType taking 0 arguments.");
    }
    Assert.assertEquals("Your method foo() in class org.twodee.speccheck.example.BadReturnType has the wrong return type.", java.lang.String.class, method.getReturnType());
    Assert.assertTrue("The modifiers for method foo() in class org.twodee.speccheck.example.BadReturnType are not correct. " + SpecCheckUtilities.getModifierDiff(9, method.getModifiers()), 9 == method.getModifiers());
    exceptions = java.util.Arrays.asList(method.getExceptionTypes());
    outlawedExceptions = java.util.Arrays.asList(new Class<?>[]{});
    for (Class<?> exception : exceptions) {
      Assert.assertFalse("The specification requires method foo() in class org.twodee.speccheck.example.BadReturnType to handle and not throw " + exception.getName() + ".", outlawedExceptions.contains(exception));
    }
    LinkedList<Method> methods = new LinkedList<Method>();
    for (Method m : Class.forName("org.twodee.speccheck.example.BadReturnType").getDeclaredMethods()) {
       methods.add(m);
    }
    try {
      method = Class.forName("org.twodee.speccheck.example.BadReturnType").getDeclaredMethod("foo", new Class<?>[]{});
      methods.remove(method);
    } catch (NoSuchMethodException e) {}
    for (Method m : methods) {
      if (!m.isBridge() && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isProtected(m.getModifiers()) && !m.getName().equals("main")) {
        Assert.fail(String.format("Method org.twodee.speccheck.example.BadReturnType.%1$s(%2$s) is not in the specification. Any methods you add should be private (or possibly protected).", m.getName(), SpecCheckUtilities.getTypesList(m.getParameterTypes()).replaceAll(".class", "")));
      }
    }
  }
}
