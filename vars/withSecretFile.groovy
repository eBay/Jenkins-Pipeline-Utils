/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Get a secret file by credentials ID
 * <pre>
 *   withSecretFile(credentialsId: 'the-secret-creds-id') { secretFilepath ->
 *     // Do something with the secret file
 *     def secret = readFile(file: secretFilepath)
 *   }
 * </pre>
 * @param args    the arguments. Required: 'credentialsId'
 * @param closure the closure to call, given a single argument which is the secret file path
 */
def call(Map args, Closure closure) {
  withCredentials([
      file(credentialsId: args.credentialsId, variable: 'SECRET_FILE')
  ]) {
    closure.call(SECRET_FILE)
  }
}