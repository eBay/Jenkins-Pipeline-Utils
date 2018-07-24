/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

package com.ebay.sd.jenkins.test

import com.ebay.sd.jenkins.util.ArrayMap
import com.ebay.sd.jenkins.util.ArrayWrapper

/**
 * Utilities for testing Jenkins Pipeline steps.
 * Example:
 * <pre>
 *   node {
 *     pipelineTests(this) {
 *       testSuite('foo') {
 *         test('test foo 1') {
 *           //Test foo 1 ...
 *         }
 *         test('Test foo 2') {
 *           //Test foo 2 ...
 *         }
 *         // Add more foo related tests here...
 *       }
 *       // Add more test suites here...
 *     }
 *   }
 * </pre>
 */
class PipelineTestUtils implements Serializable {

  private static final ArrayMap totalResults = new ArrayMap()
  private static ArrayMap suiteResults = new ArrayMap()
  private static def script
  private static boolean hasFailures = false

  private PipelineTestUtils() {}

  /**
   * Start a scope of pipeline tests. Responsible for collecting tests and suites results and publish them in the end of the run.
   * <pre>
   *   node {
   *     pipelineTests(this) {
   *       //Add your test suites here
   *     }
   *   }
   * </pre>
   * @param script  the containing script, usually <tt>this</tt>
   * @param closure the scope with the test suites
   */
  static void pipelineTests(script, Closure closure) {
    PipelineTestUtils.script = script
    try {
      closure.call()
    } finally {
      script.stage('Pipeline Test Results') {
        finish()
      }
    }
  }

  /**
   * Start a test suite.
   * <pre>
   *   testSuite('test suite 1') {
   *     //Add your tests here
   *   }
   * </pre>
   * @param name    the name of the test suite - must be unique, used for reporting
   * @param closure the scope with suite related tests
   */
  static void testSuite(String name, Closure closure) {
    suiteResults = new ArrayMap()
    script.echo "*** TEST SUITE: ${name} ***"
    try {
      script.stage(name) {
        closure.call()
      }
    } finally {
      try {
        saveSuiteResults(name)
      } catch (e) {
        echo "[ERROR] Failed to save results of test suite '$name': $e"
      }
    }
  }

  /**
   * Start a test.
   * <pre>
   *   test('test 1') {
   *     //Add your actual test here
   *   }
   * </pre>
   * @param name    the name of the test - must be unique across all tests, used for reporting
   * @param closure the scope with the actual test - calling steps and asserting results
   */
  static void test(String name, Closure closure) {
    script.echo "TEST: ${name}"
    try {
      closure.call()
      testPassed(name)
    } catch (e) {
      script.echo "Test '${name}' failed: $e"
      testFailed(name, e.toString())
    }
  }

  /**
   * Do something which is expected to finish with an error. Asserts an error is thrown.
   * @param args    arguments for the expectation. Includes:
   *                - expectedMessage - (optional) the exact error message to expect
   * @param closure
   */
  static void doAndExpectError(Map args, Closure closure) {
    boolean noErrorRaised = false
    try {
      closure.call()
      noErrorRaised = true
    } catch (e) {
      if (args.expectedMessage) {
        assertEquals actual: e.message, expected: args.expectedMessage, message: 'Unexpected error message'
      }
    }
    if (noErrorRaised) {
      script.error "doAndExpectError: an error should have been raised (args: $args)"
    }
  }

  /**
   * Assert an actual string contains an expected string
   * @param args arguments for the expectation. Includes:
   *             - actual -   the actual value
   *             - expected - the expected value
   *             - message -  the error message in case the assertion fails
   */
  static void assertStringContains(Map args) {
    if (!(args.actual.contains(args.expected))) {
      fail "Assert string contains: ${args.message}; expected: '${args.expected}' , actual: '${args.actual}'"
    }
  }

  /**
   * Assert an actual value is equal to an expected value
   * @param args arguments for the expectation. Includes:
   *             - actual -   the actual value
   *             - expected - the expected value
   *             - message -  the error message in case the assertion fails
   */
  static void assertEquals(Map args) {
    if (args.actual != args.expected) {
      fail "Assert equals: ${args.message}; expected: '${args.expected}' , actual: '${args.actual}'"
    }
  }

  /**
   * Assert an actual value is NOT equal to an expected value
   * @param args arguments for the expectation. Includes:
   *             - actual -   the actual value
   *             - expected - the expected value
   *             - message -  the error message in case the assertion fails
   */
  static void assertNotEquals(Map args) {
    if (args.actual == args.expected) {
      fail "Assert not equals: ${args.message}; expected: '${args.expected}' , actual: '${args.actual}'"
    }
  }

  /**
   * Fail explicitly
   * @param message the error message
   */
  static void fail(String message) {
    script.echo "Test failed: $message"
    script.error message
  }

  private static void testPassed(String name) {
    addTestResult name, 'SUCCESS', ''
  }

  private static void testFailed(String name, String message) {
    hasFailures = true
    addTestResult name, 'FAILURE', message ?: ''
  }

  private static void addTestResult(String name, String status, String message) {
    totalResults.put name, new ArrayWrapper([status, message])
    suiteResults.put name, new ArrayWrapper([status, message])
  }

  private static void saveSuiteResults(String suiteName) {
    //Based on: https://stackoverflow.com/questions/4922867/junit-xml-format-specification-that-hudson-supports/4925847#4925847
    script.dir('target/pipeline-test-reports') {
      StringBuilder xmlContent = new StringBuilder()
      xmlContent.append("<testsuite tests=\"${suiteResults.size()}\" name=\"${suiteName}\">\n")
      suiteResults.forEach { key, values ->
        xmlContent.append("  <testcase classname=\"$suiteName\" name=\"$key\"")
        String status = values.get(0)
        if (status == 'SUCCESS') {
          xmlContent.append('/>\n')
        } else {
          String errorMessage = values.get(1)
          xmlContent.append('>\n')
                    .append("    <failure type=\"Test Failed\">${errorMessage}</failure>\n")
                    .append("  </testcase>\n")
        }
      }
      xmlContent.append('</testsuite>')
      String normalizedSuiteName = suiteName.replaceAll(' ', '_').replaceAll(',', '_').replaceAll('-', '_')
      String xmlFilename = "TEST-${normalizedSuiteName}.xml".toString()
      script.writeFile file: xmlFilename, text: xmlContent.toString()
    }
  }

  private static void finish() {
    printResults()
    script.junit '**/target/pipeline-test-reports/TEST-*.xml'
  }

  private static void printResults() {
    String text = "Overall Result: ${hasFailures ? 'FAILURE' : 'SUCCESS'}".toString()
    int maxLength = 0
    totalResults.forEach { key, values ->
      maxLength = maxLength > key.length() ? maxLength : key.length()
    }
    totalResults.forEach { key, values ->
      text += "\n${withRightPadding(key, maxLength)} | ${values.get(0)} | ${values.get(1)}".toString()
    }
    script.echo text
  }

  private static String withRightPadding(String text, int length) {
    int pads = length - text.length()
    String padding = ''
    for (int i = 0; i < pads; i++) {
      padding += ' '
    }
    "${text}${padding}"
  }
}