package org.twodee.speccheck;

import java.lang.reflect.Method;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

public class SpecCheckTestResult {
    private Description testContext;
    private Failure failure;

    public SpecCheckTestResult(Description testContext) {
      this.testContext = testContext;
    }

    public void setFailure(Failure failure) {
      this.failure = failure;
    }

    public boolean isPassed() {
      return failure == null;
    }

    public boolean isFailed() {
      return !isPassed();
    }

    private SpecCheckTest getAnnotation() {
      try {
        Method method = SpecCheckUtilities.getMethod(testContext);
        SpecCheckTest annotation = method.getAnnotation(SpecCheckTest.class);
        return annotation;
      } catch (SecurityException e) {
        return null;
      } catch (NoSuchMethodException e) {
        return null;
      } catch (ClassNotFoundException e) {
        return null;
      }
    }

    public int getPointCount() {
      SpecCheckTest annotation = getAnnotation();
      return annotation != null ? annotation.nPoints() : 0;
    }

    public boolean isSpecCheckTest() {
      SpecCheckTest annotation = getAnnotation();
      return annotation != null && annotation.order() <= SpecCheckTest.MAX_SPECCHECK_TESTS_ORDER;
    }

    public Description getTestContext() {
      return testContext;
    }

    public Failure getFailure() {
      return failure;
    }
  }