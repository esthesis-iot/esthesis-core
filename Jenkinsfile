pipeline {
    agent {
        kubernetes {
            yaml '''
              apiVersion: v1
              kind: Pod
              metadata:
                name: esthesis-core
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
                - name: esthesis-core-builder
                  image: eddevopsd2/ubuntu-dind:dind-mvn3.8.5-jdk17-node18.16-go1.20-buildx-helm3.12.1
                  volumeMounts:
                  - name: maven
                    mountPath: /root/.m2/
                    subPath: esthesis
                  - name: sonar-scanner
                    mountPath: /root/sonar-scanner
                  - name: docker
                    mountPath: /root/.docker
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
                - name: docker
                  persistentVolumeClaim:
                    claimName: docker-nfs-pvc
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
    	stage ('Dockerd') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh 'dockerd -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock &> dockerd-logfile &'
                }
            }
        }
        stage ('Builds') {
            parallel {
                stage('Go Build Device') {
                    steps {
                        container (name: 'esthesis-core-builder') {
                            sh '''
                                export PATH=$PATH:/usr/bin/gcc
                                cd esthesis-core-device/go
                                go mod download
                                go build -o esthesis-core-device -ldflags '-linkmode external -w -extldflags "-static"' cmd/main.go
                            '''
                        }
                    }
                }
                stage('Build Server') {
                    steps {
                        container (name: 'esthesis-core-builder') {
                            sh '''
                                [ -d /sys/fs/cgroup/systemd ] || mkdir /sys/fs/cgroup/systemd
                                mount -t cgroup -o none,name=systemd cgroup /sys/fs/cgroup/systemd
                                mvn -f esthesis-core-backend/pom.xml clean install -Pcyclonedx-bom
                            '''
                        }
                    }
                }
                stage('Build Ui') {
                    steps {
                        container (name: 'esthesis-core-builder') {
                            sh '''
                                cd esthesis-core-ui
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
                container (name: 'esthesis-core-builder') {
                    withSonarQubeEnv('sonar') {
                        sh '''
                            cd esthesis-core
                            /root/sonar-scanner/sonar-scanner/bin/sonar-scanner -Dsonar.projectVersion="$(mvn -f esthesis-core-backend/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)" -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.token=${SONAR_GLOBAL_KEY} -Dsonar.working.directory="/tmp"
                        '''
                    }
                }
            }
        }
        stage('Produce bom.xml for device module') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                        cd esthesis-core-device
                        go install github.com/CycloneDX/cyclonedx-gomod/cmd/cyclonedx-gomod@v1.4.0
                        /go/bin/cyclonedx-gomod mod go > go/bom.xml
                    '''
                }
            }
        }
        stage('Produce bom.xml for frontend module') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                        cd esthesis-core-ui
                        npm install --global @cyclonedx/cyclonedx-npm
                        cyclonedx-npm --ignore-npm-errors --output-format xml --output-file bom.xml
                    '''
                }
            }
        }
        stage('Post Dependency-Track Analysis for device') {
            steps{
                container (name: 'esthesis-core-builder') {
                    sh '''
                        cat > payload.json <<__HERE__
                        {
                            "project": "415e6ce0-42b1-44be-b7ba-3b58dbb32b10",
                            "bom": "$(cat esthesis-core-device/go/bom.xml |base64 -w 0 -)"
                        }
                        __HERE__
                    '''
                    sh '''
                        curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                    '''
                }
            }
        }
        stage('Post Dependency-Track Analysis for server') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                          cat > payload.json <<__HERE__
                          {
                            "project": "39a4839b-4da4-41be-b576-22d7686e9101",
                            "bom": "$(cat esthesis-core-backend/target/bom.xml |base64 -w 0 -)"
                          }
                          __HERE__
                    '''

                    sh '''
                          curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                    '''
                }
            }
        }
        stage('Post Dependency-Track Analysis for ui') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                        cat > payload.json <<__HERE__
                        {
                          "project": "229ec483-35c9-4a98-b904-bc8c5b1d6544",
                          "bom": "$(cat esthesis-core-ui/bom.xml |base64 -w 0 -)"
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