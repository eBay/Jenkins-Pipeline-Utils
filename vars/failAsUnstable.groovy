/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Wrap a scope and report every error as UNSTABLE
 * @param body the scope to wrap
 */
def call(body) {
    try {
        body()
    } catch (e) {
        currentBuild.result = 'UNSTABLE'
        echo "Finished UNSTABLE: ${e.message}"
    }
}