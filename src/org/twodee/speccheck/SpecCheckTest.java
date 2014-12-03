package org.twodee.speccheck;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SpecCheckTest {
  int MAX_SPECCHECK_TESTS_ORDER = 50;

  /**
   * The number of points this test contributes to the student's final score if
   * this test passes.
   */
  int nPoints() default 0;

  /**
   * An indicator of priority for this test. A lesser-numbered test runs before
   * a greater-numbered test. Pure interface tests should be given a low number,
   * like 0, so that they are executed first.
   * 
   * @return
   */
  int order() default MAX_SPECCHECK_TESTS_ORDER + 1;
  
  boolean runWhenGrading() default true;
}