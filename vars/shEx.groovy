/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

import com.ebay.sd.jenkins.util.ArrayMap

/**
 * Executes a shell script and returns a map with both stdout and stderr, and also the result status
 * @param args the arguments for the call. Includes:
 *        script - (String) the script to execute
 * @return an ArrayMap with the following keys: 'out', 'err', 'status'
 */
def call(args) {
    long ts = System.currentTimeMillis()
    String outFile = "out_$ts".toString()
    String errFile = "err_$ts".toString()
    int status = sh(script: "${args.script} 1> $outFile 2> $errFile", returnStatus: true)
    String out = readFile(file: outFile)
    String err = readFile(file: errFile)
    sh script: "rm $outFile $errFile", returnStatus: true
    def res = new ArrayMap()
    res.put 'out', out
    res.put 'err', err
    res.put 'status', status
    res
}