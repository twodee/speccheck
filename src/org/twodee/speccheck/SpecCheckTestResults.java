package org.twodee.speccheck;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

public class SpecCheckTestResults {
  private HashMap<Description, SpecCheckTestResult> descriptionToResults;

  public SpecCheckTestResults() {
    descriptionToResults = new HashMap<Description, SpecCheckTestResult>();
  }

  public void add(Description description,
                  SpecCheckTestResult result) {
    descriptionToResults.put(description, result);
  }

  public SpecCheckTestResult get(Description description) {
    return descriptionToResults.get(description);
  }

  public int getSpecCheckTestsCount() {
    int nSpecCheckTests = 0;
    for (SpecCheckTestResult result : descriptionToResults.values()) {
      if (result.isSpecCheckTest()) {
        ++nSpecCheckTests;
      }
    }
    return nSpecCheckTests;
  }

  public int getSpecCheckTestsPassedCount() {
    int nSpecCheckTestsPassed = 0;
    for (SpecCheckTestResult result : descriptionToResults.values()) {
      if (result.isSpecCheckTest() && result.isPassed()) {
        ++nSpecCheckTestsPassed;
      }
    }
    return nSpecCheckTestsPassed;
  }

  public int getPassedCount() {
    int nPassed = 0;
    for (SpecCheckTestResult result : descriptionToResults.values()) {
      if (result.isPassed()) {
        ++nPassed;
      }
    }
    return nPassed;
  }

  public int getScorePossible() {
    int scorePossible = 0;
    for (SpecCheckTestResult result : descriptionToResults.values()) {
      scorePossible += result.getPointCount();
    }
    return scorePossible;
  }

  public int getScore() {
    int score = 0;
    for (SpecCheckTestResult result : descriptionToResults.values()) {
      if (result.isPassed()) {
        score += result.getPointCount();
      }
    }
    return score;
  }

  public int getFailedCount() {
    return getTestCount() - getPassedCount();
  }

  public int getTestCount() {
    return descriptionToResults.size();
  }

  public boolean isPerfect() {
    return getPassedCount() == getTestCount();
  }

  public boolean hasSpecCheckTests() {
    return getSpecCheckTestsCount() > 0;
  }

  /**
   * Gets whether or not a minimal set of interface tests have passed. The
   * minimal set consists of tests that are marked as SpecCheckTests and
   * compilation-error free code. Eclipse can generate bytecode for classes
   * that don't compile. This is annoying, and throws off my grading, which
   * does not use ecj to compile. Students see tests passing, but the grader
   * rejects those same solutions.
   * @return
   */
  public boolean isSpecCompliant() {
    if (getFailedCount() > 0) {
      for (Entry<Description, SpecCheckTestResult> pair : descriptionToResults.entrySet()) {
        Failure f = pair.getValue().getFailure();
        if (f != null) {
          Throwable throwable = f.getException();
          if (throwable.getLocalizedMessage() != null && throwable.getLocalizedMessage().contains("Unresolved compilation")) {
            return false;
          }
        }
      }
    }
    return getSpecCheckTestsCount() == getSpecCheckTestsPassedCount();
  }
  
  public Set<Entry<Description, SpecCheckTestResult>> getTests() {
    return descriptionToResults.entrySet();
  }
}