#!/usr/bin/env groovy
void call(Map pipelineParams) {

    pipeline {

        agent any

        options {
            disableConcurrentBuilds()
            disableResume()
            timeout(time: 5, unit: 'MINUTES')
        }
        
        stages {
            stage ('Build Backend') {
                when {
                    allOf {
                        // Branch Event: Nornal Flow
                        anyOf {
                            branch 'main'
                            branch 'PR-*'
                        }
                        allOf {
                            changeset "**/frontend/**"
                        }
                    }
                }
                steps {
                    script {
                        nodeBuildFrontEnd()
                    }
                }
            }
        }

        post {
            cleanup {
                cleanWs()
            }
        }
    }
}
//========================================================================
// node pipeline
// Version: v1.0
// Updated:
//========================================================================
//========================================================================
// Notes:
//
//
//========================================================================
