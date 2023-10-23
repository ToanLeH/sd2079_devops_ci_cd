#!/usr/bin/env groovy
void call() {
    String name = "frontend"
    String buildFolder = "frontend"
    //String runtime = "BookStore.API.dll"
    //String publishProject = "src/BookStore.API/BookStore.API.csproj"
    String baseImage     = "node"
    String baseTag       = "lts-buster"
    String demoRegistry = "663535708029.dkr.ecr.ap-south-1.amazonaws.com"
    //String checkBranches = "$env.BRANCH_NAME"
    //String[] deployBranches = ['main', 'jenkins']
    //String sonarToken = "sonar-token"
    String awsRegion = "ap-south-1"
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
            writeFile file: '.ci/Dockerfile', text: libraryResource('node/Dockerfile')
        }
    }

    stage('SonarQube analysis') {
        echo "Run SonarQube Analysis"
    }

    stage ("Build Solution") {
        docker.build("ecr-toanleh-devops-${name}:${BUILD_NUMBER}", " -f ./.ci/Dockerfile \
        --build-arg BASEIMG=${baseImage} --build-arg IMG_VERSION=${baseTag} ${WORKSPACE}/src/${buildFolder}") 
    }

    stage ("Trivy Scan") {
        script {
            // Install trivy
            //sh 'curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin v0.18.3'
            //sh 'curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl > html.tpl'

            // Scan all vuln levels
            sh "mkdir -p reports"
            sh "wget https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl"
            sh "trivy filesystem --ignore-unfixed --vuln-type os,library --format template --template @./html.tpl -o reports/nodjs-scan.html ${WORKSPACE}/src/${buildFolder}"
            publishHTML target : [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'reports',
                reportFiles: 'nodjs-scan.html',
                reportName: 'Trivy Scan',
                reportTitles: 'Trivy Scan'
            ]

            // Scan again and fail on CRITICAL vulns
            sh "trivy filesystem --ignore-unfixed --vuln-type os,library --exit-code 1 --severity CRITICAL ${WORKSPACE}/src/${buildFolder}"

        }
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

        // script{
        //     docker.withRegistry(ecrRegistryUrl, 'aws-credentials') {
        //         app.push("${BUILD_NUMBER}")
        //         app.push("latest")
        //     }
        // }

        //withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: awsCredential, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        // script{  
        //   sh "aws ecr get-login-password --region ${awsRegion} | docker login --username AWS --password-stdin ${demoRegistry}"
        //   sh "docker tag ecr-toanleh-devops-${name}:${BUILD_NUMBER} ${demoRegistry}/${name}:${BUILD_NUMBER}"
        //   sh "docker push ${demoRegistry}/ecr-toanleh-devops-${name}:${BUILD_NUMBER}"
        // }

        // withAWS(credentials: awsCredential, region: awsRegion) {
        //   sh "aws ecr get-login-password --region ${awsRegion} | docker login --username AWS --password-stdin ${demoRegistry}"
        //   sh "docker tag ecr-toanleh-devops-${name}:${BUILD_NUMBER} ${demoRegistry}/${name}:${BUILD_NUMBER}"
        //   sh "docker push ${demoRegistry}/ecr-toanleh-devops-${name}:${BUILD_NUMBER}"
        // }

        //
        docker.withRegistry(ecrRegistryUrl, "ecr:${awsRegion}:${awsCredential}") {
            //docker.image("your-image-name").push()
            sh "docker tag ecr-toanleh-devops-${name}:${BUILD_NUMBER} ${demoRegistry}/ecr-toanleh-devops-${name}:${BUILD_NUMBER}"
            sh "docker push ${demoRegistry}/ecr-toanleh-devops-${name}:${BUILD_NUMBER}"

            sh "docker tag ${demoRegistry}/ecr-toanleh-devops-${name}:${BUILD_NUMBER} ${demoRegistry}/ecr-toanleh-devops-${name}:latest"
            sh "docker push ${demoRegistry}/ecr-toanleh-devops-${name}:latest"
        }

    //    sh(label: 'ECR login and docker push', script:
    //      '''
    //      #!/bin/bash
         
    //        echo "Authenticate with ECR"
    //         set +x # Don't echo credentials from the login command!
    //         echo "login ECR"
    //         eval $(aws ecr get-login --region "ap-south-1" --no-include-email)
    //         # Enable Debug and Exit immediately 
    //         set -xe
    //         #two push one for master tag other is git commit ID
    //         docker tag ecr-toanleh-devops-backend:$BUILD_NUMBER 663535708029.dkr.ecr.ap-south-1.amazonaws.com/backend:$BUILD_NUMBER
    //         docker push 663535708029.dkr.ecr.ap-south-1.amazonaws.com/backend:$BUILD_NUMBER
    //         docker tag 663535708029.dkr.ecr.ap-south-1.amazonaws.com/backend:$BUILD_NUMBER 663535708029.dkr.ecr.ap-south-1.amazonaws.com/backend:latest
    //         docker push 663535708029.dkr.ecr.ap-south-1.amazonaws.com/backend:latest
    //      '''.stripIndent())
    }
    // stage ("Deploy To K8S") {
    //     // kubeconfig(credentialsId: 'eks', serverUrl: '') {
    //     //     //sh "export registry=${demoRegistry}; export appname=${name}; export tag=${BUILD_NUMBER}; \
    //     //     //envsubst < .ci/deployment.yml > deployment.yml; envsubst < .ci/service.yml > service.yml"
    //     //     //sh "kubectl apply -f deployment.yml -n ${namespace}"
    //     //     //sh "kubectl apply -f service.yml -n ${namespace}"
    //     //     sh "kubectl get pods"
    //     // }

    //     docker.withRegistry(ecrRegistryUrl, "ecr:${awsRegion}:${awsCredential}") {
    //         sh "aws eks --region ap-south-1 update-kubeconfig --name eks-dev"
    //         sh "kubectl get pods"
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