package org.twodee.speccheck

import java.awt.Color
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import java.util.regex.Pattern
import javax.swing.ImageIcon
import javax.swing.JLabel
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.min

object Assert {
  fun assertTrue(predicate: Boolean, message: String, vararg fields: Any) {
    if (!predicate) {
      throw SpecViolation(String.format(message, *fields))
    }
  }

  fun assertFalse(predicate: Boolean, message: String, vararg fields: Any) {
    if (predicate) {
      throw SpecViolation(String.format(message, *fields))
    }
  }

  fun assertThrows(exception: Class<*>, message: String, action: () -> Unit) {
    try {
      action()
    } catch (e: java.lang.Exception) {
      if (e::class.java == exception) {
        return
      }
    }

    throw SpecViolation(message)
  }

  fun assertSame(expected: Any, actual: Any, message: String, vararg fields: Any) {
    if (expected !== actual) {
      throw SpecViolation(String.format("$message\n", *fields))
    }
  }

  fun assertEquals(expected: Int, actual: Int, message: String, vararg fields: Any) {
    if (expected != actual) {
      throw SpecViolation(String.format("$message\n  Expected: %d\n    Actual: %d", *fields, expected, actual))
    }
  }

  fun assertEquals(expected: Double, actual: Double, epsilon: Double, message: String, vararg fields: Any) {
    if (abs(actual - expected) > epsilon) {
      throw SpecViolation(String.format("$message\n  Expected: %.6f\n    Actual: %.6f", *fields, expected, actual))
    }
  }

  fun assertEquals(expected: Any?, actual: Any?, message: String, vararg fields: Any) {
    if (expected != actual) {
      if (actual == null) {
        throw SpecViolation(String.format("$message\n  Expected: $expected\n    Actual: null", expected, *fields))
      } else {
        throw SpecViolation(String.format("$message\n  Expected: $expected\n    Actual: $actual", expected, actual, *fields))
      }
    }
  }

  fun assertNotMatches(pattern: Pattern, text: String, message: String, vararg fields: Any) {
    if (pattern.matcher(text).find()) {
      throw SpecViolation(String.format(message, *fields))
    }
  }

  fun assertMatches(pattern: Regex, text: String, message: String, vararg fields: Any) {
    if (!pattern.matches(text)) {
      throw SpecViolation(String.format(message, *fields))
    }
  }

  fun fail(message: String, vararg fields: Any) {
    throw SpecViolation(String.format(message, *fields))
  }

  fun assertEquals(expected: String, actual: String, message: String, vararg fields: Any) {
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
          throw SpecViolation(String.format("$message\n  Expected line ${iLine + 1}: \"${expectedLine}\"\n  But I didn't get line ${iLine + 1} from you at all.", *fields))
        } else if (iLine >= expecteds.size) {
          throw SpecViolation(String.format("$message\n  I didn't expect a line ${iLine + 1} at all, but you had \"${actualLine}\".", *fields))
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

            throw SpecViolation(String.format("""
$message
  Expected line ${iLine + 1}: "$expectedLine"
    Actual line ${iLine + 1}: "$actualLine"
      Differences:  $diff
""", *fields))
          }
        }
        ++iLine
      }
    }
  }

  fun assertEquals(expected: Color, actual: Color, message: String, vararg fields: Any) {
    if (expected != actual) {
      throw SpecViolation(String.format("$message%n  Expected: (%3d, %3d, %3d, %3d)%n    Actual: (%3d, %3d, %3d, %3d)", expected.red, expected.green, expected.blue, expected.alpha, actual.red, actual.green, actual.blue, actual.alpha, *fields))
    }
  }

  fun assertEquals(expected: Color, actual: Color, tolerance: Int, message: String, vararg fields: Any) {
    if (abs(expected.red - actual.red) > tolerance ||
        abs(expected.green - actual.green) > tolerance ||
        abs(expected.blue - actual.blue) > tolerance ||
        abs(expected.alpha - actual.alpha) > tolerance) {
      throw SpecViolation(String.format("$message%n  Expected: (%3d, %3d, %3d, %3d)%n    Actual: (%3d, %3d, %3d, %3d)", message, expected.red, expected.green, expected.blue, expected.alpha, actual.red, actual.green, actual.blue, actual.alpha, *fields))
    }
  }

  fun assertEquals(expected: Array<BooleanArray>, actual: Array<BooleanArray>, message: String, vararg fields: Any) {
    if (expected.size != actual.size) {
      throw SpecViolation(String.format("$message But the outer array had a different length than I expected.\n  Expected: ${expected.size}\n    Actual: ${actual.size}", *fields))
    }

    for (r in expected.indices) {
      if (expected[r].size != actual[r].size) {
        throw SpecViolation(String.format("$message But inner array $r had a different length than I expected.\n  Expected: ${expected[r].size}\n    Actual: ${actual[r].size}", *fields))
      }

      for (c in 0 until expected[r].size) {
        if (expected[r][c] != actual[r][c]) {
          throw SpecViolation(String.format("$message But element [$r][$c] wasn't what I expected.\n  Expected: ${expected[r][c]}\n    Actual: ${actual[r][c]}", *fields))
        }
      }
    }
  }

  fun <T> assertArrayEquals(expected: Array<T>, actual: Array<T>, message: String, vararg fields: Any) {
    // Compare same-sized chunks first.
    repeat (min(expected.size, actual.size)) { i ->
      if (expected[i] != actual[i]) {
        throw SpecViolation(String.format("$message But element $i wasn't what I expected.\n  Expected: ${expected[i]}\n    Actual: ${actual[i]}", *fields))
      }
    }

    if (expected.size != actual.size) {
      throw SpecViolation(String.format("$message But the array had a different length than I expected.\n  Expected: ${expected.size} ${expected.contentToString()}\n    Actual: ${actual.size} ${actual.contentToString()}", *fields))
    }
  }

  fun <T> assertEquals(expected: ArrayList<T>, actual: ArrayList<T>, message: String, vararg fields: Any) {
    // Compare same-sized chunks first.
    repeat (min(expected.size, actual.size)) { i ->
      if (expected[i] != actual[i]) {
        throw SpecViolation(String.format("$message But element $i wasn't what I expected.\n  Expected: ${expected[i]}\n    Actual: ${actual[i]}", *fields))
      }
    }

    if (expected.size != actual.size) {
      throw SpecViolation(String.format("$message But the list had a different length than I expected.\n  Expected: ${expected.size} ${expected}\n    Actual: ${actual.size} $actual", *fields))
    }
  }

  fun assertNull(obj: Any?, message: String, vararg fields: Any) {
    if (obj != null) {
      throw SpecViolation(String.format(message, *fields))
    }
  }

  fun assertNotNull(obj: Any?, message: String, vararg fields: Any) {
    if (obj == null) {
      throw SpecViolation(String.format(message, *fields))
    }
  }

  fun assertIndependent(a: Array<BooleanArray>, b: Array<BooleanArray>, message: String, vararg fields: Any) {
    if (a === b) {
      throw SpecViolation(String.format("$message But the array I got back is not independent of the source array. You need to make a brand new array.", *fields))
    }

    for (r in a.indices) {
      if (a[r] === b[r]) {
        throw SpecViolation(String.format("$message But the inner array at index %d is not independent of the source array. You need to make a brand new array.", *fields))
      }
    }
  }

  fun assertIndependent(noun: String, a: Any, b: Any, message: String, vararg fields: Any) {
    if (a === b) {
      throw SpecViolation(String.format("$message But the $noun I got back is not independent of the source $noun. You need to make a brand new $noun.", *fields))
    }
  }

  private fun equalColors(expected: Int, actual: Int, tolerance: Int): Boolean {
    val expectedColor = Color(expected, true)
    val actualColor = Color(actual, true)
    return abs(expectedColor.red - actualColor.red) <= tolerance && abs(expectedColor.green - actualColor.green) <= tolerance && abs(expectedColor.blue - actualColor.blue) <= tolerance && abs(expectedColor.alpha - actualColor.alpha) <= tolerance
  }

  fun assertEquals(isVisual: Boolean, expected: BufferedImage, actual: BufferedImage, tolerance: Int, message: String, fields: Any) {
    assertEquals(expected.width, actual.width, "$message But it produced an image whose width was different than expected.", fields)
    assertEquals(expected.height, actual.height, "$message But it produced an image whose height was different than expected.", fields)

    // Images that have been written and read using ImageIO.write/read may not
    // have the same type that they were created with, so checking for types
    // is not so fun.
    /* assertEquals("Method " + method + " produced an image whose type was unexpected.", expected.getType(), actual.getType()); */

    for (r in 0 until expected.height) {
      for (c in 0 until expected.width) {
        if (!equalColors(expected.getRGB(c, r), actual.getRGB(c, r), tolerance)) {
          val msg = "$message But it produced an image whose pixel ($c, $r) was not the expected color."
          if (isVisual && !isGrader) {
            val comparer = CompareFrame<JLabel>(false)
            comparer.compare(msg, JLabel(ImageIcon(expected)), JLabel(ImageIcon(actual)))
          }
          assertEquals(Color(expected.getRGB(c, r), true), Color(actual.getRGB(c, r), true), msg)
        }
      }
    }
  }

  private val isGrader: Boolean
    get() = System.getProperty("grader") == "true"
}
