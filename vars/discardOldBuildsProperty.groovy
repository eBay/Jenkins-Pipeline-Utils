/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Job parameter to discard old build.
 * @param args the arguments for the parameter, including:
 *             - daysToKeep - max number of days to keep a historical build run (optional, default: infinite)
 *             - numToKeep  - max number of historical build runs to keep (optional, default: 10)
 */
def call(Map args = [:]) {
    args.daysToKeep = args.daysToKeep ?: -1 // indefinitely
    args.numToKeep = args.numToKeep ?: 10
    [
            $class  : 'BuildDiscarderProperty',
            strategy: [
                    $class       : 'LogRotator',
                    daysToKeepStr: "${args.daysToKeep}".toString(),
                    numToKeepStr : "${args.numToKeep}".toString()
            ]
    ]
}