package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheck;
import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example")
public abstract class ModifiersWrong {
  @Specified
  public abstract void whistle();
  
  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheck.generate(ModifiersWrong.class);
  }
}
