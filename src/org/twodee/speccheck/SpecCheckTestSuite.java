package org.twodee.speccheck;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class SpecCheckTestSuite {
  private static Set<String> sources = new HashSet<String>();
  private static Set<String> extraTypes = new HashSet<String>();

  static {
    // EXTRATYPES
  }

  public static void fail(String message) {
    throw new AssertionError(StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS));
  }

  public static void assertNotNull(String message, Object object) {
    if (object == null) {
      String expanded = String.format(message);
      throw new AssertionError(StringUtilities.wrap(expanded, SpecChecker.WRAP_COLUMNS));
    }
  }

  // Assumes a and b have same cardinalities.
  public static void assertIndependent(String message, boolean[][] a, boolean[][] b) {
    if (a == b) {
      message += " But the array I got back is not independent of the source array. You need to make a brand new array.";
      throw new AssertionError(String.format("%s", StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS)));
    }

    for (int r = 0; r < a.length; ++r) {
      if (a[r] == b[r]) {
        message += String.format(" But the inner array at index %d is not independent of the source array. You need to make a brand new array.", r);
        throw new AssertionError(String.format("%s", StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS)));
      }
    }
  }

  public static void assertEquals(String message, boolean[][] expected, boolean[][] actual) {
    if (expected.length != actual.length) {
      message += " But the outer array had a different length than I expected.";
      throw new AssertionError(String.format("%s%n      This is what I expected: %d%n  This is what I actually got: %d", StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS), expected.length, actual.length));
    }
      
    for (int r = 0; r < expected.length; ++r) {
      if (expected[r].length != actual[r].length) {
        message += " But the inner array at index " + r + " of the array I got back had a different length than I expected.";
        throw new AssertionError(String.format("%s%n      This is what I expected: %d%n  This is what I actually got: %d", StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS), expected.length, actual.length));
      }

      for (int c = 0; c < expected[r].length; ++c) {
        if (expected[r][c] != actual[r][c]) {
          message += String.format(" But element [%d][%d] of the array I got back wasn't what I expected.", r, c);
          throw new AssertionError(String.format("%s%n      This is what I expected: %d%n  This is what I actually got: %d", StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS), expected, actual));
        }
      }
    }
  }

  public static <T> void assertArrayEquals(String message, T[] expected, T[] actual) {
    if (expected.length != actual.length) {
      message += " But the array had a different length than I expected.";
      throw new AssertionError(String.format("%s%n      This is what I expected: %d%n  This is what I actually got: %d", StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS), expected.length, actual.length));
    }
      
    for (int i = 0; i < expected.length; ++i) {
      if (!expected[i].equals(actual[i])) {
        message += String.format(" But element %i of the array I got back wasn't what I expected.", i);
        throw new AssertionError(String.format("%s%n      This is what I expected: %d%n  This is what I actually got: %d", StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS), expected, actual));
      }
    }
  }

  private static Integer[] toObjectArray(int[] src) {
    Integer[] dst = new Integer[src.length];
    for (int i = 0; i < src.length; ++i) {
      dst[i] = src[i];
    }
    return dst;
  }

  public static void assertEquals(String message, int[] expected, int[] actual) {
    assertArrayEquals(message, toObjectArray(expected), toObjectArray(actual));
  }

  public static void assertEquals(String message, int expected, int actual) {
    if (expected != actual) {
      message = StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS);
      throw new AssertionError(String.format("%s%n      This is what I expected: %d%n  This is what I actually got: %d", message, expected, actual));
    }
  }

  public static void assertEquals(String message, double expected, double actual, double epsilon) {
    if (Math.abs(actual - expected) > epsilon) {
      message = StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS);
      throw new AssertionError(String.format("%s%n      This is what I expected: %.6f%n  This is what I actually got: %.6f", message, expected, actual));
    }
  }

  public static void assertEquals(String message, Object expected, Object actual) {
    if (!expected.equals(actual)) {
      message = StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS);
      throw new AssertionError(String.format("%s%n      This is what I expected: %s%n  This is what I actually got: %s", message, expected.toString(), actual.toString()));
    }
  }

  public static void assertEquals(String message, String expected, String actual) {
    if (!expected.equals(actual)) {
      message = StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS);
      String[] expecteds = expected.split("(?<=\r?\n)");
      String[] actuals = actual.split("(?<=\r?\n)");

      for (int iLine = 0; iLine < expecteds.length || iLine < actuals.length; ++iLine) {
        String expectedLine = iLine < expecteds.length ? expecteds[iLine] : "";
        String actualLine = iLine < actuals.length ? actuals[iLine] : "";

        expectedLine = expectedLine.replaceAll("\n", "\\\\n");
        actualLine = actualLine.replaceAll("\n", "\\\\n");
        expectedLine = expectedLine.replaceAll("\r", "\\\\r");
        actualLine = actualLine.replaceAll("\r", "\\\\r");

        if (iLine >= actuals.length) {
          throw new AssertionError(String.format("%s%n  This is what I expected for line %d: \"%s\"%n  But I didn't get line %d from you at all.", message, iLine + 1, expectedLine, iLine + 1));
        } else if (iLine >= expecteds.length) {
          throw new AssertionError(String.format("%s%n  I didn't expect a line %d.%n  But this is what I actually got for line %d: \"%s\"%n", message, iLine + 1, iLine + 1, actualLine));
        } else {

          if (!expectedLine.equals(actualLine)) {
            String diff = "";
            for (int i = 0; i < expectedLine.length() || i < actualLine.length(); ++i) {
              if (i < expectedLine.length() && i < actualLine.length() && expectedLine.charAt(i) == actualLine.charAt(i)) {
                diff += ' ';
              } else {
                diff += '^';
              }
            }

            throw new AssertionError(String.format("%s%n" +
                                                   "      This is what I expected for line %d: \"%s\"%n" +
                                                   "  This is what I actually got for line %d: \"%s\"%n" +
                                                   "                  Differences for line %d:  %s", message, iLine + 1, expectedLine, iLine + 1, actualLine, iLine + 1, diff));
          }
        }
      }
    }
  }

  public static void assertEquals(String message, Color expected, Color actual) {
    if (!expected.equals(actual)) {
      message = StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS);
      throw new AssertionError(String.format("%s%n      This is the RGBA color I expected: (%3d, %3d, %3d, %3d)%n  This is the RGBA color I actually got: (%3d, %3d, %3d, %3d)", message, expected.getRed(), expected.getGreen(), expected.getBlue(), expected.getAlpha(), actual.getRed(), actual.getGreen(), actual.getBlue(), actual.getAlpha()));
    }
  }

  public static void assertVersion(String course, String semester, String homework, int actualVersion) {
    if (course == null || semester == null || homework == null || actualVersion == 0) {
      System.err.println("No meta data provided. Unable to validate SpecChecker version.");
    } else {
      try {
        URL url = new URL(String.format("http://www.twodee.org/teaching/vspec.php?course=%s&semester=%s&homework=%s", course, semester, homework));
        URLConnection connection = url.openConnection();
        InputStream is = connection.getInputStream();
        Scanner in = new Scanner(is);

        int expectedVersion = actualVersion;
        if (in.hasNext()) {
          expectedVersion = in.nextInt();
        } else {
          System.err.println("Homework was not registered with the server. Unable to validate SpecChecker version.");
        }

        in.close();

        if (expectedVersion != actualVersion) {
          Assert.fail("You are running a SpecChecker that is out of date. Please pull down the latest version from the template remote.");
        }

      } catch (UnknownHostException e) {
        System.err.println("Host www.twodee.org was inaccessible. Unable to validate SpecChecker version.");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static class SpecCheckPreTests {
  }

  public static class SpecCheckInterfaceTests {
  }

  public static class SpecCheckUnitTests {
  }

  public static class SpecCheckPostTests {
    @SpecCheckTest(order=100, runWhenGrading=false)
    @Test
    public void testIdentifiers() throws IOException {
      // IDs
      Set<String> types = new HashSet<String>();
      types.addAll(Arrays.asList("double", "char", "boolean", "float", "short", "long", "int", "byte", "Scanner", "String", "Random", "File", "BufferedImage", "Date", "GregorianCalendar", "ArrayList", "Double", "Character", "Integer", "Boolean", "PrintWriter"));

      for (String srcPath : sources) {
        Pattern pattern = Pattern.compile("(\\w+)\\.java$");
        Matcher matcher = pattern.matcher(srcPath);
        if (matcher.find()) {
          types.add(matcher.group(1));
        }
      }

      types.addAll(extraTypes);

      String typePattern = "(?:";
      int i = 0;
      for (String type : types) {
        if (i > 0) {
          typePattern += "|";
        }
        typePattern += type;
        ++i;
      }
      typePattern += ")";

      Pattern pattern = Pattern.compile("\\b" + typePattern + "(?:\\s*\\[\\s*\\])*\\s+(\\w+)\\s*(?:=(?!=)|,|;|\\))", Pattern.MULTILINE);

      Set<String> ids = new HashSet<String>();
      for (String srcPath : sources) {
        String src = FileUtilities.slurp(srcPath);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
          ids.add(matcher.group(1));
        }
      }

      if (ids.size() > 0 && !DialogUtilities.isListOkay("Identifiers", "Variable names are important. Bad names mislead, confuse, and frustrate. Good names accurately describe the data they hold, are readable and pronounceable, follow camelCaseConventions, and will still make sense in a week's time. Following are some variable names from your code. Are they good names?", new ArrayList(ids))) {
        Assert.fail("Some of your variable names need improvement.");
      }
    }

    @SpecCheckTest(order=101, runWhenGrading=false)
    @Test
    public void testFinalChecklist() throws IOException {
      if (!SpecChecker.runChecklist) {
        return;
      }

      // Checklist
      String[] messages = {
        "I have eliminated all compilation errors from my code. In the Package Explorer, there are no red icons on any of my files and no red exclamation point on my project.",
        "I have committed my work to my local repository. In the Package Explorer, there are no files with greater-than signs (>) preceding their names.",
        "I have pushed my work to Bitbucket. In the Package Explorer, there are no up or down arrows followed by numbers after my project name.",
        "I have verified that my work is in my remote repository at http://bitbucket.org.",
      };
      if (!DialogUtilities.isChecked("Final Steps", messages)) {
        Assert.fail("Not all items on your final steps checklist have been completed.");
      }
    }
  }

  private static boolean equalColors(int expected, int actual, int tolerance) {
    Color expectedColor = new Color(expected, true);
    Color actualColor = new Color(actual, true);
    return Math.abs(expectedColor.getRed() - actualColor.getRed()) <= tolerance &&
           Math.abs(expectedColor.getGreen() - actualColor.getGreen()) <= tolerance &&
           Math.abs(expectedColor.getBlue() - actualColor.getBlue()) <= tolerance &&
           Math.abs(expectedColor.getAlpha() - actualColor.getAlpha()) <= tolerance;
  }

  public static void assertEquals(boolean isVisual, String message, BufferedImage expected, BufferedImage actual, int tolerance) {
    assertEquals(message + " But it produced an image whose width was different than expected.", expected.getWidth(), actual.getWidth());
    assertEquals(message + " But it produced an image whose height was different than expected.", expected.getHeight(), actual.getHeight());

    // Images that have been written and read using ImageIO.write/read may not
    // have the same type that they were created with, so checking for types
    // is not so fun.
    /* assertEquals("Method " + method + " produced an image whose type was different than expected.", expected.getType(), actual.getType()); */

    for (int r = 0; r < expected.getHeight(); ++r) {
      for (int c = 0; c < expected.getWidth(); ++c) {
        if (!equalColors(expected.getRGB(c, r), actual.getRGB(c, r), tolerance)) {
          String msg = message + " But it produced an image whose pixel (" + c + ", " + r + ") was not the expected color.";
          if (isVisual) {
            CompareFrame<JLabel> comparer = new CompareFrame<JLabel>(false);
            comparer.compare(msg, new JLabel(new ImageIcon(expected)), new JLabel(new ImageIcon(actual)));
          }
          assertEquals(msg, new Color(expected.getRGB(c, r), true), new Color(actual.getRGB(c, r), true));
        }
      }
    }
  }
}
