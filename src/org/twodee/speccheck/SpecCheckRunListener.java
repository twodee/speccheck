package org.twodee.speccheck;

import java.lang.reflect.Method;
import java.util.HashMap;
import org.junit.runner.Description;
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

  /**
   * Initializes a new RunListener with 0 possible points and no tests yet.
   */
  public SpecCheckRunListener() {
    testToPoints = new HashMap<Description, Integer>();
    nPointsPossible = 0;
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
  public int getScore() {
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
  public int getScorePossible() {
    return nPointsPossible;
  }
}
