pipeline {
    agent {
        docker {
            image 'eddevopsd2/mvn3-jdk13:1.0.0'
            args '-v /root/.m2:/root/.m2 -v /root/sonar-scanner:/root/sonar-scanner'
        }
    }
    stages {
        stage('Build') {
            parallel {
                stage('platform-backend') {
                    steps {
                        sh 'mvn -f esthesis-platform-backend/pom.xml clean install'
                    }
                }
                stage('platform-device') {
                    steps {
                        sh 'mvn -f esthesis-platform-device/pom.xml clean install'
                    }
                }
                stage('platform-ui') {
                    steps {
                        sh '''
                            cd esthesis-platform-ui
                            npm install
                        '''
                    }
                }
            }
        }
        stage('Dependencies Check') {
             steps {
                sh 'mvn -f pom-dependency-check.xml org.owasp:dependency-check-maven:aggregate'
             }
        }
        stage('Sonar Analysis') {
            steps {
                sh '/root/sonar-scanner/bin/sonar-scanner -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_KEY_ESTHESIS_PLATF}'
            }
        }
        stage('Produce bom.xml for backend'){
            steps{
                sh 'mvn -f esthesis-platform-backend/pom.xml org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
            }
        }
        stage('Dependency-Track Analysis for backend')
        {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                      "project": "027dcc1e-f095-41ba-92c8-460eb0b93dbb",
                      "bom": "$(cat esthesis-platform-backend/target/bom.xml |base64 -w 0 -)"
                    }
                    __HERE__
                    '''

                sh '''
                    curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                   '''
            }
        }
        stage('Produce bom.xml for device'){
            steps{
                sh 'mvn -f esthesis-platform-device/pom.xml org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
            }
        }
        stage('Dependency-Track Analysis for device')
        {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                      "project": "3068f979-f3a5-4768-b93f-12a03b059919",
                      "bom": "$(cat esthesis-platform-device/target/bom.xml |base64 -w 0 -)"
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
                        rocketSend avatar: "http://cicd-jenkins.eurodyn.com/static/ff676c77/images/headshot.png", channel: 'esthesis-iot', message: ":white_check_mark: | ${BUILD_URL} \n\nBuild succeeded on branch *${env.BRANCH_NAME}* \nChangelog: ${getChangeString(10)}", rawMessage: true
                } else {
                        rocketSend avatar: "http://cicd-jenkins.eurodyn.com/static/ff676c77/images/headshot.png", channel: 'esthesis-iot', message: ":negative_squared_cross_mark: | ${BUILD_URL} \n\nBuild failed on branch *${env.BRANCH_NAME}* \nChangelog: ${getChangeString(10)}", rawMessage: true
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
