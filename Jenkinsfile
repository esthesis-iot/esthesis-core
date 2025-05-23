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
                  fsGroup: 0
                containers:
                - name: esthesis-core-builder
                  image: eddevopsd2/ubuntu-dind:docker24-mvn3.9.6-jdk21-node18.16-go1.23-buildx-helm
                  volumeMounts:
                  - name: maven
                    mountPath: /root/.m2/
                    subPath: esthesis
                  - name: sonar-scanner
                    mountPath: /root/sonar-scanner
                  - name: docker
                    mountPath: /root/.docker
                  - name: jacoco-cli
                    mountPath: /root/jacoco-cli
                  tty: true
                  securityContext:
                    privileged: true
                    runAsUser: 0
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
                - name: jacoco-cli
                  persistentVolumeClaim:
                    claimName: jacoco-cli-nfs-pvc
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
        stage ('Clone Common and Bom Repositories') {
            steps {
                container (name: 'esthesis-core-builder') {
                    withCredentials([usernamePassword(credentialsId: 'Jenkins-Github-token',
                    usernameVariable: 'Username',
                    passwordVariable: 'Password')]){
                        sh '''
                            git config --global user.email "devops-d2@eurodyn.com"
                            git config --global user.name "$Username"
                            git clone https://$Password@github.com/esthesis-iot/esthesis-bom
                            git clone https://$Password@github.com/esthesis-iot/esthesis-common
                        '''
                    }
                }
            }
        }
        stage('Build Bom') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                        cd esthesis-bom
                        mvn clean install
                    '''
                }
            }
        }
        stage('Build Common') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                        cd esthesis-common
                        mvn clean install
                    '''
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
                            withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                            usernameVariable: 'Username',
                            passwordVariable: 'Password')]){
                                sh '''
                                    docker login -u $Username -p $Password docker.io
                                    docker pull redis:7
                                    docker pull mongo:7.0
                                    docker pull testcontainers/sshd:1.2.0
                                    docker pull testcontainers/ryuk:0.11.0
                                    docker pull camunda/zeebe:8.3.1
                                    mvn -f esthesis-core-backend/pom.xml clean install -Pcicd
                                '''
                            }
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
        stage('Generate Aggregate Code Coverage'){
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                    cd esthesis-core-backend

                    CLASSFILES=""
                    SOURCEFILES=""

                    # Find all target/classes directories and add them to --classfiles
                    for dir in $(find . -type d -path "*/target/classes"); do
                        CLASSFILES="$CLASSFILES --classfiles=$dir"
                    done

                    # Find all src/main/java directories and add them to --sourcefiles
                    for dir in $(find . -type d -path "*/src/main/java"); do
                        SOURCEFILES="$SOURCEFILES --sourcefiles=$dir"
                    done

                    # Ensure the coverage directory exists
                    mkdir -p target/coverage

                    # Run the JaCoCo CLI
                    java -jar /root/jacoco-cli/jacococli.jar report target/jacoco.exec \
                        $CLASSFILES \
                        $SOURCEFILES \
                        --html target/coverage/jacoco-report \
                        --xml target/coverage/jacoco.xml \
                        --csv target/coverage/jacoco-report.csv
                    '''
                }
            }
        }
        stage('Sonar Analysis') {
            steps {
                container (name: 'esthesis-core-builder') {
                    withSonarQubeEnv('sonar') {
                        sh '''
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
                    		go install github.com/CycloneDX/cyclonedx-gomod/cmd/cyclonedx-gomod@v1.9.0
                        export PATH=$PATH:$(go env GOPATH)/bin
                        cd esthesis-core-device
                        /root/go/bin/cyclonedx-gomod mod go > go/bom.xml
                    '''
                }
            }
        }
        stage('Post Dependency-Track Analysis for device') {
            steps{
                container (name: 'esthesis-core-builder') {
                    sh '''
                        echo '{"project": "415e6ce0-42b1-44be-b7ba-3b58dbb32b10", "bom": "'"$(cat esthesis-core-device/go/bom.xml | base64 -w 0)"'"}' > payload.json
                    '''
                    sh '''
                        curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                    '''
                }
            }
        }
        stage('Produce bom.json for ui') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                        cd esthesis-core-ui
                        npm install --global @cyclonedx/cdxgen
                        cdxgen -t nodejs -o bom.json
                    '''
                }
            }
        }
        stage('Post Dependency-Track Analysis for ui') {
            steps {
                container (name: 'esthesis-core-builder') {
                    sh '''
                        echo '{"project": "229ec483-35c9-4a98-b904-bc8c5b1d6544", "bom": "'"$(cat esthesis-core-ui/bom.json | base64 -w 0)"'"}' > payload.json
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
                        echo '{"project": "39a4839b-4da4-41be-b576-22d7686e9101", "bom": "'"$(cat esthesis-core-backend/target/bom.xml | base64 -w 0)"'"}' > payload.json
                    '''
                    sh '''
                          curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                    '''
                }
            }
        }
    }
    post {
        success {
            build job: "esthesis-dev-deploy/main", propagate: false, wait: false
        }
        changed {
            emailext subject: '$DEFAULT_SUBJECT',
                body: '$DEFAULT_CONTENT',
                to: '12133724.eurodynlu.onmicrosoft.com@emea.teams.ms'
        }
    }
}
