void setBuildStatus(String message, String state, String context) {
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/DiscordBolt/CommandAPI"],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: context],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}

def isPRMergeBuild() {
    return (env.BRANCH_NAME ==~ /^PR-\d+$/)
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
          echo 'Stage Check is unstable... Setting Github build status'
          setBuildStatus("This commit has failed checks", "FAILURE", "continuous-integration/jenkins/checks");
          script { currentBuild.result='SUCCESS' } // We do not want to fail the entire build for these checks
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
