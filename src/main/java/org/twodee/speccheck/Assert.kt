package org.twodee.speccheck

import java.awt.Color
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import java.util.regex.Pattern

object Assert {
  fun assertEquals(message: String, expected: Int, actual: Int) {
    if (expected != actual) {
      throw SpecViolation("$message\n  Expected: ${expected}\n    Actual: ${actual}")
    }
  }

  fun assertEquals(message: String, expected: Double, actual: Double, epsilon: Double) {
    if (Math.abs(actual - expected) > epsilon) {
      throw SpecViolation(String.format("$message\n  Expected: %.6f\n    Actual: %.6f", message, expected, actual))
    }
  }

  fun assertEquals(message: String, expected: Any, actual: Any) {
    if (expected != actual) {
      throw SpecViolation(String.format("$message\n  Expected: $expected\n    Actual: $actual", message, expected, actual))
    }
  }

  fun assertNonMatch(message: String, pattern: Pattern, text: String) {
    if (pattern.matcher(text).find()) {
      throw SpecViolation(message)
    }
  }

  fun fail(message: String) {
    throw SpecViolation(message)
  }

  fun assertEquals(message: String, expected: String, actual: String) {
    if (expected != actual) {
      val expecteds = expected.split("(?<=\r?\n)".toRegex()).dropLastWhile { it.isEmpty() }
      val actuals = actual.split("(?<=\r?\n)".toRegex()).dropLastWhile { it.isEmpty() }

      var iLine = 0
      while (iLine < expecteds.size || iLine < actuals.size) {
        var expectedLine = if (iLine < expecteds.size) expecteds[iLine] else ""
        var actualLine = if (iLine < actuals.size) actuals[iLine] else ""

        expectedLine = expectedLine.replace("\n".toRegex(), "\\\\n")
        actualLine = actualLine.replace("\n".toRegex(), "\\\\n")
        expectedLine = expectedLine.replace("\r".toRegex(), "\\\\r")
        actualLine = actualLine.replace("\r".toRegex(), "\\\\r")

        if (iLine >= actuals.size) {
          throw SpecViolation("$message\n  Expected line ${iLine + 1}: \"${expectedLine}\"\n  But I didn't get line ${iLine + 1} from you at all.")
        } else if (iLine >= expecteds.size) {
          throw SpecViolation("$message\n  I didn't expect a line ${iLine + 1} at all, but you had \"${actualLine}\".")
        } else {
          if (expectedLine != actualLine) {
            var diff = ""
            var i = 0
            while (i < expectedLine.length || i < actualLine.length) {
              if (i < expectedLine.length && i < actualLine.length && expectedLine[i] == actualLine[i]) {
                diff += ' '.toString()
              } else {
                diff += '^'.toString()
              }
              ++i
            }

            throw SpecViolation("""
$message
  Expected line ${iLine + 1}: "$expectedLine"
    Actual line ${iLine + 1}: "$actualLine"
      Differences:  $diff
""")
          }
        }
        ++iLine
      }
    }
  }

  fun assertEquals(message: String, expected: Color, actual: Color) {
    if (expected != actual) {
      throw SpecViolation(String.format("%s%n  Expected: (%3d, %3d, %3d, %3d)%n    Actual: (%3d, %3d, %3d, %3d)", message, expected.getRed(), expected.getGreen(), expected.getBlue(), expected.getAlpha(), actual.getRed(), actual.getGreen(), actual.getBlue(), actual.getAlpha()))
    }
  }

  fun assertEquals(message: String, expected: Array<BooleanArray>, actual: Array<BooleanArray>) {
    if (expected.size != actual.size) {
      throw SpecViolation("$message But the outer array had a different length than I expected.\n  Expected: ${expected.size}\n    Actual: ${actual.size}")
    }

    for (r in expected.indices) {
      if (expected[r].size != actual[r].size) {
        throw SpecViolation("$message But inner array $r had a different length than I expected.\n  Expected: ${expected[r].size}\n    Actual: ${actual[r].size}")
      }

      for (c in 0 until expected[r].size) {
        if (expected[r][c] != actual[r][c]) {
          throw SpecViolation("$message But element [$r][$c] wasn't what I expected.\n  Expected: ${expected[r][c]}\n    Actual: ${actual[r][c]}")
        }
      }
    }
  }

  fun <T> assertArrayEquals(message: String, expected: Array<T>, actual: Array<T>) {
    if (expected.size != actual.size) {
      throw SpecViolation("$message But the array had a different length than I expected.\n  Expected: ${expected.size}\n    Actual: ${actual.size}")
    }

    for (i in expected.indices) {
      if (expected[i] != actual[i]) {
        throw SpecViolation("$message But element $i wasn't what I expected.\n  Expected: ${expected[i]}\n    Actual: ${actual[i]}")
      }
    }
  }

  fun assertVersion(course: String?, semester: String?, homework: String?, actualVersion: Int) {
    if (course == null || semester == null || homework == null || actualVersion == 0) {
      System.err.println("No meta data provided. Unable to validate SpecChecker version.")
    } else {
      try {
        val url = URL(String.format("https://twodee.org/teaching/vspec.php?course=%s&semester=%s&homework=%s", course, semester, homework))
        val connection = url.openConnection()
        val inputStream = connection.getInputStream()
        val scanner = Scanner(inputStream)

        var expectedVersion = actualVersion
        if (scanner.hasNext()) {
          expectedVersion = scanner.nextInt()
        } else {
          System.err.println("Homework was not registered with the server. Unable to validate SpecChecker version.")
        }

        scanner.close()

        if (expectedVersion != actualVersion) {
          throw SpecViolation("You are running a SpecChecker that is out of date. Please pull down the latest version from the template remote.")
        }

      } catch (e: UnknownHostException) {
        System.err.println("Host twodee.org was inaccessible. Unable to validate SpecChecker version.")
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun assertNotNull(message: String, obj: Any?) {
    if (obj == null) {
      throw SpecViolation(message)
    }
  }

  fun assertIndependent(message: String, a: Array<BooleanArray>, b: Array<BooleanArray>) {
    if (a === b) {
      throw SpecViolation("$message But the array I got back is not independent of the source array. You need to make a brand new array.")
    }

    for (r in a.indices) {
      if (a[r] === b[r]) {
        throw SpecViolation("$message But the inner array at index %d is not independent of the source array. You need to make a brand new array.")
      }
    }
  }


}