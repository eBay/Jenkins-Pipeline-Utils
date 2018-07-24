# Jenkins Pipeline Utilities
![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)
  
Global Jenkins Pipeline Library with common utilities.  
For basic instructions, see [Usage](#usage), [Prerequisites](#prerequisites) and [Configuration](#configuration) sections below.

Defines the following global [steps](#steps):
* [`withCommonPipelineSettings`](#withcommonpipelinesettings)
* [`withDockerEx`](#withdockerex)
* [`waitForPort`](#waitforport)
* [`waitForEndpoint`](#waitforendpoint)
* [`withUsernamePassword`](#withusernamepassword)
* [`withSecretText`](#withsecrettext)
* [`withSecretFile`](#withsecretfile)
* [`failAsUnstable`](#failasunstable)
* [`shEx`](#shex)
* [`interactiveShell`](#interactiveshell)
* [`interactiveGroovyShell`](#interactivegroovyshell)

Also defines the following [properties](#properties):
* [`discardOldBuildsProperty`](#discardoldbuildsproperty)

For utilities for testing global pipeline libraries, see: [TESTING.md](TESTING.md)

## Usage

**Basic `Jenkinsfile` Example:**
```groovy
@Library('pipeline-utils@1.x') _

properties([
  discardOldBuildsProperty()
])

withCommonPipelineSettings {
  node('node-with-docker-compose') {
    stage('Setup') {
      checkout scm
    }
        
    stage('Build') {
      // Build ...
    }
        
    stage('Unit Tests') {
      // Unit tests ...
    }
        
    stage('Publish') {
      // Publish ...
    }
        
    failAsUnstable {
      withDockerEx { dockerEx ->
        def sidecarIp
        stage('Start Sidecars') {
          def sidecar = dockerEx.compose('sidecar', file: 'path/to/sidecar.yml')
          sidecar.up().ps()
          sidecarIp = sidecar.inspectGateway()
          echo "Sidecar IP: $sidecarIp"
        }
                
        stage('Integration Tests') {
          // Run integration tests using the sidecar IP
        }
      }
    }
  }
}
```

### Prerequisites
1. Only Linux OS based Jenkins nodes are supported
1. Some steps require tools to be pre-installed (e.g. `docker`, `docker-compose`, `curl`, `nc`, etc.)

### Configuration
To use this Global Jenkins Pipeline Library you first need to add it to your Jenkins instance:
1. Go to: "Manage Jenkins" -> "Configure System" -> "Global Pipeline Library"  
1. Add a new library with:  
**Name:** `pipeline-utils` (or any other name)  
**Default version:** `<empty>`  
**Load implicitly:** `[ ]` (unchecked)  
**Allow override:** `[v]`  
**Source Code Management:** Point to this repository and give credentials

After that, you can include this library from your jenkins pipeline script:
```groovy
@Library('pipeline-utils@1.x') _
```  

### Steps

#### `withCommonPipelineSettings`
Setup common pipeline settings, including `timestamps` and `timeout`.

**Arguments:**  
* `timeout` - (Map) the timeout settings. See [basic timeout step documentation](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#code-timeout-code-enforce-time-limit) for more details.  
  (optional, default: `[time: 1, unit: 'HOURS']`)

**Example:**  
```groovy
withCommonPipelineSettings {
  // Do your magic
}
``` 

#### `withDockerEx`
Start a docker extension context.
  
**Example:**
```groovy
withDockerEx { dockerEx ->
  // Assuming there is a docker compose file named 'my-project.yml' in the current directory 
  def myProj = dockerEx.compose('my-project', [file: 'my-project.yml'])
  // Spin the composition up and print its status
  myProj.up().ps()
  // Get the public gateway IP of the composition
  def myProjIp = myProj.inspectGateway()
  // Wait for a specific port in the composition to be responsive
  waitForPort host: myProjIp, port: 8080
  
  // Do something with the composition ...
  
  // Stop and remove the composition
  myProj.down()
}
```
*Note-*  
`withDockerEx` closes its context automatically when the closure ends 
(this includes stopping and removing the composition) 
so calling `myProj.down()` explicitly is not really necessary.

**Requirements:**
* `docker`
* `docker-compose`

#### `waitForPort`
Wait for a port to be responsive or for a timeout.

**Arguments:**
* `host` - (String) the host or IP to check
* `port` - (int) the port to check
* `timeout` - (Map) the timeout settings. See [basic timeout step documentation](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#code-timeout-code-enforce-time-limit) for more details.  
  (optional, default: `[time: 20, unit: 'SECONDS']`)
  
**Example:**
```groovy
waitForPort host: 'the-host', port: 1234
```

**Requirements:**
* `nc` (NetCat)

#### `waitForEndpoint`
Wait for an HTTP endpoint to be responsive. 
Issues a GET request to a given URL endpoint and waits until it responds (no matter the response status). 

**Arguments:**
* `url` - (String) the URL endpoint to check
* `timeout` - (Map) the timeout settings. See [basic timeout step documentation](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#code-timeout-code-enforce-time-limit) for more details.  
  (optional, default: `[time: 20, unit: 'SECONDS']`)
* `requestTimeoutSec` - (int) the max time in seconds for each attempt.  
  (optional, default: `3`)

**Example:**
```groovy
waitForEndpoint url: 'http://www.acme.com'
```

**Requirements:**
* `curl`

#### `withUsernamePassword`
Get username and password from a stored jenkins credentials and use them.

**Arguments:**
* `credentialsId` - (String) the ID of the credentials to use

**Example:**
```groovy
withUsernamePassword(credentialsId: 'the-creds-id') { username, password ->
  // Do something with the username and password...
}
```

#### `withSecretText`
Get a secret text by credentials ID and use it.

**Arguments:**
* `credentialsId` - (String) the ID of the credentials to use

**Example:**
```groovy
withSecretText(credentialsId: 'the-secret-creds-id') { secretText ->
  // Do something with the secret
}
```

#### `withSecretFile`
Get a secret file path by credentials ID and use it.

**Arguments:**
* `credentialsId` - (String) the ID of the credentials to use

**Example:**
```groovy
withSecretFile(credentialsId: 'the-secret-creds-id') { secretFilepath ->
  // Do something with the secret file
  def secret = readFile(file: secretFilepath)
}
```

#### `failAsUnstable`
Defines a scope in which in case of failure it is reported as `UNSTABLE`.

**Example:**
```groovy
failAsUnstable {
  // Do something...
}
``` 

*Note-* This step can be used only in the end of the pipeline. 
Further steps after the `failAsUnstable` scope is closed will be executed and hence might affect the result status.

#### `shEx`
Executes a shell script and returns a map with both stdout and stderr, and also the result status.  
Returns an `ArrayMap` with the following keys: 'out' (String), 'err' (String), 'status' (int).

**Arguments:**
* `script` - (String) the script to execute

**Example:**
```groovy
def result = shEx(script: 'java -version')
echo "out: ${result.get('out')}, err: ${result.get('err')}, status: ${result.get('status')}"
```

#### `withRetry`
Execute a code block and retry in case of an error.  
If all attempts fail, the error of the last attempt is thrown.

**Arguments:**  
* `retries` - (int) the number of retry attempts  
  (optional, default: `3`)

**Example:**
```groovy
withRetry(retries: 5) {
  // Do something that might fail
}
```

#### `interactiveShell`
Starts an interactive shell loop where the build execution is paused and the user can interactively 
execute shell command (implemented using the `sh` basic step). 
To break the loop enter the `exit` command. The interactive shell has a total timeout of 10 minutes.  
*Note-* This step shall not be part of a real pipeline. 
Its goal is to assist with pipeline development and debugging.

**Example:**
```groovy
interactiveShell()
//Pauses the pipeline execution and waits for human interaction
```

#### `interactiveGroovyShell`
Starts an interactive groovy shell loop where the build execution is paused and the user can interactively 
execute groovy script (implemented using the `load` basic step). 
To break the loop enter `exit` as the script. The interactive groovy shell has a total timeout of 10 minutes.  
*Note-* This step shall not be part of a real pipeline. 
Its goal is to assist with pipeline development and debugging.  
**LIMITATION:**  
When using "Replay" after using this step, the scripts of the previous run are used.
Their content is available in the "Replay" page and can be modified from there. 
But pay attention that the content in the "Replay" page overrides the input in the actual run.   
The easiest way to workaround it is to use "Replay" and add `return` in the beginning of the script. 
This clears the cached loaded scripts for the next replay.     

**Example:**
```groovy
interactiveGroovyShell()
//Pauses the pipeline execution and waits for human interaction
```

### Properties

Following properties can be used in the `properties([...])` declaration.  
For example:
```groovy
properties([
  discardOldBuildsProperty()
])
```

#### `discardOldBuildsProperty`

Configure the job to discard old builds according to the following parameters:
* `numToKeep` - (integer) the maximum number of builds to keep for the job.  
  (optional, default: `10`)
* `daysToKeep` - (integer) the maximum number of days to keep a build.  
  (optional, default: `-1`, i.e. infinite)

**Example:**  
```groovy
properties([
  discardOldBuildsProperty(numToKeep: 20, daysToKeep: 30)
])
```   

  
----
  
License  

Copyright 2018 eBay Inc.  
Developer: [Yinon Avraham](https://github.com/yinonavraham)

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
