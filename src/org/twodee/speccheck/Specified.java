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
 * A tag for marking that a construct is required by a homework specification.
 * 
 * @author cjohnson
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Specified {
  // --------------------------------------------------------------------------
  // CLASS-SPECIFIC PARAMETERS
  // These parameters only apply to classes. On methods, constructors, and
  // fields they are ignored.
  // --------------------------------------------------------------------------

  /**
   * Allows students to add unspecified public methods and instance variables.
   * This defaults to false so that a student is steered toward keeping helper
   * methods that are introduced private.
   */
  boolean allowUnspecifiedPublicStuff() default false;

  /**
   * Allows a public default constructor, even if the specification doesn't ask
   * for one. This option is included and defaults to true because the Java
   * compiler will automatically generate a public default constructor if no
   * other constructor is present. If unspecified public constructs are not
   * allowed, the class will not pass the SpecCheck tests without the student
   * adding in a constructor.
   * 
   * @see allowUnspecifiedPublicStuff
   */
  boolean allowUnspecifiedPublicDefaultCtor() default true;

  /**
   * Allows public constants (static and final) fields in the class.
   */
  boolean allowUnspecifiedPublicConstants() default false;

  /**
   * The number of allowed instance variables inside a class. If this number is
   * exceeded, a warning message is issued to the student. A value of -1 means
   * no limit is imposed.
   */
  int maxVariableCount() default -1;

  /**
   * Enforces the class's supertype.
   */
  boolean checkSuper() default false;

  /**
   * A list of interfaces that the student's version of this class must
   * implement. The interfaces must be explicitly named, allowing instructors to
   * impose on the students only a subset of the interfaces that the reference
   * implementation does.
   */
  Class<?>[] mustImplement() default {};

  // --------------------------------------------------------------------------
  // METHOD-SPECIFIC PARAMETERS
  // These parameters only apply to methods. On classes and fields, they are
  // ignored.
  // --------------------------------------------------------------------------

  /**
   * A list of exceptions that a method must be declared to throw.
   */
  Class<?>[] mustThrow() default {};

  /**
   * A list of exceptions that a method must not be declared to throw.
   */
  Class<?>[] mustNotThrow() default {};
}
