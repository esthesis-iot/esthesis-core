pipeline {
    agent {
        docker {
          image 'eddevopsd2/maven-java-npm-docker:mvn3.8.5-jdk17-node18.16-go1.20-docker'
          args '-v /root/.m2/Esthesis:/root/.m2 -v /root/sonar-scanner:/root/sonar-scanner -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage ('Builds') {
            parallel {
                stage('Go Build Device') {
                    steps {
                        sh '''
                            cd esthesis-core/esthesis-core-device/go
                            go mod download
                            go build -o esthesis-agent -ldflags '-linkmode external -w -extldflags "-static"' cmd/main.go
                        '''
                    }
                }
                stage('Build Server') {
                    steps {
                        sh 'mvn -f esthesis-core/esthesis-core-backend/pom.xml clean install -Pcyclonedx-bom'
                    }
                }
                stage('Build Ui') {
                    steps {
                        sh '''
                            cd esthesis-core/esthesis-core-ui
                            npm install
                            npx ng build --configuration production --output-path=dist
                        '''
                    }
                }
            }
        }
        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh '''
                        cd esthesis-core
                        /root/sonar-scanner/bin/sonar-scanner -Dsonar.projectVersion="$(mvn -f esthesis-core-backend/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)" -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_KEY_ESTHESIS_PLATFORM_V3}
                    '''
                }
            }
        }
        stage('Produce bom.xml for device module') {
            steps {
                sh '''
                    cd esthesis-core/esthesis-core-device
                    go install github.com/CycloneDX/cyclonedx-gomod/cmd/cyclonedx-gomod@v1.4.0
                    cyclonedx-gomod mod go > go/bom.xml
                '''
            }
        }
        stage('Produce bom.xml for frontend module') {
            steps {
                sh '''
                    cd esthesis-core/esthesis-core-ui
                    npm install --global @cyclonedx/cyclonedx-npm
                    cyclonedx-npm --ignore-npm-errors --output-format xml --output-file bom.xml
                '''
            }
        }
        stage('Post Dependency-Track Analysis for device') {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                        "project": "7b2bd0e6-6b00-4d20-8147-b102a23ac56f",
                        "bom": "$(cat esthesis-core/esthesis-core-device/go/bom.xml |base64 -w 0 -)"
                    }
                    __HERE__
                '''
                sh '''
                    curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                '''
            }
        }
        stage('Post Dependency-Track Analysis for server')
        {
            steps {
                sh '''
                      cat > payload.json <<__HERE__
                      {
                        "project": "cd0e48ff-6cbc-490f-82d5-927414a3bda5",
                        "bom": "$(cat esthesis-core/esthesis-core-backend/target/bom.xml |base64 -w 0 -)"
                      }
                      __HERE__
                '''

                sh '''
                      curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                '''
            }
        }
        stage('Post Dependency-Track Analysis for ui')
        {
            steps {
                sh '''
                    cat > payload.json <<__HERE__
                    {
                      "project": "e55de6df-15be-47d5-8692-0dee163d6865",
                      "bom": "$(cat esthesis-core/esthesis-core-ui/bom.xml |base64 -w 0 -)"
                    }
                    __HERE__
                '''

                sh '''
                    curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                '''
            }
        }
    }
    post {
        changed {
            script {
                if (currentBuild.result == 'SUCCESS') {
                        rocketSend avatar: "http://d2-np.eurodyn.com/jenkins/jenkins.png", channel: 'esthesis-iot', message: ":white_check_mark: | ${BUILD_URL} \n\nBuild succeeded on branch *${env.BRANCH_NAME}* \nChangelog: ${getChangeString(10)}", rawMessage: true
                } else {
                        rocketSend avatar: "http://d2-np.eurodyn.com/jenkins/jenkins.png", channel: 'esthesis-iot', message: ":x: | ${BUILD_URL} \n\nBuild failed on branch *${env.BRANCH_NAME}* \nChangelog: ${getChangeString(10)}", rawMessage: true
                }
            }
        }
    }
}
@NonCPS
def getChangeString(maxMessages) {
    MAX_MSG_LEN = 100
    def changeString = ""

    def changeLogSets = currentBuild.changeSets

    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length && i + j < maxMessages; j++) {
            def entry = entries[j]
            truncated_msg = entry.msg.take(MAX_MSG_LEN)
            changeString += "*${truncated_msg}* _by author ${entry.author}_\n"
        }
    }

    if (!changeString) {
        changeString = " There have not been any changes since the last build"
    }

    return changeString
}