package org.twodee.speccheck;

import org.junit.Assert;
import org.junit.Test;

public class SpecCheckTestSuite {
  public static class SpecCheckPreTests {
  }

  public static class SpecCheckInterfaceTests {
  }

  public static class SpecCheckUnitTests {
  }

  public static class SpecCheckPostTests {
    @Test
    public void testCommitted() {
      String[] messages = {
        "I have eliminated all compilation errors from my code. In the Package Explorer, there are no red icons on any of my files.",
        "I have committed my work to my local repository. In the Package Explorer, there are no files with greater-than signs (>) preceding their names.",
        "I have pushed my work to Bitbucket. In the Package Explorer, there are no up or down arrows followed by numbers after my project name.",
        "I have verified that my work is in my remote repository at http://bitbucket.org.",
      };
      if (!DialogUtilities.isChecked(messages)) {
        Assert.fail("Not all items on your final steps checklist have been completed.");
      }
    }
  }
}
