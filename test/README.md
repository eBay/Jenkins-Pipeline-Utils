# Jenkins-Pipeline-Utils Tests

## Prerequisites

### Node

A builder node labeled `jenkins-pipeline-utils-test-1.x` with the following prerequisites:

| Name             | Minimal Tested Version |
| ---------------- | ---------------------- |
| `docker`         | client: `1.10.3`, server: `1.12.6` |
| `docker-compose` | `1.17.0` |
| `curl`           | |

### Credentials

| ID                                    | Type               | Content |
| ------------------------------------- | ------------------ | ------- |
| `for-pipeline-utils-test-userpass`    | `UsernamePassword` | username: `dummyuser`, password: `dummypassword` |
| `for-pipeline-utils-test-secret-text` | `SecretText`       | ```dummy secret text``` |
| `for-pipeline-utils-test-secret-file` | `SecretFile`       | ```dummy secret file``` |
