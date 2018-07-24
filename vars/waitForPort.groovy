/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Wait until a given port in a given host is responsive or a timeout reached (default: 20s).
 * <p>
 * Example:
 * <pre>
 * waitForPort host: 'the-host', port: 1234, timeout: [time: 20, unit: 'SECONDS']
 * </pre>
 * </p>
 * @param args the map of arguments
 */
def call(Map args) {
    String host = args.host
    int port = args.port
    def timeoutArgs = args.timeout ?: [time: 20, unit: 'SECONDS']
    timeout(timeoutArgs) {
        waitUntil {
            def retCode = sh(script: "nc -z $host $port", returnStatus: true)
            retCode == 0
        }
    }
}