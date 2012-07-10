package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheck;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class InPackage {
  @Specified
  public static void foo(InPackageTypeA a, InPackageTypeB b) {
  }

  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheck.generate(InPackage.class, InPackageTypeA.class, InPackageTypeB.class);
  }
}
