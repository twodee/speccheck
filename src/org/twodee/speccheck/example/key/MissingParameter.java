package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheck;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class MissingParameter {
  @Specified
  public static String foo(int a, int b) {
    return "foo";
  }

  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheck.generate(MissingParameter.class);
  }
}
