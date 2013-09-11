package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheckGenerator;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example", checkSuper = true)
public class SupertypeWrong extends RuntimeException {
  public static void main(String[] args) throws ClassNotFoundException {
    new SpecCheckGenerator().generateInto("org.twodee.speccheck.example.key.SupertypeWrongSpecChecker", SupertypeWrong.class);
  }
}
