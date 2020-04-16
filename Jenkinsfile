pipeline {
    agent {
        docker {
            image 'imousmoutis/maven3-jdk8-dind:1.0.3'
            args '-v /root/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Depencencies Check') {
             steps {
                sh 'mvn org.owasp:dependency-check-maven:aggregate'
             }
        }
        stage('Sonar Analysis') {
            steps {
                sh 'mvn sonar:sonar -Dsonar.projectName=Esthesis-Platform -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_KEY_ESTHESIS_PLATF}'
            }
        }
        stage('Produce bom.xml'){
            steps{
                sh 'mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
            }
        }
        stage('Dependency-Track Analysis')
        {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                      "project": "027dcc1e-f095-41ba-92c8-460eb0b93dbb",
                      "bom": "$(cat target/bom.xml |base64 -w 0 -)"
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
