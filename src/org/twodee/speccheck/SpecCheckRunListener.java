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

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.junit.ComparisonFailure;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A custom JUnit RunListener. We offer our own so that we can optionally add
 * points to a student's score when tests pass.
 * 
 * @author cjohnson
 */
public class SpecCheckRunListener extends RunListener {
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
   * Triggered when a new test has been started. If tagged with @SpecCheckTest,
   * the test's points are added to the total.
   */
  @Override
  public void testStarted(Description description) {
    try {
      Method m = SpecCheckUtilities.getMethod(description);
      SpecCheckTest anno = m.getAnnotation(SpecCheckTest.class);
      if (anno != null) {
        // I put the test/points in the map here, but it may be taken out later
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
   * Gets the number of points earned. This number is only meaningful after the
   * tests have been run.
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
