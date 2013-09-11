package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheckGenerator;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class MissingConstructor {
  @Specified
  public MissingConstructor() {
  }

  public static void main(String[] args) throws ClassNotFoundException {
    new SpecCheckGenerator().generateInto("org.twodee.speccheck.example.key.MissingConstructorSpecChecker", MissingConstructor.class);
  }
}
