/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

/**
 * Start an interactive groovy script shell with a total timeout of 10 minutes.
 */
def call() {
  timeout(time: 10, unit: 'MINUTES') {
    echo "[Groovy Shell] ***** Starting interactive Groovy shell *****"
    String gscript = ''
    while (true) {
      gscript = input(id: 'gscript', message: 'Groovy Script:', parameters: [
          [$class: 'TextParameterDefinition',
           description: 'Enter Groovy script to run, "exit" to stop the groovy shell',
           defaultValue: gscript,
           name: 'Groovy Script']
      ])
      if (gscript == 'exit') {
        break
      }
      String scriptFilename = "tmp-script-${System.currentTimeMillis()}.groovy"
      String fullScript = """
            def call() {
                $gscript
            }
            return this
            """
      try {
        writeFile file: scriptFilename, text: fullScript
        def scriptCall = load(scriptFilename)
        def ret = scriptCall()
        echo "[Groovy Shell] Return Value: $ret"
      } catch (e) {
        echo "[Groovy Shell] Error: $e"
      } finally {
        try {
          sh "rm $scriptFilename"
        } catch(e) {
          echo "[Groovy Shell] Failed to delete temporary script file '${scriptFilename}': $e"
        }
      }
    }
    echo "[Groovy Shell] Bye!"
  }
}