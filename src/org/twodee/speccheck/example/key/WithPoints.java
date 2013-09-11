package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheckGenerator;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class WithPoints {
  @Specified
  public WithPoints(String foo) {
  }

  @Specified
  public int getFive() {
    return 5;
  }

  public static void main(String[] args) throws ClassNotFoundException {
    new SpecCheckGenerator().generateInto("org.twodee.speccheck.example.key.WithPointsSpecChecker", WithPoints.class);
  }
}
