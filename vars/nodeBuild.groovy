#!/usr/bin/env groovy
void call() {
    String name = "backend"
    //String runtime = "BookStore.API.dll"
    //String publishProject = "src/BookStore.API/BookStore.API.csproj"
    String baseImage     = "node"
    String baseTag       = "lts-buster"
    //String demoRegistry = "demotraining.azurecr.io"
    //String checkBranches = "$env.BRANCH_NAME"
    //String[] deployBranches = ['main', 'jenkins']
    //String sonarToken = "sonar-token"
    String ecrRegistryUrl = "https://663535708029.dkr.ecr.ap-south-1.amazonaws.com"
    String ecrCredential = 'aws-credentials'
    String k8sCredential = 'ekstest'
    String namespace = "demo"
    //String rununitTest = "dotnet test --no-build -l:trx -c Release -p:DOTNET_RUNTIME_IDENTIFIER=linux-x64 --collect:'XPlat Code Coverage' --verbosity minimal --results-directory ./results"

//========================================================================
//========================================================================

//========================================================================
//========================================================================

    stage ('Prepare Package') {
        script {
            writeFile file: '.ci/Dockerfile', text: libraryResource('node/Dockerfile')
        }
    }

    stage('SonarQube analysis') {
        echo "Run SonarQube Analysis"
    }

    stage ("Build Solution") {
        docker.build("ecr-toanleh-devops-${name}:${BUILD_NUMBER}", " -f ./.ci/Dockerfile \
        --build-arg BASEIMG=${baseImage} --build-arg IMG_VERSION=${baseTag} ${WORKSPACE}/src/backend") 
    }

    stage ('Run Unit Tests') {
        echo "Run Unit Tests"
    }

    stage ('Run Integration Tests') {
        echo "Run Integration Tests"
    }

    stage ('Process Test Results') {
        echo "Export Test Results"
    }

    // stage ("Publish Package") {
    //     docker.build("${demoRegistry}/demo/${name}:${BUILD_NUMBER}", "--force-rm --no-cache -f ./.ci/Dockerfile.Runtime.API \
    //     --build-arg BASEIMG=demo/${name}-sdk --build-arg IMG_VERSION=${BUILD_NUMBER} \
    //     --build-arg ENTRYPOINT=${runtime} --build-arg PUBLISH_PROJ=${publishProject} --build-arg RUNIMG=${baseImage} --build-arg RUNVER=${baseTag} .")
    // }

    stage ("Push Docker Images") {
        echo "Push Docker Images"
        // withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: acrCredential, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        //     docker.withRegistry("https://${demoRegistry}", acrCredential ) {
        //         sh "docker login ${demoRegistry} -u ${USERNAME} -p ${PASSWORD}"
        //         sh "docker push ${demoRegistry}/demo/${name}:${BUILD_NUMBER}"
        //     }
        // }
        steps {
            script{
                docker.withRegistry(ecrRegistryUrl, ecrCredential) {
                    app.push("${BUILD_NUMBER}")
                    app.push("latest")
                }
            }
        }
    }
    // stage ("Deploy To K8S") {
    //     kubeconfig(credentialsId: 'akstest', serverUrl: '') {
    //         sh "export registry=${demoRegistry}; export appname=${name}; export tag=${BUILD_NUMBER}; \
    //         envsubst < .ci/deployment.yml > deployment.yml; envsubst < .ci/service.yml > service.yml"
    //         sh "kubectl apply -f deployment.yml -n ${namespace}"
    //         sh "kubectl apply -f service.yml -n ${namespace}"
    //     }
    // }
}

//========================================================================
// node CI
// Version: v1.0
// Updated:
//========================================================================
//========================================================================
// Notes:
//
//
//========================================================================