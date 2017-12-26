pipeline {
	agent { docker }
	stages {
		stage('build') {
			steps {
				bat './gradlew build'
			}
		}
	}
}
