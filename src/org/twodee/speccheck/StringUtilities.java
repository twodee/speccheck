package org.twodee.speccheck;

import java.util.ArrayList;

public class StringUtilities {
  public static String join(ArrayList<?> list,
                            String separator) {
    if (list.isEmpty()) {
      return "";
    }

    String joined = list.get(0).toString();
    for (int i = 1; i < list.size(); ++i) {
      joined += separator + list.get(i);
    }

    return joined;
  }
  
  public static String wrap(String s,
                            int nChars) {
    final String wrapPattern = "(.{1," + nChars + "})( +|\n)";
    return s.replaceAll(wrapPattern, String.format("$1%n"));
  }
}
