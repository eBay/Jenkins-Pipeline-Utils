/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Get username and password from Jenkins credentials and pass them to the given closure as arguments.
 * <p>
 * Example:
 * <pre>
 * withUsernamePassword(credentialsId: 'the-creds-id') { username, password ->
 *   do something...
 * }
 * </pre>
 * </p>
 * @param args the arguments
 * @param closure the closure to call, expected to get 2 arguments: (username, password)
 */
def call(Map args, Closure closure) {
    withCredentials([[
            $class          : 'UsernamePasswordMultiBinding',
            credentialsId   : args.credentialsId,
            usernameVariable: 'USERNAME',
            passwordVariable: 'PASSWORD']]) {
        closure.call(USERNAME, PASSWORD)
    }
}