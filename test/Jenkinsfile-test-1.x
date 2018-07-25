/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

@Library('pipeline-utils@1.x') _


import com.ebay.sd.jenkins.util.ArrayMap
import com.ebay.sd.jenkins.util.ArrayWrapper

import static com.ebay.sd.jenkins.test.PipelineTestUtils.*

withCommonPipelineSettings {
  node('jenkins-pipeline-utils-test-1.x') {

    pipelineTests(this) {

      testSuite('ArrayWrapper') {

        test('Empty ArrayWrapper') {
          def array = new ArrayWrapper()
          assertEquals expected: 0, actual: array.size(), message: 'Array size not as expected'
          assertEquals expected: '[]', actual: array.toString(), message: 'Array toString not as expected'
        }

        test('new ArrayWrapper from list') {
          def array = new ArrayWrapper([1, 'a', true, null])
          assertEquals expected: 4, actual: array.size(), message: 'Array size not as expected'
          assertEquals expected: '[1, a, true, null]', actual: array.toString(), message: 'Array toString not as expected'
        }

        test('ArrayWrapper operations: add, set, get') {
          def array = new ArrayWrapper([])
          assertEquals expected: 0, actual: array.size(), message: 'Array size not as expected'
          array.add('a')
          assertEquals expected: 1, actual: array.size(), message: 'Array size not as expected'
          assertEquals expected: 'a', actual: array.get(0), message: 'Array at index 0 not as expected'
          assertEquals expected: '[a]', actual: array.toString(), message: 'Array toString not as expected'
          array.set(0, 'b')
          assertEquals expected: 1, actual: array.size(), message: 'Array size not as expected'
          assertEquals expected: 'b', actual: array.get(0), message: 'Array at index 0 not as expected'
          assertEquals expected: '[b]', actual: array.toString(), message: 'Array toString not as expected'
          array.add(false)
          array.add(null)
          array.add(0)
          assertEquals expected: 4, actual: array.size(), message: 'Array size not as expected'
          assertEquals expected: 'b', actual: array.get(0), message: 'Array at index 0 not as expected'
          assertEquals expected: false, actual: array.get(1), message: 'Array at index 1 not as expected'
          assertEquals expected: null, actual: array.get(2), message: 'Array at index 2 not as expected'
          assertEquals expected: 0, actual: array.get(3), message: 'Array at index 3 not as expected'
          assertEquals expected: '[b, false, null, 0]', actual: array.toString(), message: 'Array toString not as expected'
        }

      }

      testSuite('ArrayMap') {

        test('Empty ArrayMap') {
          def map = new ArrayMap()
          assertEquals expected: 0, actual: map.size(), message: 'ArrayMap size not as expected'
          assertEquals expected: '{}', actual: map.toString(), message: 'ArrayMap toString not as expected'
        }

        test('new ArrayMap from map') {
          def map = new ArrayMap([a: 1, b: 'B'])
          assertEquals expected: 2, actual: map.size(), message: 'ArrayMap size not as expected'
          assertEquals expected: '{a=1, b=B}', actual: map.toString(), message: 'ArrayMap toString not as expected'
        }

        test('ArrayMap basic operations: put, get') {
          def map = new ArrayMap([:])
          assertEquals expected: 0, actual: map.size(), message: 'ArrayMap size not as expected'
          assertEquals expected: null, actual: map.get('a'), message: 'getting a value by non existing key should return null'
          def old = map.put 'a', 'A'
          assertEquals expected: 1, actual: map.size(), message: 'ArrayMap size not as expected'
          assertEquals expected: 'A', actual: map.get('a'), message: 'value for key "a" not as expected'
          assertEquals expected: null, actual: old, message: 'old value returned from put not as expected'
          assertEquals expected: '{a=A}', actual: map.toString(), message: 'ArrayMap toString not as expected'
          old = map.put 'a', 'AA'
          assertEquals expected: 'AA', actual: map.get('a'), message: 'value for key "a" not as expected'
          assertEquals expected: 'A', actual: old, message: 'old value returned from put not as expected'
          def value = map.get('b', { -> 'B' })
          assertEquals expected: 'B', actual: value, message: 'value for key "b" not as expected - should have been created'
          assertEquals expected: 2, actual: map.size(), message: 'ArrayMap size not as expected'
          assertEquals expected: 'B', actual: map.get('b'), message: 'value for key "b" not as expected'
          assertEquals expected: '{a=AA, b=B}', actual: map.toString(), message: 'ArrayMap toString not as expected'
        }

        test('ArrayMap forEach') {
          def result = ''
          def map = new ArrayMap()
          map.forEach { key, value -> result += ('' + key + value) }
          assertEquals expected: '', actual: result, message: 'forEach result not as expected'
          result = ''
          map = new ArrayMap([a: 1, b: 'B', c: true, d: null])
          map.forEach { key, value -> result += ('' + key + value) }
          assertEquals expected: 'a1bBctruednull', actual: result, message: 'forEach result not as expected'
        }

      }

      testSuite('withDockerEx, waitForEndpoint') {

        test('withDockerEx, waitForEndpoint') {
          def ip
          withDockerEx { dockerEx ->
            //https://hub.docker.com/r/crccheck/hello-world/
            writeFile file: 'test-docker-compose.yml', text: """
---
version: "2.1"
services:
  web-test:
    image: crccheck/hello-world
    ports:
      - "54321:8000"
"""
            def myproj = dockerEx.compose('myproj', [file: 'test-docker-compose.yml'])
            myproj.up().ps()
            ip = myproj.inspectGateway()
            // waitForPort host: ip, port: 54321
            waitForEndpoint url: "http://${ip}:54321"
            def logs = myproj.logs()
            assertNotEquals expected: null, actual: logs, message: 'logs are null'
            myproj.stop().ps()
            def res = sh(script: 'curl $ip:54321', returnStatus: true)
            assertNotEquals expected: 0, actual: res, message: 'curl expected to fail'
            myproj.up()
            waitForEndpoint url: "http://${ip}:54321"
            myproj.archiveLogs()
          }
          def res = sh(script: 'curl $ip:54321', returnStatus: true)
          assertNotEquals expected: 0, actual: res, message: 'curl expected to fail'
        }
      }

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

        test('withRetry - retries:5, all 5 should fail') {
          counter = 0
          doAndExpectError(expectedMessage: 'DUMMY ERROR') {
            withRetry(retries: 5) {
              counter++
              error 'DUMMY ERROR'
            }
          }
          assertEquals actual: counter, expected: 5, message: 'retries should be 5'
        }

        test('withRetry - retries:1, first run should fail') {
          counter = 0
          doAndExpectError(expectedMessage: 'DUMMY ERROR') {
            withRetry(retries: 1) {
              counter++
              error 'DUMMY ERROR'
            }
          }
          assertEquals actual: counter, expected: 1, message: 'retries should be 1'
        }

        test('withRetry - retries:5, first 2 should fail, 3rd should succeed') {
          counter = 0
          withRetry(retries: 5) {
            counter++
            if (counter < 3) error 'DUMMY ERROR'
          }
          assertEquals actual: counter, expected: 3, message: 'retries should be 3'
        }

        test('withRetry - retries:0, illegal argument') {
          counter = 0
          doAndExpectError(expectedMessage: 'withRetry: retries must be positive number: 0') {
            withRetry(retries: 0) {
              counter++
            }
          }
          assertEquals actual: counter, expected: 0, message: 'counter should not have been incremented'
        }

        test('withRetry - retries:-1, illegal argument') {
          counter = 0
          doAndExpectError(expectedMessage: 'withRetry: retries must be positive number: -1') {
            withRetry(retries: -1) {
              counter++
            }
          }
          assertEquals actual: counter, expected: 0, message: 'counter should not have been incremented'
        }
      }

      testSuite('withCredentials') {
        test('withUsernamePassword') {
          withUsernamePassword(credentialsId: 'for-pipeline-utils-test-userpass') { username, password ->
            assertEquals actual: username, expected: 'dummyuser', message: 'username is not as expected'
            assertEquals actual: password, expected: 'dummypassword', message: 'password is not as expected'
          }
        }

        test('withSecretText') {
          withSecretText(credentialsId: 'for-pipeline-utils-test-secret-text') { text ->
            assertEquals actual: text, expected: 'dummy secret text', message: 'secret text is not as expected'
          }
        }

        test('withSecretFile') {
          withSecretFile(credentialsId: 'for-pipeline-utils-test-secret-file') { filepath ->
            def secret = readFile(file: filepath)
            assertEquals actual: secret, expected: 'dummy secret file', message: 'text in secret file is not as expected'
          }
        }
      }
    }
  }
}