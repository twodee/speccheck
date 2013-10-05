package org.twodee.speccheck.utilities;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class CompareTextFrame extends CompareFrame<JTextArea> {

  private static final int NROWS = 30;
  private static final int NCOLS = 20;

  public CompareTextFrame compare(String msg,
                                  String ours,
                                  String theirs) {
    Font font = new Font("Monospaced", Font.PLAIN, 12);

    JTextArea ourBox = new JTextArea(ours, NROWS, NCOLS);
    ourBox.setLineWrap(true);
    ourBox.setFont(font);

    JTextArea theirBox = new JTextArea(theirs, NROWS, NCOLS);
    theirBox.setLineWrap(true);
    theirBox.setFont(font);

    compare(msg, ourBox, theirBox);
    return this;
  }

  public CompareTextFrame compare(String msg,
                                  File ours,
                                  File theirs) throws IOException {
    return compare(msg,
                   FileUtilities.getFileText(ours),
                   FileUtilities.getFileText(theirs));
  }

  public CompareTextFrame compare(String msg,
                                  int[][] ours,
                                  int[][] theirs,
                                  String format) {
    return compare(msg,
                   ArrayUtilities.toString(ours, format),
                   ArrayUtilities.toString(theirs, format));
  }

  public <E> CompareTextFrame compare(String msg,
                                      ArrayList<E> ours,
                                      ArrayList<E> theirs,
                                      String separator) {
    return compare(msg,
                   StringUtilities.join(ours, separator),
                   StringUtilities.join(theirs, separator));
  }
}
