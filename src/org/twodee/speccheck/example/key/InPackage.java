package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheckGenerator;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class InPackage {
  @Specified
  public static void foo(org.twodee.speccheck.example.InPackageTypeA a, org.twodee.speccheck.example.InPackageTypeB b) {
  }

  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheckGenerator.generateInto("org.twodee.speccheck.example.key.InPackageSpecChecker", InPackage.class, InPackageTypeA.class, InPackageTypeB.class);
  }
}
