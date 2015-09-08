package org.twodee.speccheck;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class SpecChecker {
  private static final int WRAP_COLUMNS = 65;
  private static final String tag = "hw";
  private static final String[] filesToZip = {};

  public static void run(String[] args) {
    boolean isGrading = false;
    boolean hasLaterWeek = false;

    for (int i = 0; i < args.length; ++i) {
      if (args[i].equals("-g")) {
        isGrading = true;
      } else if (args[i].equals("-l")) {
        hasLaterWeek = true;
      }
    }

    try {
      int status = test(isGrading, hasLaterWeek);
      System.exit(status);
    } catch (Error e) {
      System.out.println(e);
      System.out.println("Tests couldn't be run. Did you add JUnit to your project?");
      System.exit(20);
    }
  }

  /**
   * Run the JUnit tests contained in the specified class, customizing output
   * for the student working on an assignment.
   * 
   * @param tester
   * Class containing JUnit tests.
   */
  public static int test(boolean isGrading) {
    return test(isGrading, true);
  }

  public static int test(boolean isGrading,
                         boolean hasLaterWeek) {
    try {
      SpecCheckTestResults results = runTestSuite(isGrading);
      int status = report(results, isGrading, hasLaterWeek);

      if (!isGrading && (!results.hasSpecCheckTests() ||
          results.isSpecCompliant()) && filesToZip.length > 0) {
        SpecCheckZipper.zip(results.isPerfect(), tag, filesToZip);
      }

      return status;
    } catch (Error e) {
      System.out.println("Tests couldn't be run. I saw this ugly exception instead:");
      System.out.println();
      System.out.println(e);
      System.out.println();
      System.out.println("Did you add JUnit to your project?");
      return 20;
    }
  }

  public static int report(SpecCheckTestResults results,
                           boolean isGrading,
                           boolean hasLaterWeek) {
    String scoreMessage = String.format("%d out of %d tests pass.", results.getPassedCount(), getTestsCount(isGrading));
    if (results.getScorePossible() > 0) {
      scoreMessage = String.format("You received %d/%d points. %s", results.getScore(), results.getScorePossible(), scoreMessage);
    }

    if (!isGrading) {
      System.out.printf("%s%n%n", scoreMessage);
    }

    if (results.getFailedCount() > 0) {
      for (Entry<Description, SpecCheckTestResult> pair : results.getTests()) {
        Failure f = pair.getValue().getFailure();
        if (f != null) {
          System.out.println("PROBLEM: ");
          if (!f.getException().getClass().equals(AssertionError.class) &&
              !f.getException().getClass().equals(ComparisonFailure.class) &&
              !f.getException().getClass().equals(ArrayComparisonFailure.class)) {
            System.out.println(StringUtilities.wrap("Your code threw an exception, making it impossible to test. You'll have to sleuth out what caused it. Start by finding the line in your code where it was first thrown. Look in the following listing for the first reference to your code.", WRAP_COLUMNS));
            System.out.println(f.getException().getClass());
            f.getException().printStackTrace(System.out);
          } else {
            System.out.printf("%s%n", StringUtilities.wrap(f.getException().getLocalizedMessage(), WRAP_COLUMNS));
          }
          System.out.printf("%n");
        }
      }

      System.out.println(StringUtilities.wrap("If you do not fix these problems, you are deviating from the homework specification and may not receive credit for your work.", WRAP_COLUMNS));
      System.out.println();
    }

    if (results.getScorePossible() != 0) {
      scoreMessage = "TOTAL: " + results.getScore() + "/" + results.getScorePossible();
    }

    if (results.isPerfect()) {
      System.out.println(StringUtilities.wrap("High five. You have passed all tests. Now commit and push before the deadline.", WRAP_COLUMNS));
      return 0;
    } else if (!hasLaterWeek) {
      System.out.println(StringUtilities.wrap("You've not passed all tests. But you will! Keep at it.", WRAP_COLUMNS));
      return 20;
    } else if (results.hasSpecCheckTests() && results.isSpecCompliant()) {
      System.out.printf(StringUtilities.wrap("You've not passed all tests. However, you've passed enough tests to qualify for later-week submission. Now commit and push before the deadline.%n", WRAP_COLUMNS));
      return 10;
    } else {
      System.out.printf(StringUtilities.wrap("You have not passed enough tests to qualify for later-week submission.%n", WRAP_COLUMNS));
      return 20;
    }
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
  private static SpecCheckTestResults runTestSuite(boolean isGrading) {
    try {
      return evaluateTests(isGrading);
    } catch (NoClassDefFoundError e) {
      System.out.printf("A class by the name of %1$s could not be found. Check case, spelling, and that you created your class in the right package.", e.getMessage());
      return new SpecCheckTestResults();
    } catch (Error e) {
      System.out.println("Tests couldn't be run. Did you add JUnit to your project?");
      return new SpecCheckTestResults();
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
  public static SpecCheckTestResults evaluateTests(boolean isGrading) {
    SpecCheckRunListener listener = new SpecCheckRunListener();
    SpecCheckTestResults results = listener.getResults();

    JUnitCore core = new JUnitCore();
    core.addListener(listener);

    core.run(getViableTests(SpecCheckTestSuite.SpecCheckPreTests.class, isGrading));
    if (results.isPerfect()) {
      core.run(getViableTests(SpecCheckTestSuite.SpecCheckInterfaceTests.class, isGrading));
      if (results.isPerfect()) {
        core.run(getViableTests(SpecCheckTestSuite.SpecCheckUnitTests.class, isGrading));
      }
    }

    return results;
  }
  
  private static Request getViableTests(Class<?> clazz, final boolean isGrading) {
    Request request = Request.aClass(clazz);
    if (isGrading) {
      request = request.filterWith(new Filter() {
        @Override
        public boolean shouldRun(Description description) {
          try {
            Method testMethod = SpecCheckUtilities.getMethod(description);
            SpecCheckTest anno = testMethod.getAnnotation(SpecCheckTest.class);
            return anno == null || !isGrading || anno.runWhenGrading();
          } catch (Exception e) {
            e.printStackTrace();
          }
          return true;
        }

        @Override
        public String describe() {
          return null;
        }
      });
    }
    return request.sortWith(new SpecCheckTestComparator());
  }
  
  private static int getTestsCount(boolean isGrading) {
    return getTestsCount(SpecCheckTestSuite.SpecCheckPreTests.class, isGrading) +
           getTestsCount(SpecCheckTestSuite.SpecCheckInterfaceTests.class, isGrading) +
           getTestsCount(SpecCheckTestSuite.SpecCheckUnitTests.class, isGrading);
  }
  
  private static int getTestsCount(Class<?> clazz, boolean isGrading) {
    Method[] methods = clazz.getMethods();
    int nTests = 0;
    for (Method method : methods) {
      Test testAnnotation = method.getAnnotation(Test.class);
      if (testAnnotation != null) {
        SpecCheckTest specCheckTestAnnotation = method.getAnnotation(SpecCheckTest.class);
        if (specCheckTestAnnotation == null || !isGrading || specCheckTestAnnotation.runWhenGrading()) {
          ++nTests;
        }
      }
    }
    return nTests;
  }

  /**
   * Uninstantiable.
   */
  private SpecChecker() {
  }
}
