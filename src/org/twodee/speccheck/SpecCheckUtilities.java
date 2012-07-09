package org.twodee.speccheck;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

  public static String getModifierDiff(int expected,
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
    if (Modifier.isInterface(expected) != Modifier.isInterface(actual)) {
      msg += "It should " + (Modifier.isInterface(actual) ? "not " : "") + "be interface. ";
    } else {
      if (Modifier.isAbstract(expected) != Modifier.isAbstract(actual)) {
        msg += "It should " + (Modifier.isAbstract(actual) ? "not " : "") + "be abstract. ";
      }
    }
    return msg;
  }

  /**
   * Get a comma-separated list of class literals (i.e,
   * "String.class, boolean.class") corresponding to specified array of class
   * instances.
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
