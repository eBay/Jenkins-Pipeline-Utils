/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Wait until a given endpoint is responsive or a timeout reached (default: 20s).
 * <p>
 * Example:
 * <pre>
 *   waitForEndpoint url: 'http://acme.com:8080/foo', timeout: [time: 20, unit: 'SECONDS']
 * </pre>
 * </p>
 * @param args the map of arguments, including:
 *             - url -               the endpoint URL
 *             - timeout -           (optional, default: 20sec) the overall timeout settings (time and unit)
 *             - requestTimeoutSec - (default: 3sec) the timeout in seconds for each call to the endpoint
 */
def call(Map args) {
    def url = args.url
    def timeoutSettings = args.timeout ?: [time: 20, unit: 'SECONDS']
    int requestTimeoutSec = args.requestTimeoutSec ?: 3
    timeout(timeoutSettings) {
        waitUntil {
            try {
                sh "curl -s $url --max-time $requestTimeoutSec -o /dev/null"
                return true
            } catch (ignore) { }
            false
        }
    }
}