package org.twodee.speccheck;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ArrayUtilities {
  public static boolean equal(int[][] ours,
                              int[][] theirs) {
    if (ours.length != theirs.length) {
      return false;
    }

    for (int r = 0; r < ours.length; ++r) {
      if (ours[r].length != theirs[r].length) {
        return false;
      }

      for (int c = 0; c < ours[r].length; ++c) {
        if (ours[r][c] != theirs[r][c]) {
          return false;
        }
      }
    }

    return true;
  }

  public static String toString(int[][] a,
                                String format) {
    StringBuilder builder = new StringBuilder();
    for (int r = 0; r < a.length; ++r) {
      for (int c = 0; c < a[r].length; ++c) {
        builder.append(String.format(format, a[r][c]));
      }
      builder.append(System.getProperty("line.separator"));
    }
    return builder.toString();
  }

  public static int[][] clone(int[][] source) {
    int[][] copy = new int[source.length][];

    for (int r = 0; r < source.length; r++) {
      copy[r] = new int[source[r].length];
      System.arraycopy(source[r], 0, copy[r], 0, source[r].length);
    }

    return copy;
  }

  public static BufferedImage toImage(int[][] pixels) {
    int width = pixels[0].length;
    int height = pixels.length;
    
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int r = 0; r < height; ++r) {
      for (int c = 0; c < width; ++c) {
        image.setRGB(c, r, new Color(pixels[r][c], pixels[r][c], pixels[r][c]).getRGB());
      }
    }
    
    return image;
  }
}
