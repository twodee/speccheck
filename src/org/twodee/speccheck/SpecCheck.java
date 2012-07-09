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

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

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
