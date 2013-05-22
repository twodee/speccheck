package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheckGenerator;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class NothingSpecified {
  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheckGenerator.generateInto("org.twodee.speccheck.example.key.NothingSpecifiedSpecChecker", NothingSpecified.class);
  }
}
