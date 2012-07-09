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

public class MissingConstructorSpecChecker {
  public static void main(String[] args) {
    SpecCheck.testAsStudent(MissingConstructorSpecChecker.class);
  }

  @SpecCheckTest(order=0)
  @Test
  public void testForClasses() throws Exception {
    try {
       Class.forName("org.twodee.speccheck.example.MissingConstructor");
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.MissingConstructor could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.MissingConstructor could not be found. Check case, spelling, and that you created your class in the right package.");
    }
  }
  @SpecCheckTest(order=10)
  @Test
  public void testOrgTwodeeSpeccheckExampleMissingConstructor() throws Exception {
    try {
       Class<?> cls = Class.forName("org.twodee.speccheck.example.MissingConstructor");
       Assert.assertTrue("The modifiers for class org.twodee.speccheck.example.MissingConstructor are not correct. " + SpecCheckUtilities.getModifierDifference(1, cls.getModifiers()), 1 == cls.getModifiers());
    } catch (ClassNotFoundException e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.MissingConstructor could not be found. Check case, spelling, and that you created your class in the right package.");
    } catch (NoClassDefFoundError e) {
       Assert.fail("A class by the name of org.twodee.speccheck.example.MissingConstructor could not be found. Check case, spelling, and that you created your class in the right package.");
    }
    Class<?> cls = Class.forName("org.twodee.speccheck.example.MissingConstructor");
    List<Class<?>> ifaces = java.util.Arrays.asList(cls.getInterfaces());
    Field field = null;
    LinkedList<Field> fields = new LinkedList<Field>();
    for (Field actual : Class.forName("org.twodee.speccheck.example.MissingConstructor").getDeclaredFields()) {
       fields.add(actual);
    }
    for (Field actual : fields) {
      if (Modifier.isStatic(actual.getModifiers())) {
        Assert.assertTrue(String.format("Field org.twodee.speccheck.example.MissingConstructor.%1$s is not in the specification. Any static fields you add should be private.", actual.getName()), Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      } else {
        Assert.assertTrue("Instance variables must be private (or possibly protected). org.twodee.speccheck.example.MissingConstructor." + actual.getName() + " is not. The only public variables should be specified constants, which must be static and final.", Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
      }
    }
    List<Class<?>> exceptions = null;
    List<Class<?>> outlawedExceptions = null;
    Constructor<?> ctor = null;
    try {
      ctor = Class.forName("org.twodee.speccheck.example.MissingConstructor").getDeclaredConstructor(new Class<?>[]{});
    } catch (NoSuchMethodException e) {
      Assert.fail("You need a constructor in class org.twodee.speccheck.example.MissingConstructor taking 0 arguments.");
    }
    Assert.assertTrue("The modifiers for constructor org.twodee.speccheck.example.key.MissingConstructor() are not correct. " + SpecCheckUtilities.getModifierDifference(1, ctor.getModifiers()), 1 == ctor.getModifiers());
    exceptions = java.util.Arrays.asList(ctor.getExceptionTypes());
    outlawedExceptions = java.util.Arrays.asList(new Class<?>[]{});
    for (Class<?> exception : exceptions) {
      Assert.assertFalse("The specification requires constructor org.twodee.speccheck.example.key.MissingConstructor() to handle and not throw " + exception.getName() + ".", outlawedExceptions.contains(exception));
    }
    LinkedList<Constructor<?>> ctors = new LinkedList<Constructor<?>>();
    for (Constructor<?> actual : Class.forName("org.twodee.speccheck.example.MissingConstructor").getDeclaredConstructors()) {
       ctors.add(actual);
    }
    try {
      ctor = Class.forName("org.twodee.speccheck.example.MissingConstructor").getDeclaredConstructor(new Class<?>[]{});
      ctors.remove(ctor);
    } catch (NoSuchMethodException e) {}
    for (Constructor<?> actual : ctors) {
      if (Modifier.isPublic(actual.getModifiers()) && actual.getParameterTypes().length != 0) {
        Assert.fail(String.format("Constructor %1$s(%2$s) is not in the specification. Any constructors you add should be private (or possibly protected).", actual.getName(), SpecCheckUtilities.getTypesList(actual.getParameterTypes()).replaceAll(".class", "")));
      }
    }
    Method method = null;
    LinkedList<Method> methods = new LinkedList<Method>();
    for (Method m : Class.forName("org.twodee.speccheck.example.MissingConstructor").getDeclaredMethods()) {
       methods.add(m);
    }
    for (Method m : methods) {
      if (!m.isBridge() && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isProtected(m.getModifiers()) && !m.getName().equals("main")) {
        Assert.fail(String.format("Method org.twodee.speccheck.example.MissingConstructor.%1$s(%2$s) is not in the specification. Any methods you add should be private (or possibly protected).", m.getName(), SpecCheckUtilities.getTypesList(m.getParameterTypes()).replaceAll(".class", "")));
      }
    }
  }
}
