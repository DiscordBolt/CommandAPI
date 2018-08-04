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
      image 'gradle:4.9-jdk8-slim'
    }
  }
  stages {
    stage('Build') {
      steps {
        sh 'gradle build -x test'
        archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
      }
    }
    stage('Test') {
      steps {
        sh 'gradle test'
        junit 'build/test-results/**/*.xml'
      }
    }
    stage('Check') {
      steps {
        step([$class: 'hudson.plugins.checkstyle.CheckStylePublisher', pattern: '**/reports/checkstyle/main.xml'])
        script {
          def warnings = tm('$CHECKSTYLE_COUNT').toInteger();
          def warnings_new = tm('$CHECKSTYLE_NEW').toInteger();
          if (warnings > 0) {
            setBuildStatus("This commit has " + warnings + " checkstyle warnings. (" + warnings_new + " new)", "FAILURE", "continuous-integration/jenkins/checkstyle");
          }
        }
      }
    }
  }
}
