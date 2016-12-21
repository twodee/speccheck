package org.twodee.speccheck;

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

  public static class SpecCheckPreTests {
  }

  public static class SpecCheckInterfaceTests {
  }

  public static class SpecCheckUnitTests {
  }

  public static class SpecCheckPostTests {
    @SpecCheckTest(order=100, runWhenGrading=false)
    @Test
    public void testCommitted() throws IOException {
      // IDs
      Set<String> types = new HashSet<String>();
      types.addAll(Arrays.asList("double", "char", "boolean", "float", "short", "long", "int", "byte", "Scanner", "String", "Random", "File", "BufferedImage", "Date", "GregorianCalendar"));

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

      Pattern pattern = Pattern.compile("^\\s*" + typePattern + "(?:\\s*\\[\\s*\\])*\\s+(\\w+)\\s*(?:=(?!=)|,|;)", Pattern.MULTILINE);

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
}
