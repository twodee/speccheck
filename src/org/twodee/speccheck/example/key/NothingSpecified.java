package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheck;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class NothingSpecified {
  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheck.generate(NothingSpecified.class);
  }
}
