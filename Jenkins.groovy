pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        S3_BUCKET = 'java-artifact-store'
        SNS_TOPIC_ARN = 'arn:aws:sns:us-east-1:504354966349:Jenkins-bulid-sns'
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

    post {
        success {
            script {
                withAWS(region: "${AWS_REGION}") {
                    snsPublish(
                        topicArn: "${SNS_TOPIC_ARN}",
                        message: "Your Jenkins Job '${env.JOB_NAME}' #${env.BUILD_NUMBER} succeeded at ${BULID_TIMESTAMP}",
                        subject: "Jenkins SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                    )
                }
            }
        }

        failure {
            script {
                withAWS(region: "${AWS_REGION}") {
                    snsPublish(
                        topicArn: "${SNS_TOPIC_ARN}",
                        message: "Your Jenkins Job '${env.JOB_NAME}' #${env.BUILD_NUMBER} failed at ${BUILD_TIMESTAMP}",
                        subject: "Jenkins FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                    )
                }
            }
        }
    }
}
