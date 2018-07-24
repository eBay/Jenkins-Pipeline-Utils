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
 * A docker handle.
 * <p>
 * Provides methods to create various docker related handles, such as: compose, network, registry.<br>
 * Manages the created handles, once this docker handle is closed, all its managed handles are also closed.
 * </p>
 * @see DockerExRegistry
 * @see DockerExCompose
 * @see DockerExNetword
 * @see #close
 */
class DockerEx implements Serializable {

    protected final def script
    private final ArrayMap registries = new ArrayMap()
    private final ArrayMap compositions = new ArrayMap()
    private final ArrayMap networks = new ArrayMap()

    DockerEx(script) {
        this.script = script
    }

    /**
     * Get a handle for a registry with a given name
     * @param name the name of the registry
     */
    DockerExRegistry registry(String name) {
        DockerEx dockerEx = this
        registries.get(name, { -> new DockerExRegistry(dockerEx, name) })
    }

    /**
     * Get a handle for a docker composition with a given name and optionally a config.
     * <p>
     * Gets a name for the docker composition project and optionally a configuration map.
     * The supported configuration includes:
     * <ul>
     *     <li>
     *         file - <br>
     *         the path (relative or absolute) to the docker compose yml file
     *     </li>
     * </ul>
     * </p>
     * <p>
     * Usage example:
     * <pre>
     * def myCompose = dockerEx.compose('myCompose', [file: 'path/to/my-compose.yml'])
     * </pre>
     * </p>
     * @param name the name of the network
     * @param config (optional) specific docker-compose configuration map.
     */
    DockerExCompose compose(String name, Map config = [:]) {
        DockerEx dockerEx = this
        compositions.get(name, { -> new DockerExCompose(dockerEx, name, config) })
    }

    /**
     * Get a handle for a network with a given name
     * @param name the name of the network
     */
    DockerExNetwork network(String name) {
        DockerEx dockerEx = this
        networks.get(name, { -> new DockerExNetwork(dockerEx, name) })
    }

    /**
     * Close this docker handle, together with all its managed related handles (registries, networks, compositions, etc.)
     */
    void close() {
        registries.forEach { _, registry ->
            script.echo "Logging out from docker registry: ${registry.name}"
            try { registry.logout() } catch (e) { script.echo "Could not logout from registry '${registry.name}': ${e}" }
        }

        compositions.forEach { _, compose ->
            script.echo "Tearing down docker compose: ${compose.name}"
            try { compose.down() } catch (e) { script.echo "Could not destroy composition '${compose.name}': ${e}" }
        }

        networks.forEach { _, network ->
            if (network.wasManuallyCreated()) {
                script.echo "Removing docker network: ${network.name}"
                try { network.remove() } catch (e) { script.echo "Could not remove network '${network.name}': ${e}" }
            }
        }
    }
}