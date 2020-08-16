pipeline {
  agent any
  
  environment {
  REPOSITORY_NAME="${env.SERVER_TYPE == "prod" ? "${PROJECT_NAME}" : "${PROJECT_NAME}-${SERVER_TYPE}"}"
  SLACK_CHANNEL="#test"
 }
     
  stages {
     stage("Set Current Build DisplayName") {
      steps {
        script {
          currentBuild.displayName = "${SERVER_TYPE}_${BRANCH_NAME}_${EXECUTOR}"
        }
        slackSend (channel:"${SLACK_CHANNEL}", color: 'good', message: "@${EXECUTOR} \n\n STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' \n(${env.BUILD_URL})")
      }
     }
     
     stage("Git Checkout") {
        steps {
            checkout([
                $class: 'GitSCM', 
                branches: [[name: '*/${BRANCH_NAME}']], 
                doGenerateSubmoduleConfigurations: false, 
                extensions: [
                    [$class: 'PruneStaleBranch'],
                    [$class: 'CleanCheckout'],
                ], 
                submoduleCfg: [], 
                userRemoteConfigs: [[credentialsId: 'github_nodejs-deploy', url: '${GIT_PROJECT_URL}']]
                ])
            sh 'pwd'
            sh 'ls -lat'
        }
     }
     
     stage('Docker Build') {
        steps {
            echo "[Docker Build]"
            sh "pwd && ls -al"
            sh "docker ps -a"                       
        }
     }
     
     stage("Docker Push") {
         steps {
             echo "[Test Start]"

             
         }
     }        
     
     stage("Unit Test") {
         steps {
             echo "[Test Start]"
             sh "./gradlew clean build -x test --stacktrace"
         }
     }
     
     stage("Static Analysis") {
         steps {
             echo "[Static Analysis]"
         }
     }
     
     stage("Deploy") {
         steps {
             echo "[Deploy]"             
         }
     }
     
  }
  post {
        success {
            slackSend (channel: "${SLACK_CHANNEL}", color: '#00FF00', message: "@${EXECUTOR} \n\n SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' \n(${env.BUILD_URL})")
        }
        failure {
            slackSend (channel: "${SLACK_CHANNEL}", color: '#FF0000', message: "@${EXECUTOR} \n\n FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' \n(${env.BUILD_URL})")
        }
    }
}
