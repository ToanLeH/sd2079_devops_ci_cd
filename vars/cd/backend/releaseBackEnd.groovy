#!/usr/bin/env groovy
void call() {
    String name = "backend"
    String buildFolder = "backend"
    //String runtime = "BookStore.API.dll"
    //String publishProject = "src/BookStore.API/BookStore.API.csproj"
    //String baseImage     = "node"
    //String baseTag       = "lts-buster"
    String demoRegistry = "663535708029.dkr.ecr.ap-south-1.amazonaws.com"
    //String checkBranches = "$env.BRANCH_NAME"
    //String[] deployBranches = ['main', 'jenkins']
    //String sonarToken = "sonar-token"
    String awsRegion = "ap-south-1"
    String eksName = "eks-dev"
    String ecrRegistryUrl = "https://663535708029.dkr.ecr.ap-south-1.amazonaws.com"
    String awsCredential = 'aws-credentials'
    String ecrCredential = 'ecr-credentials'
    String k8sCredential = 'ekstest'
    String namespace = "demo"
    //String rununitTest = "dotnet test --no-build -l:trx -c Release -p:DOTNET_RUNTIME_IDENTIFIER=linux-x64 --collect:'XPlat Code Coverage' --verbosity minimal --results-directory ./results"

//========================================================================
//========================================================================

//========================================================================
//========================================================================
    stage ('Prepare Package') {
        script {
            writeFile file: '.ci/service/deployment.yml', text: libraryResource('deploy/eks/service/deployment.yml')
            writeFile file: '.ci/service/service.yml', text: libraryResource('deploy/eks/service/service.yml')
        }
    }

    stage ("Deploy BackEnd To K8S") {
        docker.withRegistry(ecrRegistryUrl, "ecr:${awsRegion}:${awsCredential}") {
            sh "export registry=${demoRegistry}; export appname=${name}; export tag=latest; \
            envsubst < .ci/service/deployment.yml > deployment.yml; envsubst < .ci/service/service.yml > service.yml"
            sh "aws eks --region ${awsRegion} update-kubeconfig --name ${eksName}"
            sh "kubectl apply -f deployment.yml"
            sh "kubectl apply -f service.yml"
        }
    }
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