package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheck;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class MissingConstructor {
  @Specified
  public MissingConstructor() {
  }

  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheck.generate(MissingConstructor.class);
  }
}
