/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

package com.ebay.sd.jenkins.docker

import com.ebay.sd.jenkins.util.ArrayMap

/**
 * A docker compose handle
 */
class DockerExCompose implements Serializable {

    private final DockerEx dockerEx
    private final def script
    private final ArrayMap config
    final String name

    /**
     * @param dockerEx the docker handle
     * @param name the name of the docker compose project
     * @param config (optional) configuration map (supports: file - the path of the docker compose yml file)
     */
    protected DockerExCompose(DockerEx dockerEx, String name, Map config = [:]) {
        this.dockerEx = dockerEx
        this.script = dockerEx.script
        this.name = name
        this.config = new ArrayMap(config)
    }

    /**
     * Start the docker composition (calls: docker-compose up -d)
     * @return this handle
     */
    DockerExCompose up() {
        script.sh "$dockerComposeCmd up -d"
        this
    }

    /**
     * Print (echo) the docker composition status (calls: docker-compose ps)
     * @return this handle
     */
    DockerExCompose ps() {
        script.sh "$dockerComposeCmd ps"
        this
    }

    /**
     * Tear down the docker composition (calls: docker-compose down)
     * @return this handle
     */
    DockerExCompose down() {
        script.sh "$dockerComposeCmd down"
        this
    }

    /**
     * Stop the docker composition (calls: docker-compose stop)
     * @return this handle
     */
    DockerExCompose stop() {
        script.sh "$dockerComposeCmd stop"
        this
    }

    /**
     * Inspect the gateway IP of this docker composition. Requires the project's network to exist.
     * @return the gateway IP of the project
     */
    String inspectGateway() {
        defaultNetwork.inspectGateway()
    }

    /**
     * Inspect the IPs of the containers in this docker composition. Requires the project's network to exist.
     * @return a map of container name to container IP
     */
    ArrayMap inspectContainersIps() {
        defaultNetwork.inspectContainersIps()
    }

    /**
     * Get the default network of this project
     * @return the network handle
     */
    DockerExNetwork getDefaultNetwork() {
        dockerEx.network("${name}_default".toString())
    }

    /**
     * Get the logs from the composition
     * @return the text from all the logs
     */
    String logs() {
        script.sh script: "$dockerComposeCmd logs --no-color", returnStdout: true
    }

    /**
     * Read the logs from the composition and archive it in a file - to be available when the build run ends
     * @return this handle
     */
    DockerExCompose archiveLogs() {
        String logs = this.logs()
        String filename = "${name}-docker-compose.log".toString()
        script.writeFile file: filename, text: logs
        script.archiveArtifacts artifacts: filename
        this
    }

    private String getDockerComposeCmd() {
        "docker-compose --project-name $name $fileArg".toString()
    }

    private String getFileArg() {
        def file = config.get('file')
        file ? "--file ${file}" : ''
    }
}