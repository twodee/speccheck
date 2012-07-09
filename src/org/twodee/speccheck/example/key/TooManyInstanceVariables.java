package org.twodee.speccheck.example.key;

import org.twodee.speccheck.SpecCheck;
import org.twodee.speccheck.Specified;

@Specified(maxVariableCount = 3)
public class TooManyInstanceVariables {
  private int a, b, c, d;

  public static void main(String[] args) throws ClassNotFoundException {
    SpecCheck.generate(TooManyInstanceVariables.class);
  }
}
