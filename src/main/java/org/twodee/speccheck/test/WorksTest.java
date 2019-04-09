package org.twodee.speccheck.test;

import org.twodee.speccheck.Assert;
import org.twodee.speccheck.Test;

public class WorksTest {
  @Test
  public void testFoo() {
    Assert.INSTANCE.assertEquals("I tried boody boo.", 5, 623);
  }
}
