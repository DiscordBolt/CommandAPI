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
        step([$class: 'hudson.plugins.checkstyle.CheckStylePublisher', pattern: '**/reports/checkstyle/main.xml'])
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
      
      echo TokenMacro.expandAll( CHECKSTYLE_COUNT, "Checkstyle count: ${COUNT} );
     /* script {
        //echo "Checkstyle warning count: ${CHECKSTYLE_COUNT}"
        if (CHECKSTYLE_COUNT > 0) {
          echo "Checkstyle has warnings"
          setBuildStatus("This commit has ${CHECKSTYLE_COUNT} warnings (${CHECKSTYLE_NEW} new)", "FAILURE", "continuous-integration/jenkins/checkstyle");
        }
      } */
    }
  }
}
