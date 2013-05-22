package org.twodee.speccheck.example.key;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TooManyInstanceVariablesSpecChecker {
  public static void main(String[] args) {
    SpecCheck.testAsStudent(TestSuite.class);
  }

  public static class TestSuite {
    @SpecCheckTest(order=0)
@Test
public void testForClasses() throws Exception {
  try {
     Class.forName("org.twodee.speccheck.example.key.TooManyInstanceVariables");
  } catch (ClassNotFoundException e) {
     Assert.fail("A class by the name of org.twodee.speccheck.example.key.TooManyInstanceVariables could not be found. Check case, spelling, and that you created your class in the right package.");
  } catch (NoClassDefFoundError e) {
     Assert.fail("A class by the name of org.twodee.speccheck.example.key.TooManyInstanceVariables could not be found. Check case, spelling, and that you created your class in the right package.");
  }
}
@SpecCheckTest(order=10)
@Test
public void testOrgTwodeeSpeccheckExampleKeyTooManyInstanceVariables() throws Exception {
  try {
     Class<?> cls = Class.forName("org.twodee.speccheck.example.key.TooManyInstanceVariables");
     Assert.assertTrue("The modifiers for class org.twodee.speccheck.example.key.TooManyInstanceVariables are not correct. " + SpecCheckUtilities.getModifierDifference(1, cls.getModifiers()), 1 == cls.getModifiers());
  } catch (ClassNotFoundException e) {
     Assert.fail("A class by the name of org.twodee.speccheck.example.key.TooManyInstanceVariables could not be found. Check case, spelling, and that you created your class in the right package.");
  } catch (NoClassDefFoundError e) {
     Assert.fail("A class by the name of org.twodee.speccheck.example.key.TooManyInstanceVariables could not be found. Check case, spelling, and that you created your class in the right package.");
  }
  Class<?> cls = Class.forName("org.twodee.speccheck.example.key.TooManyInstanceVariables");
  List<Class<?>> ifaces = java.util.Arrays.asList(cls.getInterfaces());
  Field field = null;
  LinkedList<Field> fields = new LinkedList<Field>();
  for (Field actual : Class.forName("org.twodee.speccheck.example.key.TooManyInstanceVariables").getDeclaredFields()) {
     fields.add(actual);
  }
  for (Field actual : fields) {
    if (Modifier.isStatic(actual.getModifiers())) {
      Assert.assertTrue(String.format("Field org.twodee.speccheck.example.key.TooManyInstanceVariables.%1$s is not in the specification. Any static fields you add should be private.", actual.getName()), Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
    } else {
      Assert.assertTrue("Instance variables must be private (or possibly protected). org.twodee.speccheck.example.key.TooManyInstanceVariables." + actual.getName() + " is not. The only public variables should be specified constants, which must be static and final.", Modifier.isPrivate(actual.getModifiers()) || Modifier.isProtected(actual.getModifiers()));
    }
  }
  List<Class<?>> exceptions = null;
  List<Class<?>> outlawedExceptions = null;
  Constructor<?> ctor = null;
  LinkedList<Constructor<?>> ctors = new LinkedList<Constructor<?>>();
  for (Constructor<?> actual : Class.forName("org.twodee.speccheck.example.key.TooManyInstanceVariables").getDeclaredConstructors()) {
     ctors.add(actual);
  }
  for (Constructor<?> actual : ctors) {
    if (Modifier.isPublic(actual.getModifiers()) && actual.getParameterTypes().length != 0) {
      Assert.fail(String.format("Constructor %1$s(%2$s) is not in the specification. Any constructors you add should be private (or possibly protected).", actual.getName(), SpecCheckUtilities.getTypesList(actual.getParameterTypes()).replaceAll(".class", "")));
    }
  }
  Method method = null;
  LinkedList<Method> methods = new LinkedList<Method>();
  for (Method m : Class.forName("org.twodee.speccheck.example.key.TooManyInstanceVariables").getDeclaredMethods()) {
     methods.add(m);
  }
  for (Method m : methods) {
    if (!m.isBridge() && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isProtected(m.getModifiers()) && !m.getName().equals("main")) {
      Assert.fail(String.format("Method org.twodee.speccheck.example.key.TooManyInstanceVariables.%1$s(%2$s) is not in the specification. Any methods you add should be private (or possibly protected).", m.getName(), SpecCheckUtilities.getTypesList(m.getParameterTypes()).replaceAll(".class", "")));
    }
  }
}
@SpecCheckTest(order=5)
@Test
public void testOrgTwodeeSpeccheckExampleKeyTooManyInstanceVariablesFieldCount() throws Exception {
  Field[] fields = null;
  try {
     fields = Class.forName("org.twodee.speccheck.example.key.TooManyInstanceVariables").getDeclaredFields();
  } catch (ClassNotFoundException e) {
     Assert.fail("A class by the name of org.twodee.speccheck.example.key.TooManyInstanceVariables could not be found. Check case, spelling, and that you created your class in the right package.");
  } catch (NoClassDefFoundError e) {
     Assert.fail("A class by the name of org.twodee.speccheck.example.key.TooManyInstanceVariables could not be found. Check case, spelling, and that you created your class in the right package.");
  }
  int nInstanceVars = 0;
  for (Field f : fields) {
     if (!Modifier.isStatic(f.getModifiers())) {
       ++nInstanceVars;
     }
  }
  Assert.assertTrue("You have a lot of instance variables in class org.twodee.speccheck.example.key.TooManyInstanceVariables. Perhaps some of them should be local variables?", nInstanceVars <= 3);
}

  }

  /*
   * SpecCheck - a system for automatically generating tests for
   * interface-conformance Copyright (C) 2012 Chris Johnson
   * 
   * This program is free software: you can redistribute it and/or modify it
   * under the terms of the GNU General Public License as published by the Free
   * Software Foundation, either version 3 of the License, or (at your option)
   * any later version.
   * 
   * This program is distributed in the hope that it will be useful, but WITHOUT
   * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
   * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
   * more details.
   * 
   * You should have received a copy of the GNU General Public License along
   * with this program. If not, see <http://www.gnu.org/licenses/>.
   */

  /**
   * A utility class for running JUnit tests and reporting any failures.
   * 
   * @author cjohnson
   */
  static class SpecCheck {
    /**
     * Uninstantiable.
     */
    private SpecCheck() {
    }

    /**
     * Run the JUnit tests in the specified SpecChecker class, customizing
     * output for grading.
     * 
     * @param tester
     * SpecChecker class, assumed to contain at least one JUnit test.
     * @return A message containing the results of running the tests.
     */
    public static String testAsGrader(Class<?> tester) {
      return run(tester, false);
    }

    /**
     * Run the JUnit tests contained in the specified class, customizing output
     * for the student working on an assignment.
     * 
     * @param tester
     * Class containing JUnit tests.
     * @return Message indicating how many tests passed.
     */
    public static String testAsStudent(Class<?> tester) {
      return run(tester, true);
    }

    /**
     * Run the JUnit tests contained in the specified class, customizing output
     * according to the arguments.
     * 
     * @param tester
     * Class containing JUnit tests.
     * @param isVerbose
     * Whether or not to be wordy in diagnostic messages.
     * @return Message indicating how many tests passed.
     */
    private static String run(Class<?> tester,
                              boolean isVerbose) {
      try {
        return evaluateTests(tester, isVerbose);
      } catch (Error e) {
        return "Tests couldn't be run. Did you add JUnit to your project?";
      }
    }

    /**
     * Run each @Test in the specified tester class and print the results.
     * 
     * @param tester
     * The class containing the JUnit tests.
     * 
     * @return A message indicating a score and how many tests pass.
     */
    public static String evaluateTests(Class<?> tester,
                                       boolean isVerbose) {
      JUnitCore core = new JUnitCore();
      Request request = Request.aClass(tester);
      request = request.sortWith(new SpecCheckTestComparator());
      SpecCheckRunListener listener = new SpecCheckRunListener(isVerbose);
      core.addListener(listener);
      core.run(request);

      return listener.getScoreMessage();
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface SpecCheckTest {
    /**
     * The number of points this test contributes to the student's final score
     * if this test passes.
     */
    int nPoints() default 0;

    /**
     * An indicator of priority for this test. A lesser-numbered test runs
     * before a greater-numbered test. Pure interface tests should be given a
     * low number, like 0, so that they are executed first.
     * 
     * @return
     */
    int order() default 20;
  }

  /**
   * This class contains a number of helper methods for ripping apart JUnit data
   * types into usable pieces.
   * 
   * @author cjohnson
   */
  static class SpecCheckUtilities {
    /**
     * Gets the method identified by the given Description.
     * 
     * @param d
     * The description given by JUnit.
     * @return The method
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public static Method getMethod(Description d) throws SecurityException, NoSuchMethodException, ClassNotFoundException {
      return Class.forName(getClassName(d)).getDeclaredMethod(getMethodName(d));
    }

    /**
     * Gets the name of the class identified by the given Description.
     * 
     * @param d
     * The description given by JUnit.
     * @return The class name
     */
    public static String getClassName(Description d) {
      Matcher matcher = parseDescription(d);
      return matcher.matches() ? matcher.group(2) : d.toString();
    }

    /**
     * Gets the name of the method identified by the given Description.
     * 
     * @param d
     * The description given by JUnit.
     * @return The method name
     */
    public static String getMethodName(Description d) {
      Matcher matcher = parseDescription(d);
      if (matcher.matches()) {
        return matcher.group(1);
      } else {
        return null;
      }
    }

    /**
     * Parses the JUnit Description and holds the results in a Matcher object.
     * 
     * @param d
     * The description given by JUnit
     * @return A Matcher holding the method and class names.
     */
    private static Matcher parseDescription(Description d) {
      // The description has the form methodName(className).
      return Pattern.compile("(.*)\\((.*)\\)").matcher(d.toString());
    }

    /**
     * Gets a description of how the modifiers differ from what was expected/
     * 
     * @param expected
     * The expected modifiers
     * @param actual
     * The actual modifiers observed
     * @return A short description how actual differs from expected
     */
    public static String getModifierDifference(int expected,
                                               int actual) {
      String msg = "";

      if (Modifier.isStatic(expected) != Modifier.isStatic(actual)) {
        msg += "It should " + (Modifier.isStatic(actual) ? "not " : "") + "be static. ";
      }

      if (Modifier.isPublic(expected) != Modifier.isPublic(actual)) {
        msg += "It should " + (Modifier.isPublic(actual) ? "not " : "") + "be public. ";
      }

      if (Modifier.isProtected(expected) != Modifier.isProtected(actual)) {
        msg += "It should " + (Modifier.isProtected(actual) ? "not " : "") + "be protected. ";
      }

      if (Modifier.isPrivate(expected) != Modifier.isPrivate(actual)) {
        msg += "It should " + (Modifier.isPrivate(actual) ? "not " : "") + "be private. ";
      }

      if (Modifier.isFinal(expected) != Modifier.isFinal(actual)) {
        msg += "It should " + (Modifier.isFinal(actual) ? "not " : "") + "be final. ";
      }

      // For interfaces, we don't care if they are marked abstract. TODO: is
      // there
      // a better way to express this?
      if (Modifier.isInterface(expected) != Modifier.isInterface(actual)) {
        msg += "It should " + (Modifier.isInterface(actual) ? "not " : "") + "be an interface. ";
      } else if (Modifier.isAbstract(expected) != Modifier.isAbstract(actual)) {
        msg += "It should " + (Modifier.isAbstract(actual) ? "not " : "") + "be abstract. ";
      }

      return msg;
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
    public static String getTypesList(Class<?>[] types) {
      String list = "";
      if (types.length > 0) {
        list += types[0].getName() + ".class";
        for (int i = 1; i < types.length; ++i) {
          list += ", " + types[i].getName() + ".class";
        }
      }
      return list;
    }
  }

  /**
   * A custom JUnit RunListener. We offer our own so that we can optionally add
   * points to a student's score when tests pass.
   * 
   * @author cjohnson
   */
  static class SpecCheckRunListener extends RunListener {
    /** A map from a test to points earned for it */
    private HashMap<Description, Integer> testToPoints;

    /** The total number of points possible in this test suite */
    private int nPointsPossible;

    private boolean isVerbose;

    private PrintStream oldOut;

    private String scoreMessage;

    public SpecCheckRunListener(boolean isVerbose) {
      this.isVerbose = isVerbose;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
      super.testRunStarted(description);
      testToPoints = new HashMap<Description, Integer>();
      nPointsPossible = 0;
      scoreMessage = null;
      oldOut = null;

      if (!isVerbose) {
        oldOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
          public void write(int b) {
          }
        }));
      }
    }

    /**
     * Triggered when a new test has been started. If tagged with
     * @SpecCheckTest, the test's points are added to the total.
     */
    @Override
    public void testStarted(Description description) {
      try {
        Method m = SpecCheckUtilities.getMethod(description);
        SpecCheckTest anno = m.getAnnotation(SpecCheckTest.class);
        if (anno != null) {
          // I put the test/points in the map here, but it may be taken out
          // later
          // if the test ends up failing.
          testToPoints.put(description, anno.nPoints());
          nPointsPossible += anno.nPoints();
        }
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

    /**
     * Triggered when a test fails.
     */
    @Override
    public void testFailure(Failure failure) {
      // Earlier we blindly added the test to the points map. Take it out now
      // because those points weren't earned.
      testToPoints.remove(failure.getDescription());
    }

    /**
     * Gets the number of points earned. This number is only meaningful after
     * the tests have been run.
     * 
     * @return Number of points earned.
     */
    private int getScore() {
      int score = 0;

      for (Integer nPoints : testToPoints.values()) {
        score += nPoints;
      }

      return score;
    }

    /**
     * Gets the number of points possible. This number is only meaningful after
     * the tests have been run.
     * 
     * @return Number of points possible.
     */
    private int getScorePossible() {
      return nPointsPossible;
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
      super.testRunFinished(result);

      if (!isVerbose) {
        System.setOut(oldOut);
      }

      int nTests = result.getRunCount();
      int nFailed = result.getFailureCount();
      final String wrapPattern = "(.{50,}?) ";

      scoreMessage = String.format("%d out of %d tests pass.", nTests - nFailed, nTests);
      if (getScorePossible() > 0) {
        scoreMessage = String.format("You received %d/%d points. %s", getScore(), getScorePossible(), scoreMessage);
      }
      System.out.printf("%s%n%n", scoreMessage);

      if (nFailed > 0) {
        for (Failure f : result.getFailures()) {
          System.out.println("PROBLEM: ");
          if (!f.getException().getClass().equals(AssertionError.class) &&
              !f.getException().getClass().equals(ComparisonFailure.class) &&
              !f.getException().getClass().equals(ArrayComparisonFailure.class)) {
            System.out.println("Your code threw an exception, making it impossible to test. You'll have to sleuth out what caused it. Start by finding the line in *your* code where it was first thrown. Look in the following listing for the first reference to your code.".replaceAll(wrapPattern, "$1\n"));
            System.out.println(f.getException().getClass());
            f.getException().printStackTrace(System.out);
          } else {
            System.out.printf("%s%n", f.getException().getLocalizedMessage().replaceAll(wrapPattern, "$1\n"));
          }
          System.out.printf("%n");
        }

        System.out.println("If you do not fix these problems, you are deviating from the homework specification and may lose points.".replaceAll(wrapPattern, "$1\n"));
      }

      if (getScorePossible() != 0) {
        scoreMessage = "TOTAL: " + getScore() + "/" + getScorePossible();
      }
    }

    public String getScoreMessage() {
      return scoreMessage;
    }
  }

  /**
   * We want pure interface tests to run before any optional functional tests.
   * To support this, @SpecCheckTest exposes an order parameter. Tests with a
   * lesser order are run before tests with a greater order. This comparator can
   * be used to order two tests. This class is unlikely to be used directly by
   * the instructor.
   * 
   * @author cjohnson
   */
  static class SpecCheckTestComparator implements Comparator<Description> {
    /**
     * Order two SpecCheckTests based on their order parameter.
     * 
     * @param a
     * One test.
     * @param b
     * The other test.
     * @return A negative value if the order of a is less than the order of b, 0
     * if they are the same, and a positive value otherwise.
     */
    @Override
    public int compare(Description a,
                       Description b) {
      Method m1 = null;
      Method m2 = null;

      try {
        m1 = SpecCheckUtilities.getMethod(a);
        m2 = SpecCheckUtilities.getMethod(b);
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }

      SpecCheckTest a1 = m1.getAnnotation(SpecCheckTest.class);
      SpecCheckTest a2 = m2.getAnnotation(SpecCheckTest.class);

      if (a1 == null || a2 == null || a1.order() == a2.order()) {
        return 0;
      } else if (a1.order() < a2.order()) {
        return -1;
      } else {
        return 1;
      }
    }
  }
}

