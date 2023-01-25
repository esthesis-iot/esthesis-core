pipeline {
    agent {
        docker {
            image 'eddevopsd2/maven-java-npm-docker:mvn3.8.5-jdk17-npm8.5.0-docker'
            args '-v /root/.m2/Esthesis:/root/.m2 -v /root/sonar-scanner:/root/sonar-scanner'
        }
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Build') {
            parallel {
                stage('Build server') {
                    steps {
                       sh 'mvn -f server/pom.xml clean install -DskipTests -Pcyclonedx-bom'
                    }
                }
                stage('Build ui') {
                    steps {
                        sh '''
                            cd ui
                            npm install
                            npx ng build --configuration production --output-path=dist
                        '''
                    }
                }
            }
        }
        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('sonar'){
                    sh '/root/sonar-scanner/bin/sonar-scanner -Dsonar.projectVersion="$(mvn -f server/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)" -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_KEY_ESTHESIS_PLATFORM_V3}'
                }
            }
        }
        stage('Produce bom.xml'){
            parallel {
                stage('Produce bom.xml for ui') {
                    steps{
                        sh '''
                            cd ui
                            npm install --global @cyclonedx/cyclonedx-npm
                            cyclonedx-npm --ignore-npm-errors --output-format xml --output-file bom.xml
                        '''
                    }
                }
            }
        }
        stage('Dependency-Track Analysis for server')
        {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                      "project": "b2baacfa-12b5-44b2-972d-d7ef1a8a995e",
                      "bom": "$(cat server/target/bom.xml |base64 -w 0 -)"
                    }
                    __HERE__
                    '''

                sh '''
                    curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                   '''
            }
        }
        stage('Dependency-Track Analysis for ui')
        {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                      "project": "0cfd9e59-15b8-44ff-9b86-290c88efbcb4",
                      "bom": "$(cat ui/bom.xml |base64 -w 0 -)"
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