package org.twodee.speccheck.test;

import org.twodee.speccheck.SpecifiedClass;
import org.twodee.speccheck.SpecifiedConstructor;
import org.twodee.speccheck.SpecifiedField;
import org.twodee.speccheck.SpecifiedMethod;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

@SpecifiedClass(
  isSuperclassChecked = true,
  maxInstanceVariables = 3,
  mustImplement = ActionListener.class,
  unitTesters = WorksTest.class
)
public class Works extends ArrayList<String> implements ActionListener {
  @SpecifiedField
  public double a = 10;
//  public int b = 6;

  @SpecifiedConstructor
  public Works(String l, int j, double d) {
    System.out.println(void.class);
  }

  @SpecifiedMethod(mustThrow = IOException.class, mustNotThrow = FileNotFoundException.class)
  public int foo(int i) throws IOException {
    throw new IOException();
  }

  //  @Override
  @SpecifiedMethod
  public void actionPerformed(ActionEvent e) {
  }
}
