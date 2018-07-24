# Testing Pipeline Libraries

This global shared library also provides basic means for testing other global pipeline libraries.

## Usage

To test your global pipeline library, create a `Jenkinsfile` with tests, arranged in test suites.

* [Structural Methods](#structural-methods)  
* [Assertion Methods](#assertion-methods)  

### Example
  
```groovy
@Library('pipeline-utils@1.x') _

import static com.ebay.sd.jenkins.test.PipelineTestUtils.*

withCommonPipelineSettings {

  pipelineTests(this) {
  
    testSuite('withRetry') {
      test('withRetry - retries:3, successful first run') {
        def counter = 0
        withRetry(retries: 3) {
          counter++
        }
        assertEquals actual: counter, expected: 1, message: 'counter should be 1 - no retry attempts were expected'
      }
    
      test('withRetry - default retries, all 3 should fail') {
        counter = 0
        doAndExpectError(expectedMessage: 'DUMMY ERROR') {
          withRetry {
            counter++
            error 'DUMMY ERROR'
          }
        }
        assertEquals actual: counter, expected: 3, message: 'default retries should be 3'
      }
      
      // More related tests go here...
    }
    
    // More test suites go here...
  }
}
```
For a complete example, see: [Jenkinsfile-test-1.x](test/Jenkinsfile-test-1.x)

Running a pipeline jenkins job using these utilities creates a descriptive report with the test results, based on the JUnit format.

### Structural Methods

The test pipeline needs to be structured as in the example above using the methods below.

* [`pipelineTests`](#pipelinetests)
* [`testSuite`](#testsuite)
* [`test`](#test)

#### `pipelineTests`
Wraps all test suites, responsible for collecting the test results during the tests execution 
and for publishing the results in the end of all the test suites.

**Arguments:**
* `script` - a reference to the script that runs the tests. Should always be `this`  
 
#### `testSuite`
Wraps tests in the same area, mainly used for reporting. 
There must be at least one test suite, but it is best practice to split suites by distinct areas.

**Arguments:**
* `name` - (String) the name of the test suite, used for reporting, must be unique across all test suites

#### `test`
Defines a single test. Each test can include multiple steps and assertions. 
Each test must be part of a test suite. There must be at least one test in a test suite.

**Arguments:**
* `name` - (String) the name of the test, used for reporting, must be globally unique across all tests (not just in its test suite)

### Assertion Methods

* [`doAndExpectError`](#doandexpecterror)
* [`assertStringContains`](#assertstringcontains)
* [`assertEquals`](#assertequals)
* [`assertNotEquals`](#assertnotequals)
* [`fail`](#fail)

#### `doAndExpectError`
Wraps a code block and expects it to finish with an error. 
If the code block does not finish with an error, this step fails. 

**Arguments:**
* `expectedMessage` - (String) an expected error message. 
  If given, then the actual error message must equal to this expected message.  
  (optional, the error message is not checked if this is not provided)

**Example:**
```groovy
doAndExpectError(expectedMessage: 'DUMMY ERROR') {
  echo 'hello world!'
  error 'DUMMY ERROR'
}
```

#### `assertStringContains`
Assert that an actual string contains an expected string.

**Arguments:** (Map)
* `expected` - (String) the expected string
* `actual` - (String) the actual string
* `message` - (String) the error message in case the assertion fails

**Example:**
```groovy
def str = 'hello foo world!'
assertStringContains expected: 'foo', actual: str, message: 'str was expected to contain foo'
```

#### `assertEquals`
Assert that an actual value is equal to an expected value.

**Arguments:** (Map)
* `expected` - the expected value
* `actual` - the actual value
* `message` - (String) the error message in case the assertion fails

**Example:**
```groovy
def value = 7
assertEquals expected: 7, actual: value, message: 'value was expected to be 7'
```

#### `assertNotEquals`
Assert that an actual value is *not* equal to an expected value.

**Arguments:** (Map)
* `expected` - the expected value
* `actual` - the actual value
* `message` - (String) the error message in case the assertion fails

**Example:**
```groovy
def value = 7
assertNotEquals expected: null, actual: value, message: 'value should not be null'
```

#### `fail`
Simply fail the test with a given message

**Arguments:**
* `message` - (String) the error message in case the assertion fails

**Example:**
```groovy
fail 'the test failed for some reason...'
```