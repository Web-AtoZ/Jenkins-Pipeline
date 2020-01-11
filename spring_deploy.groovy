pipeline {
  agent any
  
  environment {
  //     REGISTRY_HOST=""
  //     REGISTRY_PORT=""
  REPOSITORY_NAME="${env.SERVER_TYPE == "prod" ? "${PROJECT_NAME}" : "${PROJECT_NAME}-${SERVER_TYPE}"}"
 }

  stages {
     stage("Set Current Build DisplayName") {
      steps {
        script {
          currentBuild.displayName = "${SERVER_TYPE}_${BRANCH_NAME}_${EXECUTOR}"
        }
      }
     }
     
     stage("Git Checkout") {
        steps {
            checkout([
                $class: 'GitSCM', branches: [[name: '*/${BRANCH_NAME}']], 
                doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], 
                userRemoteConfigs: [[credentialsId: 'a3f48a77-c95f-4222-ac9a-7826d9a08961', url: 'https://github.com/Soobinnn/Devops-Start']]
                ])
            sh 'ls -lat'
        }
     }
     
     stage('Docker Build') {
        steps {
            echo "[Docker Build]"
            echo "$(git log --pretty = format : % H origin / master .. $ CI_COMMIT_SHA  | tr ' \ n '  ' , ' )"
            //sh "gradle clean build -x test --stacktrace"
            //sh 'docker build -t ${REPOSITORY_NAME}:${TAG} .'
            // sh 'docker build -t ${REPOSITORY_NAME}:${TAG} ./${REPOSITORY_NAME} -f ${WORKSPACE}/${REPOSITORY_NAME}/Dockerfile.server --build-arg BUILD_ARGUMENT_ENV=${SERVER_TYPE}'
        }
     }
                
     stage("Unit Test") {
         steps {
             echo "[Test Start]"
         }
     }
  }
}
