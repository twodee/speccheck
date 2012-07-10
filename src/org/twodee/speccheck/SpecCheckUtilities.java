/*
 SpecCheck - a system for automatically generating tests for interface-conformance
 Copyright (C) 2012 Chris Johnson

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    // For interfaces, we don't care if they are marked abstract. TODO: is there
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
      list += getRequestedClassName(types[0]) + ".class";
      for (int i = 1; i < types.length; ++i) {
        list += ", " + getRequestedClassName(types[i]) + ".class";
      }
    }
    return list;
  }

  public static String getRequestedClassName(Class<?> clazz) {
    Specified anno = clazz.getAnnotation(Specified.class);
    if (anno == null || anno.inPackage().isEmpty()) {
      return clazz.getName();
    } else {
      return anno.inPackage() + "." + clazz.getSimpleName();
    }
  }
}
