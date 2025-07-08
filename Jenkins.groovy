pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        S3_BUCKET = 'java-artifact-store'
    }

    tools {
        maven "MAVEN3"
        jdk "OracleJDK17"
    }

    stages {
        stage('Fetching Code from GitHub') {
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

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Checkstyle Analysis') {
            steps {
                sh 'mvn checkstyle:checkstyle'
            }
        }

        stage('Upload to S3') {
            steps {
            
                    s3Upload(
                        bucket: "${S3_BUCKET}",
                        file: 'target/vprofile-v2.war',
                        path: "builds/myapp-${BUILD_NUMBER}-${ BUILD_TIMESTAMP}.war"
                    )
                
            } 
        }
    }
}
