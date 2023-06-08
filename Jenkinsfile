pipeline {
    agent {
        docker {
          image 'eddevopsd2/maven-java-npm-docker:mvn3.8.5-jdk17-node14.21.3-go1.20-docker'
          args '-v /root/.m2/Esthesis:/root/.m2 -v /root/sonar-scanner:/root/sonar-scanner'
        }
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Go Build Device') {
          steps {
            sh '''
                cd estesis-core/estesis-core-device/go
                go mod download
                go build -o esthesis-agent -ldflags '-linkmode external -w -extldflags "-static"' cmd/main.go
            '''
          }
        }
        stage('Build Server') {
          steps {
            sh 'mvn -f esthesis-core/esthesis-core-backend/pom.xml clean install -DskipTests -Pcyclonedx-bom'
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
        stage('Sonar Analysis') {
          steps {
            withSonarQubeEnv('sonar') {
              sh 'cd estesis-core && /root/sonar-scanner/bin/sonar-scanner -Dsonar.projectVersion="$(mvn -f server/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)" -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_KEY_ESTHESIS_PLATFORM_V3}'
            }
          }
        }
        stage('Produce bom.xml for Device module') {
            steps {
                sh '''
                cd esthesis-core/esthesis-core-device/go
                go install github.com/CycloneDX/cyclonedx-gomod/cmd/cyclonedx-gomod@v1.3.0
                cyclonedx-gomod app go > go/bom.xml
                '''
            }
        }
        stage('Produce bom.xml for frontend module') {
          parallel {
            stage('Produce bom.xml for ui') {
              steps {
                sh '''
                    cd esthesis-core/esthesis-core-ui
                    npm install --global @cyclonedx/cyclonedx-npm
                    cyclonedx-npm --ignore-npm-errors --output-format xml --output-file bom.xml
                '''
              }
            }
          }
        }
        stage('Post Dependency-Track Analysis for device') {
            steps{
                sh '''
                    cat > payload.json <<__HERE__
                    {
                        "project": "9bbb88b1-ac8b-4657-a9e5-ecdf536a8f67",
                        "bom": "$(cat go/bom.xml |base64 -w 0 -)"
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
                "project": "b2baacfa-12b5-44b2-972d-d7ef1a8a995e",
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
                "project": "0cfd9e59-15b8-44ff-9b86-290c88efbcb4",
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
}