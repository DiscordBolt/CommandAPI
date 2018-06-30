void setBuildStatus(String message, String state) {
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/DiscordBolt/CommandAPI"],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}

pipeline {
  agent {
    docker {
      image 'gradle:4.8-jdk8-alpine'
    }

  }
  stages {
    stage('Checkout') {
      steps {
        echo 'Stage:Checkout'
        git 'https://github.com/DiscordBolt/CommandAPI'
      }
    }
    stage('Build') {
      steps {
        echo 'Stage:Build'
        sh 'chmod +x gradlew'
        sh './gradlew build -x test'
      }
    }
    stage('Test') {
      steps {
        echo 'Stage:Test'
        sh './gradlew test'
      }
    }
    stage('Check') {
      steps {
        echo 'Stage:Check'
        step([$class: 'hudson.plugins.checkstyle.CheckStylePublisher', pattern: '**/reports/checkstyle/main.xml', unstableTotalAll:'0'])
      }
      post {
        unstable {
          echo 'Stage Check is unstable... Setting Github build status"
          setBuildStatus("This commit has failed checks", "FAILURE");
        }
      }
    }
    stage('Deploy') {
      steps {
        echo 'Stage:Deploy'
      }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
      junit 'build/test-results/**/*.xml'
    }
  }
}
