package org.twodee.speccheck.utilities;

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
}
