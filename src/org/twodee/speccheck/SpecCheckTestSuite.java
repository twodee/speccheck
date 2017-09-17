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

  public static void assertEquals(String message, int expected, int actual) {
    if (expected != actual) {
      message = StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS);
      throw new AssertionError(String.format("%s%n      This is what I expected: %d%n  This is what I actually got: %d", message, expected, actual));
    }
  }

  public static void assertEquals(String message, String expected, String actual) {
    if (!expected.equals(actual)) {
      message = StringUtilities.wrap(message, SpecChecker.WRAP_COLUMNS);
      String[] expecteds = expected.split("(?<=\r?\n)");
      String[] actuals = actual.split("(?<=\r?\n)");

      for (int iline = 0; iline < expecteds.length || iline < actuals.length; ++iline) {

        String expectedLine = iline < expecteds.length ? expecteds[iline] : "";
        String actualLine = iline < actuals.length ? actuals[iline] : "";

        expectedLine = expectedLine.replaceAll("\n", "\\\\n");
        actualLine = actualLine.replaceAll("\n", "\\\\n");
        expectedLine = expectedLine.replaceAll("\r", "\\\\r");
        actualLine = actualLine.replaceAll("\r", "\\\\r");

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
                                                 "      This is what I expected for line %d: %s%n" +
                                                 "  This is what I actually got for line %d: %s%n" +
                                                 "                  Differences for line %d: %s", message, iline + 1, expectedLine, iline + 1, actualLine, iline + 1, diff));
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

  public static void assertEquals(boolean isVisual, String method, BufferedImage expected, BufferedImage actual, int tolerance) {
    assertEquals("Method " + method + " produced an image whose width was different than expected.", expected.getWidth(), actual.getWidth());
    assertEquals("Method " + method + " produced an image whose height was different than expected.", expected.getHeight(), actual.getHeight());

    // Images that have been written and read using ImageIO.write/read may not
    // have the same type that they were created with, so checking for types
    // is not so fun.
    /* assertEquals("Method " + method + " produced an image whose type was different than expected.", expected.getType(), actual.getType()); */

    for (int r = 0; r < expected.getHeight(); ++r) {
      for (int c = 0; c < expected.getWidth(); ++c) {
        if (!equalColors(expected.getRGB(c, r), actual.getRGB(c, r), tolerance)) {
          String msg = "Method " + method + " produced an image whose pixel (" + c + ", " + r + ") was not the expected color.";
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
