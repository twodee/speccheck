package org.twodee.speccheck;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Permission;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.runner.Description;

/**
 * This class contains a number of helper methods for ripping apart JUnit data
 * types into usable pieces.
 * 
 * @author cjohnson
 */
public class SpecCheckUtilities {
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