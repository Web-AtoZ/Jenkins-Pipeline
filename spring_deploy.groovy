pipeline {
  agent any
  
  environment {
  //     REGISTRY_HOST=""
  //     REGISTRY_PORT=""
  REPOSITORY_NAME="${env.SERVER_TYPE == "prod" ? "${PROJECT_NAME}" : "${PROJECT_NAME}-${SERVER_TYPE}"}"
  SLACK_CHANNEL="#deploy"
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
                userRemoteConfigs: [[credentialsId: 'github_spring-deploy', url: '${GIT_PROJECT_URL}']]
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
            sh "chmod +x ./gradlew" 
            sh "./gradlew clean"
            sh "./gradlew build"
            sh "docker build --build-arg JAR_FILE=build/libs/*.jar -t springboot-docker-test ."
            //sh 'docker build -t ${REPOSITORY_NAME}:${TAG} .'
            // sh 'docker build -t ${REPOSITORY_NAME}:${TAG} ./${REPOSITORY_NAME} -f ${WORKSPACE}/${REPOSITORY_NAME}/Dockerfile.server --build-arg BUILD_ARGUMENT_ENV=${SERVER_TYPE}'
        }
     }
     
     stage("Docker Push") {
         steps {
             echo "[Test Start]"

             withCredentials([usernamePassword( credentialsId: 'docker-hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                sh "docker login -u ${USERNAME} -p ${PASSWORD}"
                sh "docker tag springboot-docker-test webatoz/spring-docker:${TAG}"
                sh "docker push webatoz/spring-docker:${TAG}"
            }
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
             println '${PROJECT_NAME}'
             sh "docker ps -q --filter 'name=${PROJECT_NAME}' | grep -q . && docker stop ${PROJECT_NAME} && docker rm ${PROJECT_NAME} || true"
             sh "docker run -p 8080:8080 -p 80:80 -d --name=${PROJECT_NAME} webatoz/${PROJECT_NAME}"
             sh "docker rmi -f \$(docker images -f 'dangling=true' -q) || true"
             sh "docker ps -a"
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
