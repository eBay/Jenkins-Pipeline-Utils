/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Get a secret text by credentials ID
 * <pre>
 *   withSecretText(credentialsId: 'the-secret-creds-id') { secretText ->
 *     // Do something with the secret
 *   }
 * </pre>
 * @param args    the arguments. Required: 'credentialsId'
 * @param closure the closure to call, given a single argument which is the secret text
 */
def call(Map args, Closure closure) {
  withCredentials([
      string(credentialsId: args.credentialsId, variable: 'SECRET_TEXT')
  ]) {
    closure.call(SECRET_TEXT)
  }
}