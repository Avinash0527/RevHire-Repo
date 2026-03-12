pipeline {
    agent any

    environment {
        BACKEND_IP = '18.222.43.85'
        S3_BUCKET = 'revhire-f'
    }

    stages {
        stage('Setup Java 21 Environment') {
            steps {
                sh '''
                if [ ! -d "jdk-21" ]; then
                    echo "Downloading Amazon Corretto 21..."
                    wget -q https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz
                    tar -xzf amazon-corretto-21-x64-linux-jdk.tar.gz
                    mv amazon-corretto-21.* jdk-21
                fi
                '''
            }
        }

        stage('Build Backend') {
            steps {
                dir('RevHire-HiringPlatform') {
                    sh '''
                    export JAVA_HOME=$(pwd)/../jdk-21
                    export PATH=$JAVA_HOME/bin:$PATH
                    java -version
                    mvn clean package -DskipTests
                    '''
                }
            }
        }

        stage('Deploy Backend (EC2)') {
            steps {
                // Uses the SSH key stored globally on the Jenkins Server to securely copy the Jar
                sh 'scp -o StrictHostKeyChecking=no -i /var/lib/jenkins/revhiree-key.pem RevHire-HiringPlatform/target/RevHire-HiringPlatform-0.0.1-SNAPSHOT.jar ec2-user@${BACKEND_IP}:~/'
                
                // Restarts the background Java process on the remote machine
                sh '''
                ssh -o StrictHostKeyChecking=no -i /var/lib/jenkins/revhiree-key.pem ec2-user@${BACKEND_IP} "
                    sudo fuser -k 9090/tcp || true
                    nohup java -jar -Dspring.profiles.active=aws ~/RevHire-HiringPlatform-0.0.1-SNAPSHOT.jar > ~/app.log 2>&1 &
                "
                '''
            }
        }

        stage('Build Frontend') {
            steps {
                dir('RevHire-HiringPlatform--Frontend') {
                    sh 'sed -i "s/GROQ_API_KEY_PLACEHOLDER/${GROQ_API_KEY}/g" src/environments/environment.prod.ts'
                    sh 'npm install'
                    sh 'npm run build -- --configuration production'
                }
            }
        }

        stage('Deploy Frontend (S3)') {
            steps {
                // Uses AWS CLI to sync the built Angular files into S3
                sh 'aws s3 sync RevHire-HiringPlatform--Frontend/dist/revhire-frontend/browser s3://${S3_BUCKET} --delete'
            }
        }
    }
}
