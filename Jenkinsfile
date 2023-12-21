pipeline {
    agent {
        kubernetes {
            yaml '''
              apiVersion: v1
              kind: Pod
              metadata:
                name: esthesis-v2
                namespace: jenkins
              spec:
                affinity:
                  podAntiAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 50
                      podAffinityTerm:
                        labelSelector:
                          matchExpressions:
                          - key: jenkins/jenkins-jenkins-agent
                            operator: In
                            values:
                            - "true"
                        topologyKey: kubernetes.io/hostname
                securityContext:
                  runAsUser: 0
                  runAsGroup: 0
                containers:
                - name: esthesis-v2-builder
                  image: eddevopsd2/maven-java-npm-docker:mvn3.6.3-jdk15-node16.14.2-docker
                  volumeMounts:
                  - name: maven
                    mountPath: /root/.m2/
                    subPath: esthesis
                  - name: sonar-scanner
                    mountPath: /root/sonar-scanner
                  tty: true
                  securityContext:
                    privileged: true
                    runAsUser: 0
                    fsGroup: 0
                imagePullSecrets:
                - name: regcred
                volumes:
                - name: maven
                  persistentVolumeClaim:
                    claimName: maven-nfs-pvc
                - name: sonar-scanner
                  persistentVolumeClaim:
                    claimName: sonar-scanner-nfs-pvc
            '''
            workspaceVolume persistentVolumeClaimWorkspaceVolume(claimName: 'workspace-nfs-pvc', readOnly: false)
        }
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 3, unit: 'HOURS')
    }
    stages {
        stage('Build') {
            parallel {
                stage('Build esthesis-server') {
                    steps {
                        container (name: 'esthesis-v2-builder') {
                            sh 'mvn -f esthesis-server/pom.xml clean install'
                        }
                    }
                }
                stage('Build esthesis-ui') {
                    steps {
                        container (name: 'esthesis-v2-builder') {
                            sh '''
                                cd esthesis-ui
                                npm install
                                npx ng build --configuration production --output-path=dist
                            '''
                        }
                    }
                }
            }
        }
        stage('Sonar Analysis') {
            steps {
                container (name: 'esthesis-v2-builder') {
                    withSonarQubeEnv('sonar'){
                        sh ' /root/sonar-scanner/sonar-scanner/bin/sonar-scanner -Dsonar.projectVersion="$(mvn -f esthesis-server/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)" -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.token=${SONAR_GLOBAL_KEY} -Dsonar.working.directory="/tmp"'
                    }
                }
            }
        }
        stage('Produce bom.xml'){
            parallel {
                stage('Produce bom.xml for esthesis-server') {
                    steps{
                        container (name: 'esthesis-v2-builder') {
                            sh 'mvn -f esthesis-server/pom.xml org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
                        }
                    }
                }
                stage('Produce bom.xml for esthesis-ui') {
                    steps{
                        container (name: 'esthesis-v2-builder') {
                            sh '''
                                cd esthesis-ui
                                npm install --global @cyclonedx/cyclonedx-npm
                                cyclonedx-npm --ignore-npm-errors --output-format xml --output-file bom.xml
                            '''
                        }
                    }
                }
            }
        }
        stage('Dependency-Track Analysis for esthesis-server') {
            steps{
                container (name: 'esthesis-v2-builder') {
                    sh '''
                        cat > payload.json <<__HERE__
                        {
                          "project": "bebe91b0-5878-44a0-95c9-26b0b225a4ad",
                          "bom": "$(cat esthesis-server/target/bom.xml |base64 -w 0 -)"
                        }
                        __HERE__
                    '''

                    sh '''
                        curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                    '''
                }
            }
        }
        stage('Dependency-Track Analysis for esthesis-ui') {
            steps{
                container (name: 'esthesis-v2-builder') {
                    sh '''
                        cat > payload.json <<__HERE__
                        {
                          "project": "647c1466-5d7b-4953-8220-758dc3c70739",
                          "bom": "$(cat esthesis-ui/bom.xml |base64 -w 0 -)"
                        }
                        __HERE__
                    '''

                    sh '''
                        curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                    '''
                }
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