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
 * A docker network handle
 */
class DockerExNetwork implements Serializable {

    private final def script
    private final DockerEx dockerEx
    final String name
    private boolean created = false

    /**
     * @param dockerEx the docker handle
     * @param name the name of the network
     */
    protected DockerExNetwork(DockerEx dockerEx, String name) {
        this.dockerEx = dockerEx
        this.name = name
        this.script = dockerEx.script
    }

    /**
     * Whether this network was created manually (using the "create" method), or by other means
     */
    boolean wasManuallyCreated() {
        created
    }

    /**
     * Create the network, optionally using custom configuration.
     * <p>
     * Supports the following custom configuration:
     * <ul>
     *     <li>
     *         driver -<br>
     *         the network driver to use (default: "bridge")
     *     </li>
     * </ul>
     * @param config the custom config map
     * @return this network handle
     */
    DockerExNetwork create(Map config = [:]) {
        String driver = config.driver ?: 'bridge'
        script.sh "docker network create --driver $driver $name"
        created = true
        this
    }

    /**
     * Remove this network
     * @return this network handle
     */
    DockerExNetwork remove() {
        script.sh "docker network rm $name"
        this
    }

    /**
     * Inspect this network and return its gateway IP
     */
    String inspectGateway() {
        script.sh(script: "docker network inspect -f '{{range .IPAM.Config}}{{.Gateway}}{{end}}' $name", returnStdout: true).trim().split('/')[0].toString()
    }

    /**
     * Inspect this network and return the IPs of the containers which are members of this network
     * @return a map of container name to its IP
     */
    ArrayMap inspectContainersIps() {
        def networksStr = script.sh(script: "docker network inspect -f '{{range .Containers}}{{.Name}}:{{.IPv4Address}}#{{end}}' $name", returnStdout: true).trim()
        ArrayMap networks = new ArrayMap()
        for (String s in networksStr.split('#')) {
            if (s.length() == 0) continue
            def pair = s.split(':')
            def ip = pair[1].split('/')[0]
            networks.put(pair[0], ip)
        }
        networks
    }
}