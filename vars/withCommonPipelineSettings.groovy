/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Wrap a scope and set common pipeline settings, including timestamps in console output and overall timeout
 * @param args arguments for the settings, including:
 *             - timeout - the timeout settings (time and unit) (optional, default: 1 hour)
 * @param body the scope to wrap
 */
def call(Map args = [:], body) {
  timestamps {
    def timeoutArgs = args.timeout ?: [time: 1, unit: 'HOURS']
    timeout(timeoutArgs) {
      body()
    }
  }
}
