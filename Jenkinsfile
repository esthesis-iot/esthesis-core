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
    }
}
