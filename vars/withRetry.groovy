/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Execute a closure and retry if it fails.
 * <p>
 * Example:
 * <pre>
 * withRetry(retries: 3) {
 *   doSomething()
 * }
 * </pre>
 * </p>
 * @param args the map of arguments
 */
def call(Map args = [:], Closure closure) {
  int retries = args.retries != null ? args.retries : 3
  if (retries <= 0) {
    error "withRetry: retries must be positive number: $retries"
  }
  for (int i = 1; i <= retries; i++) {
    try {
      echo "Retry attempt #$i out of $retries ..."
      def result = closure.call()
      return result
    } catch (e) {
      echo "Retry attempt #$i failed: $e"
      if (i >= retries) {
        throw e
      }
    }
  }
}