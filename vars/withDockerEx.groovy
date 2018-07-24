/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

import com.ebay.sd.jenkins.docker.DockerEx

/**
 * Do something with a DockerEx handle
 * @param body the scope to do, a closure which accepts a single DockerEx arguments
 */
def call(body) {
    DockerEx dockerEx = new DockerEx(this)
    try {
        body(dockerEx)
    } finally {
        dockerEx.close()
    }
}