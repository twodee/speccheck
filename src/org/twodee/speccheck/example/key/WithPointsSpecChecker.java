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

public class WithPointsSpecChecker {
  public static void main(String[] args) {
    SpecCheck.testAsStudent(WithPointsSpecChecker.class);
  }
  
  @SpecCheckTest(nPoints = 10)
  @Test
  public void testGetFive() {
    Assert.assertEquals("getFive doesn't return 5", 5, new WithPoints("foo").getFive());
  }

  @SpecCheckTest(order=0)
  @Test
  public void testForClasses() throws Exception {
    try {
       Class.forName("org.twodee.speccheck.example.WithPoints");
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.WithPoints could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.WithPoints could not be found. Check case, spelling, and that you created your class in the right package.");
    }
  }
  @SpecCheckTest(order=10)
  @Test
  public void testOrgTwodeeSpeccheckExampleWithPoints() throws Exception {
    try {
       Class<?> cls = Class.forName("org.twodee.speccheck.example.WithPoints");
       Assert.assertTrue("The modifiers for class org.twodee.speccheck.example.WithPoints are not correct. " + SpecCheckUtilities.getModifierDifference(1, cls.getModifiers()), 1 == cls.getModifiers());
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.WithPoints could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.WithPoints could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    Class<?> cls = Class.forName("org.twodee.speccheck.example.WithPoints");
    List<Class<?>> ifaces = java.util.Arrays.asList(cls.getInterfaces());
    Field field = null;
    LinkedList<Field> fields = new LinkedList<Field>();
    for (Field actual : Class.forName("org.twodee.speccheck.example.WithPoints").getDeclaredFields()) {
       fields.add(actual);
    }
    for (Field actual : fields) {
      if (Modifier.isStatic(actual.getModifiers())) {
        Assert.assertTrue(String.format("Field org.twodee.speccheck.example.WithPoints.%1$s is not in the specification. Any static fields you add should be private.", actual.getName()), Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      } else {
        Assert.assertTrue("Instance variables must be private (or possibly protected). org.twodee.speccheck.example.WithPoints." + actual.getName() + " is not. The only public variables should be specified constants, which must be static and final.", Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      }
    }
    List<Class<?>> exceptions = null;
    List<Class<?>> outlawedExceptions = null;
    Constructor<?> ctor = null;
    try {
      ctor = Class.forName("org.twodee.speccheck.example.WithPoints").getDeclaredConstructor(new Class<?>[]{java.lang.String.class});
    } catch (NoSuchMethodException e) {
      Assert.fail("You need a constructor in class org.twodee.speccheck.example.WithPoints taking 1 argument, having type java.lang.String.");
    }
    Assert.assertTrue("The modifiers for constructor org.twodee.speccheck.example.key.WithPoints(java.lang.String) are not correct. " + SpecCheckUtilities.getModifierDifference(1, ctor.getModifiers()), 1 == ctor.getModifiers());
    exceptions = java.util.Arrays.asList(ctor.getExceptionTypes());
    outlawedExceptions = java.util.Arrays.asList(new Class<?>[]{});
    for (Class<?> exception : exceptions) {
      Assert.assertFalse("The specification requires constructor org.twodee.speccheck.example.key.WithPoints(java.lang.String) to handle and not throw " + exception.getName() + ".", outlawedExceptions.contains(exception));
    }
    LinkedList<Constructor<?>> ctors = new LinkedList<Constructor<?>>();
    for (Constructor<?> actual : Class.forName("org.twodee.speccheck.example.WithPoints").getDeclaredConstructors()) {
       ctors.add(actual);
    }
    try {
      ctor = Class.forName("org.twodee.speccheck.example.WithPoints").getDeclaredConstructor(new Class<?>[]{java.lang.String.class});
      ctors.remove(ctor);
    } catch (NoSuchMethodException e) {}
    for (Constructor<?> actual : ctors) {
      if (Modifier.isPublic(actual.getModifiers()) && actual.getParameterTypes().length != 0) {
        Assert.fail(String.format("Constructor %1$s(%2$s) is not in the specification. Any constructors you add should be private (or possibly protected).", actual.getName(), SpecCheckUtilities.getTypesList(actual.getParameterTypes()).replaceAll(".class", "")));
      }
    }
    Method method = null;
    try {
      method = Class.forName("org.twodee.speccheck.example.WithPoints").getDeclaredMethod("getFive", new Class<?>[]{});
    } catch (NoSuchMethodException e) {
      Assert.fail("You need a getFive method in class org.twodee.speccheck.example.WithPoints taking 0 arguments.");
    }
    Assert.assertEquals("Your method getFive() in class org.twodee.speccheck.example.WithPoints has the wrong return type.", int.class, method.getReturnType());
    Assert.assertTrue("The modifiers for method getFive() in class org.twodee.speccheck.example.WithPoints are not correct. " + SpecCheckUtilities.getModifierDifference(1, method.getModifiers()), 1 == method.getModifiers());
    exceptions = java.util.Arrays.asList(method.getExceptionTypes());
    outlawedExceptions = java.util.Arrays.asList(new Class<?>[]{});
    for (Class<?> exception : exceptions) {
      Assert.assertFalse("The specification requires method getFive() in class org.twodee.speccheck.example.WithPoints to handle and not throw " + exception.getName() + ".", outlawedExceptions.contains(exception));
    }
    LinkedList<Method> methods = new LinkedList<Method>();
    for (Method m : Class.forName("org.twodee.speccheck.example.WithPoints").getDeclaredMethods()) {
       methods.add(m);
    }
    try {
      method = Class.forName("org.twodee.speccheck.example.WithPoints").getDeclaredMethod("getFive", new Class<?>[]{});
      methods.remove(method);
    } catch (NoSuchMethodException e) {}
    for (Method m : methods) {
      if (!m.isBridge() && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isProtected(m.getModifiers()) && !m.getName().equals("main")) {
        Assert.fail(String.format("Method org.twodee.speccheck.example.WithPoints.%1$s(%2$s) is not in the specification. Any methods you add should be private (or possibly protected).", m.getName(), SpecCheckUtilities.getTypesList(m.getParameterTypes()).replaceAll(".class", "")));
      }
    }
  }
}
