package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheckGenerator;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class BadReturnType {
  @Specified
  public static String foo() {
    return "foo";
  }

  public static void main(String[] args) throws ClassNotFoundException {
    new SpecCheckGenerator().generateInto("org.twodee.speccheck.example.key.BadReturnTypeSpecChecker", BadReturnType.class);
  }
}
