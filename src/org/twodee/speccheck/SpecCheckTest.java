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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A mechanism for marking up SpecCheck tests. Instructors only use this
 * annotation directly if they wish to add functional tests to the test suite
 * they distribute to students. With it, an instructor can add points to a test
 * and force it to run after interface-conformance tests.
 * 
 * @author cjohnson
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecCheckTest {
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
  int order() default 20;
}
