package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheckGenerator;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class MissingParameter {
  @Specified
  public static String foo(int a, int b) {
    return "foo";
  }

  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheckGenerator.generateInto("org.twodee.speccheck.example.key.MissingParameterSpecChecker", MissingParameter.class);
  }
}
