/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Start an interactive shell with a total timeout of 10 minutes.
 */
def call() {
    timeout(time: 10, unit: 'MINUTES') {
        echo "[Shell] ***** Starting interactive shell *****"
        String cmd = ''
        while (true) {
            cmd = input(id: 'cmd', message: 'Command:', parameters: [
                    [$class: 'TextParameterDefinition',
                     description: 'Enter shell command to run, "exit" to stop the shell',
                     defaultValue: cmd,
                     name: 'cmd']
            ])
            if (cmd == 'exit') {
                break
            }
            def ret = sh(script: cmd, returnStatus: true)
            echo "[Shell] Return Code: $ret"
        }
        echo "[Shell] Bye!"
    }
}