pipeline {
	agent { docker }
	stages {
		stage('build') {
			steps {
				sh './gradlew build'
			}
		}
	}
}
