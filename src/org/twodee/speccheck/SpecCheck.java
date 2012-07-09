package org.twodee.speccheck;

import java.io.OutputStream;
import java.io.PrintStream;
import org.junit.ComparisonFailure;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * A utility class for running JUnit tests and reporting any failures.
 * 
 * @author cjohnson
 */
public class SpecCheck {
  /**
   * Uninstantiable.
   */
  private SpecCheck() {
  }
  

  /**
   * Issue tests for each of the specified classes.
   * 
   * @param classNames
   * Names of classes for which to issue tests.
   * 
   * @throws ClassNotFoundException
   */
  public static void generate(Class<?>... clazzes) throws ClassNotFoundException {
    SpecCheckGenerator.generateClassExistenceTest(clazzes);
    for (Class<?> clazz : clazzes) {
      SpecCheckGenerator.generateClassTest(clazz);
    }
  }

  /**
   * Run the JUnit tests in the specified SpecChecker class, customizing output
   * for grading.
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
   * @param verbose
   * Whether or not to be wordy in diagnostic messages.
   * @return Message indicating how many tests passed.
   */
  private static String run(Class<?> tester,
                            boolean verbose) {
    try {
      return evaluateTests(tester, verbose);
    } catch (Error e) {
      return "Tests couldn't be run. Did you add JUnit to your project?";
    }
  }

  /**
   * Run each test in the specified class and print the results.
   * 
   * @param tester
   * The class containing the JUnit tests.
   * 
   * @return A message indicating how many tests pass.
   */
  public static String evaluateTests(Class<?> tester,
                                     boolean verbose) {
    JUnitCore core = new JUnitCore();
    Request request = Request.aClass(tester);
    request = request.sortWith(new SpecCheckTestComparator());
    SpecCheckRunListener listener = new SpecCheckRunListener();
    core.addListener(listener);

    PrintStream out = null;
    if (!verbose) {
      out = System.out;
      System.setOut(new PrintStream(new OutputStream() {
        public void write(int b) {
        }
      }));
    }
    Result result = core.run(request);

    if (!verbose) {
      System.setOut(out);
    }

    int nTests = result.getRunCount();
    int nFailed = result.getFailureCount();
    final String wrapPattern = "(.{50,}?) ";

    String scoreMessage = String.format("%d out of %d tests pass.", nTests - nFailed, nTests);
    if (listener.getScorePossible() > 0) {
      scoreMessage = String.format("You received %d/%d points. %s", listener.getScore(), listener.getScorePossible(), scoreMessage);
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

    if (listener.getScorePossible() == 0) {
      return scoreMessage;
    } else {
      return "TOTAL: " + listener.getScore() + "/" + listener.getScorePossible();
    }
  }
}
