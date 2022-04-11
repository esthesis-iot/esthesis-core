pipeline {
    agent {
        docker {
            image 'eddevopsd2/maven-java-npm-docker:mvn3.6.3-jdk15-npm6.14.4-docker'
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
                stage('Build esthesis-server') {
                    steps {
                       sh 'mvn -f esthesis-server/pom.xml clean install'
                    }
                }
                stage('Build esthesis-ui') {
                    steps {
                        sh '''
                            cd esthesis-ui
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
                    sh '/root/sonar-scanner/bin/sonar-scanner -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_KEY_ESTHESIS_UI}'
                }
            }
        }
        stage('Produce bom.xml'){
            parallel {
                stage('Produce bom.xml for esthesis-server') {
                    steps{
                        sh 'mvn -f esthesis-server/pom.xml org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
                    }
                }
                stage('Produce bom.xml for esthesis-ui') {
                    steps{
                        sh '''
                            cd esthesis-ui
                            npm install -g @cyclonedx/bom
                            cyclonedx-bom -o bom.xml
                        '''
                    }
                }
            }
        }
        stage('Dependency-Track Analysis for esthesis-server')
        {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                      "project": "cd0e48ff-6cbc-490f-82d5-927414a3bda5",
                      "bom": "$(cat esthesis-server/target/bom.xml |base64 -w 0 -)"
                    }
                    __HERE__
                    '''

                sh '''
                    curl -X "PUT" ${DEPENDENCY_TRACK_URL} -H 'Content-Type: application/json' -H 'X-API-Key: '${DEPENDENCY_TRACK_API_KEY} -d @payload.json
                   '''
            }
        }
        stage('Dependency-Track Analysis for esthesis-ui')
        {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                      "project": "e55de6df-15be-47d5-8692-0dee163d6865",
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

