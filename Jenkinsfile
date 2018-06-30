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
        step([$class: 'hudson.plugins.checkstyle.CheckStylePublisher', pattern: '**/src/test/resources/checkstyle.xml', unstableTotalAll:'0'])
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
