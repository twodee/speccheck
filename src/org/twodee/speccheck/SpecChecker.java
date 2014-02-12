package org.twodee.speccheck;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class SpecChecker {
  private static final String tag = "hw";
  private static final String[] filesToZip = {};

  public static void main(String[] args) {
    boolean isGrading = args.length > 0 && args[0].equals("-g");

    try {
      int status = SpecCheck.test(isGrading);
      System.exit(status);
    } catch (Error e) {
      System.out.println(e);
      System.out.println("Tests couldn't be run. Did you add JUnit to your project?");
      System.exit(20);
    }
  }

  public static class SpecCheckPreTests {
  }

  public static class SpecCheckInterfaceTests {
  }

  public static class SpecCheckUnitTests {
  }

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
     * Run the JUnit tests contained in the specified class, customizing output
     * for the student working on an assignment.
     * 
     * @param tester
     * Class containing JUnit tests.
     */
    public static int test(boolean isGrading) {
      try {
        SpecCheckTestResults results = runTestSuite();
        int status = results.report(isGrading);

        if (!isGrading && (!results.hasSpecCheckTests() || results.isSpecCompliant()) && filesToZip.length > 0) {
          SpecCheckZipper.zip(results.isPerfect(), tag, filesToZip);
        }

        return status;
      } catch (Error e) {
        System.out.println(e);
        System.out.println("Tests couldn't be run. Did you add JUnit to your project?");
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
    private static SpecCheckTestResults runTestSuite() {
      try {
        return evaluateTests();
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
    public static SpecCheckTestResults evaluateTests() {
      SpecCheckRunListener listener = new SpecCheckRunListener();
      SpecCheckTestResults results = listener.getResults();

      JUnitCore core = new JUnitCore();
      core.addListener(listener);

      Request request = Request.aClass(SpecCheckPreTests.class);
      request = request.sortWith(new SpecCheckTestComparator());
      core.run(request);

      if (results.isPerfect()) {
        request = Request.aClass(SpecCheckInterfaceTests.class);
        request = request.sortWith(new SpecCheckTestComparator());
        core.run(request);

        if (results.isPerfect()) {
          request = Request.aClass(SpecCheckUnitTests.class);
          request = request.sortWith(new SpecCheckTestComparator());
          core.run(request);
        }
      }

      return results;
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface SpecCheckTest {
    int MAX_SPECCHECK_TESTS_ORDER = 50;

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
    int order() default MAX_SPECCHECK_TESTS_ORDER + 1;
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

      // For interfaces, we don't care if they are marked abstract.
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
        list += types[0].getCanonicalName() + ".class";
        for (int i = 1; i < types.length; ++i) {
          list += ", " + types[i].getCanonicalName() + ".class";
        }
      }
      return list;
    }
  }

  static class SpecCheckTestResult {
    private Description testContext;
    private Failure failure;

    public SpecCheckTestResult(Description testContext) {
      this.testContext = testContext;
    }

    public void setFailure(Failure failure) {
      this.failure = failure;
    }

    public boolean isPassed() {
      return failure == null;
    }

    public boolean isFailed() {
      return !isPassed();
    }

    private SpecCheckTest getAnnotation() {
      try {
        Method method = SpecCheckUtilities.getMethod(testContext);
        SpecCheckTest annotation = method.getAnnotation(SpecCheckTest.class);
        return annotation;
      } catch (SecurityException e) {
        return null;
      } catch (NoSuchMethodException e) {
        return null;
      } catch (ClassNotFoundException e) {
        return null;
      }
    }

    public int getPointCount() {
      SpecCheckTest annotation = getAnnotation();
      return annotation != null ? annotation.nPoints() : 0;
    }

    public boolean isSpecCheckTest() {
      SpecCheckTest annotation = getAnnotation();
      return annotation != null && annotation.order() <= SpecCheckTest.MAX_SPECCHECK_TESTS_ORDER;
    }

    public Description getTestContext() {
      return testContext;
    }

    public Failure getFailure() {
      return failure;
    }
  }

  static class SpecCheckTestResults {
    private HashMap<Description, SpecCheckTestResult> descriptionToResults;

    public SpecCheckTestResults() {
      descriptionToResults = new HashMap<Description, SpecCheckTestResult>();
    }

    public void add(Description description,
                    SpecCheckTestResult result) {
      descriptionToResults.put(description, result);
    }

    public SpecCheckTestResult get(Description description) {
      return descriptionToResults.get(description);
    }

    public int getSpecCheckTestsCount() {
      int nSpecCheckTests = 0;
      for (SpecCheckTestResult result : descriptionToResults.values()) {
        if (result.isSpecCheckTest()) {
          ++nSpecCheckTests;
        }
      }
      return nSpecCheckTests;
    }

    public int getSpecCheckTestsPassedCount() {
      int nSpecCheckTestsPassed = 0;
      for (SpecCheckTestResult result : descriptionToResults.values()) {
        if (result.isSpecCheckTest() && result.isPassed()) {
          ++nSpecCheckTestsPassed;
        }
      }
      return nSpecCheckTestsPassed;
    }

    public int getPassedCount() {
      int nPassed = 0;
      for (SpecCheckTestResult result : descriptionToResults.values()) {
        if (result.isPassed()) {
          ++nPassed;
        }
      }
      return nPassed;
    }

    public int getScorePossible() {
      int scorePossible = 0;
      for (SpecCheckTestResult result : descriptionToResults.values()) {
        scorePossible += result.getPointCount();
      }
      return scorePossible;
    }

    public int getScore() {
      int score = 0;
      for (SpecCheckTestResult result : descriptionToResults.values()) {
        if (result.isPassed()) {
          score += result.getPointCount();
        }
      }
      return score;
    }

    public int getFailedCount() {
      return getTestCount() - getPassedCount();
    }

    public int getTestCount() {
      return descriptionToResults.size();
    }

    public boolean isPerfect() {
      return getPassedCount() == getTestCount();
    }

    public boolean hasSpecCheckTests() {
      return getSpecCheckTestsCount() > 0;
    }

    /**
     * Gets whether or not a minimal set of interface tests have passed. The
     * minimal set consists of tests that are marked as SpecCheckTests and
     * compilation-error free code. Eclipse can generate bytecode for classes
     * that don't compile. This is annoying, and throws off my grading, which
     * does not use ecj to compile. Students see tests passing, but the grader
     * rejects those same solutions.
     * @return
     */
    public boolean isSpecCompliant() {
      if (getFailedCount() > 0) {
        for (Entry<Description, SpecCheckTestResult> pair : descriptionToResults.entrySet()) {
          Failure f = pair.getValue().getFailure();
          if (f != null) {
            Throwable throwable = f.getException();
            if (throwable.getLocalizedMessage().contains("Unresolved compilation")) {
              return false;
            }
          }
        }
      }
      return getSpecCheckTestsCount() == getSpecCheckTestsPassedCount();
    }

    public int report(boolean isGrading) {
      final String wrapPattern = "(.{50,}?) ";

      String scoreMessage = String.format("%d out of %d tests pass.", getPassedCount(), getTestCount());
      if (getScorePossible() > 0) {
        scoreMessage = String.format("You received %d/%d points. %s", getScore(), getScorePossible(), scoreMessage);
      }

      if (!isGrading) {
        System.out.printf("%s%n%n", scoreMessage);
      }

      if (getFailedCount() > 0) {
        for (Entry<Description, SpecCheckTestResult> pair : descriptionToResults.entrySet()) {
          Failure f = pair.getValue().getFailure();
          if (f != null) {
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
        }

        System.out.println("If you do not fix these problems, you are deviating from the homework specification and may not receive credit for your work.".replaceAll(wrapPattern, "$1\n"));
        System.out.println();
      }

      if (getScorePossible() != 0) {
        scoreMessage = "TOTAL: " + getScore() + "/" + getScorePossible();
      }

      if (isPerfect()) {
        System.out.println("You've passed all tests. Now commit and push before the deadline.".replaceAll(wrapPattern, "$1\n"));
        return 0;
      } else if (hasSpecCheckTests() && isSpecCompliant()) {
        System.out.printf("You've not passed all tests. However, you've passed enough tests to qualify for later-week submission. Now commit and push before the deadline.%n".replaceAll(wrapPattern, "$1\n"));
        return 10;
      } else {
        System.out.printf("You have not passed enough tests to qualify for later-week submission.%n".replaceAll(wrapPattern, "$1\n"));
        return 20;
      }
    }
  }

  /**
   * A custom JUnit RunListener. We offer our own so that we can optionally add
   * points to a student's score when tests pass.
   * 
   * @author cjohnson
   */
  static class SpecCheckRunListener extends RunListener {
    private SpecCheckTestResults results;

    public SpecCheckRunListener() {
      results = new SpecCheckTestResults();
    }

    public SpecCheckTestResults getResults() {
      return results;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
      super.testRunStarted(description);
    }

    /**
     * Triggered when a new test has been started. If tagged with
     * 
     * @SpecCheckTest, the test's points are added to the total.
     */
    @Override
    public void testStarted(Description description) {
      results.add(description, new SpecCheckTestResult(description));
    }

    /**
     * Triggered when a test fails.
     */
    @Override
    public void testFailure(Failure failure) {
      results.get(failure.getDescription()).setFailure(failure);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
      super.testRunFinished(result);
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

  static class SpecCheckZipper {
    public static void zip(boolean isPerfect,
                           String tag,
                           String... requiredFiles) {
      ArrayList<File> filesToZip = getFilesToZip(requiredFiles);

      // Create archive.
      boolean ok = false;
      try {
        String m = isPerfect ? "All tests pass." : "Not all tests pass.";
        int opt = JOptionPane.showConfirmDialog(null, m + " Create ZIP to submit?", "Create ZIP", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
          System.out.println("Choose a *directory* in which to store " + tag + ".zip.");
          ok = zip(tag, filesToZip);
          JOptionPane.getRootFrame().dispose();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      // Send them on.
      if (ok) {
        JOptionPane.showMessageDialog(null, "Open the ZIP file you created. Verify that it has your files in it.");
        System.out.println();
        System.out.println("Drop your archive named " + tag + ".zip into W:\\c s\\johnch\\cs245\\students\\<YOUR-USERNAME>.");
      } else {
        System.out.println("Archive not created!");
      }
    }

    private static ArrayList<File> getFilesToZip(String[] requiredFiles) {
      ArrayList<File> filesToZip = new ArrayList<File>();

      for (String path : requiredFiles) {
        File f = new File(path);
        if (f.isDirectory()) {
          addDirectory(f, filesToZip);
        } else {
          filesToZip.add(f);
        }
      }

      return filesToZip;
    }

    private static void addDirectory(File dir,
                                     ArrayList<File> files) {
      if (dir.exists()) {
        File[] contents = dir.listFiles();
        for (File file : contents) {
          if (file.isDirectory() && !file.getName().equals("speccheck")) {
            addDirectory(file, files);
          } else if (!files.contains(file) && !file.getName().startsWith("speccheck") && !file.getName().endsWith(".jar")) {
            files.add(file);
          }
        }
      } else {
        System.err.println("Directory " + dir + " doesn't exist.");
      }
    }

    /**
     * A help method for creating the archive tag.zip from the specified source
     * files.
     * 
     * @param tag
     * Root name of zip file.
     * 
     * @param sources
     * Paths to source files.
     * 
     * @return True if the archive was successfully created. False if the user
     * canceled the archiving because of a missing source file or a canceled
     * save dialog.
     * 
     * @throws IOException
     */
    private static boolean zip(String tag,
                               ArrayList<File> sources) throws IOException {
      JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int result = chooser.showOpenDialog(null);
      if (result != JFileChooser.APPROVE_OPTION) {
        return false;
      }
      String zipName = chooser.getSelectedFile().getAbsolutePath() + File.separatorChar + tag + ".zip";
      System.out.println("Archive to be saved at " + zipName + ".");

      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(zipName);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("Couldn't create ZIP file! Does the directory " + chooser.getSelectedFile().getAbsolutePath() + " actually exist?", e);
      }

      BufferedOutputStream bos = new BufferedOutputStream(fos);
      ZipOutputStream zos = new ZipOutputStream(bos);
      byte[] data = new byte[2048];
      boolean ok;

      System.out.println("Files in archive:");
      for (File source : sources) {
        ok = addSourceFile(source, zos, data);
        if (!ok) {
          return false;
        }
      }
      System.out.println();

      try {
        zos.close();
      } catch (IOException e) {
        throw new RuntimeException("Couldn't close ZIP file! Was at least one source code file present?", e);
      }

      return true;
    }

    /**
     * A helper method from dumping a source file out an already-opened zip
     * file.
     * 
     * @param source
     * Path to source file.
     * 
     * @param zos
     * Existing zip output stream.
     * 
     * @param data
     * Existing buffer for writing file contents.
     * 
     * @return True if archiving was not canceled due to missing file.
     * 
     * @throws IOException
     */
    private static boolean addSourceFile(File source,
                                         ZipOutputStream zos,
                                         byte[] data) throws IOException {
      BufferedInputStream bis;
      try {
        bis = new BufferedInputStream(new FileInputStream(source));
        ZipEntry entry = new ZipEntry(source.getPath().replace('\\', '/'));
        zos.putNextEntry(entry);
        int bytesRead;
        while ((bytesRead = bis.read(data, 0, data.length)) != -1) {
          zos.write(data, 0, bytesRead);
        }
        System.out.println(" - " + source);
      } catch (FileNotFoundException e) {
        int ok = JOptionPane.showConfirmDialog(null, "File " + source + " couldn't be found. Create archive anyway?", "Missing File", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
          return false;
        }
      } catch (IOException e) {
        System.out.println("Couldn't write or read file! Did you yank out your hard drive?");
        e.printStackTrace();
        throw e;
      }

      return true;
    }

    public static boolean hasOccludingSpecCheckers(String current) {
      String classpath = System.getProperty("java.class.path");
      String[] entries = classpath.split(File.pathSeparator);
      for (String entry : entries) {
        String tail = new File(entry).getName();
        if (tail.startsWith("speccheck")) {
          if (tail.startsWith("speccheck_" + current)) {
            return false;
          } else {
            System.out.println("A SpecChecker from a previous assignment is on the Build Path,");
            System.out.println("and it may interfere with this assignment's SpecChecker.");
            System.out.println("Please remove it by expanding Referenced Libraries,");
            System.out.println("right-clicking on " + tail + ",");
            System.out.println("and selecting Build Path -> Remove from Build Path.");
            System.out.println();
            return true;
          }
        }
      }
      return false;
    }

    /**
     * This security manager prevents students from calling System.exit() and
     * ending tests, throws a SecurityException instead.
     * 
     * @see "http://forums.sun.com/thread.jspa?threadID=667798"
     */
    public final static SecurityManager noExit = new SecurityManager() {
      @Override
      public void checkPermission(Permission perm) {
        if (perm.getName().startsWith("exitVM")) {
          throw new SecurityException("Your code cannot be checked because you call System.exit()");
        }
      }
    };
  }
}
