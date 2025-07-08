pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        S3_BUCKET = 'java-artifact-store '
    }
    tools {
        maven "MAVEN3"
        jdk "OracleJDK17"
    }

    stages {
        stage('fetching code from github'){
            steps {
                git branch: 'main', url: 'https://github.com/alfinalbert10/Jenkins-pratice.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
            post {
                success {
                    echo "Now Archiving."
                    archiveArtifacts artifacts: '**/*.war'
                }
            }
        }
        stage('Test'){
            steps {
                sh 'mvn test'
            }

        }

        stage('Checkstyle Analysis'){
            steps {
                sh 'mvn checkstyle:checkstyle'
            }
        }

    }
}
