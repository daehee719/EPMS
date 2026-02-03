pipeline {
  agent any
  triggers {
    pollSCM('H/1 * * * *')
  }
  parameters {
    choice(name: 'PROFILE', choices: ['dev', 'prod'], description: 'Maven profile')
  }
  tools {
    jdk 'corretto-17'
    maven 'maven-system'
  }
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
    stage('Build') {
      steps {
        sh "mvn -P${params.PROFILE} clean package"
      }
    }
  }
  post {
    success {
      archiveArtifacts artifacts: 'target/*.war', fingerprint: true
    }
  }
}
