package org.twodee.speccheck;

import org.twodee.speccheck.test.Works;

public class Main {
  public static void main(String[] args) {
    String foo = Generator.INSTANCE.generateJson(Works.class);
    System.out.println(foo);

    try {
      String json = UtilitiesKt.getDummy(); //"[{\"name\": \"org.twodee.speccheck.test.Works\"}]";
      Verifier.INSTANCE.verify(json);
    } catch (SpecViolation e) {
      System.err.println(e.getMessage());
    }
//    Verifier.INSTANCE.verify(UtilitiesKt.getDummy());
  }
}

