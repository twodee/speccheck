package org.twodee.speccheck;

import java.lang.reflect.Method;
import java.util.Comparator;
import org.junit.runner.Description;

/**
 * We want pure interface tests to run before any optional functional tests. To
 * support this, @SpecCheckTest exposes an order parameter. Tests with a lesser
 * order are run before tests with a greater order. This comparator can be used
 * to order two tests. This class is unlikely to be used directly by the
 * instructor.
 * 
 * @author cjohnson
 */
class SpecCheckTestComparator implements Comparator<Description> {
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