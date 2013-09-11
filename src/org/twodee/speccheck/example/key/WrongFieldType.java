package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheckGenerator;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public class WrongFieldType {
  @Specified
  public static final Integer foo = 5;
    
  public static void main(String[] args) throws ClassNotFoundException {
    new SpecCheckGenerator().generateInto("org.twodee.speccheck.example.key.WrongFieldTypeSpecChecker", WrongFieldType.class);
  }
}
