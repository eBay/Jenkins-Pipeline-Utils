/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

package com.ebay.sd.jenkins.docker

/**
 * A docker registry handle
 */
class DockerExRegistry implements Serializable {

    private final def script
    private final DockerEx dockerEx
    final String name

    /**
     * @param dockerEx the docker handle
     * @param name the name of the registry
     */
    protected DockerExRegistry(DockerEx dockerEx, String name) {
        this.dockerEx = dockerEx
        this.name = name
        this.script = dockerEx.script
    }

    /**
     * Login to this registry using a given credentials
     * @param credentialsId the ID of the credentials to use. Currently supports only a username & password credentials
     * @return this registry handle
     */
    DockerExRegistry login(String credentialsId) {
        script.withUsernamePassword(credentialsId: credentialsId) { username, password ->
            script.sh "docker login -u ${username} -p ${password} -e jenkins@ebay.com ${this.name}"
        }
        this
    }

    /**
     * Logout from this registry
     * @return this registry handle
     */
    DockerExRegistry logout() {
        script.sh "docker logout ${this.name}"
        this
    }
}