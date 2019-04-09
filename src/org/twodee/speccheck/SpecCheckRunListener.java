package org.twodee.speccheck;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A custom JUnit RunListener. We offer our own so that we can optionally add
 * points to a student's score when tests pass.
 * 
 * @author cjohnson
 */
public class SpecCheckRunListener extends RunListener {
  private SpecCheckTestResults results;

  public SpecCheckRunListener() {
    results = new SpecCheckTestResults();
  }

  public SpecCheckTestResults getResults() {
    return results;
  }

  @Override
  public void testRunStarted(Description description) throws Exception {
    super.testRunStarted(description);
  }

  /**
   * Triggered when a new test has been started. If tagged with
   * 
   * @SpecCheckTest, the test's points are added to the total.
   */
  @Override
  public void testStarted(Description description) {
    results.add(description, new SpecCheckTestResult(description));
  }

  /**
   * Triggered when a test fails.
   */
  @Override
  public void testFailure(Failure failure) {
    results.get(failure.getDescription()).setFailure(failure);
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    super.testRunFinished(result);
  }
}