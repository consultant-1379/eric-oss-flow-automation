#!/usr/bin/env groovy

def defaultBobImage = 'armdocker.rnd.ericsson.se/proj-adp-cicd-drop/bob.2.0:1.7.0-55'

def bob_container = new BobCommand()
    .bobImage(defaultBobImage)
    .envVars([
        HOME:'${HOME}',
        ISO_VERSION:'${ISO_VERSION}',
        RELEASE:'${RELEASE}',
        SONAR_HOST_URL:'${SONAR_HOST_URL}',
        SONAR_AUTH_TOKEN:'${SONAR_AUTH_TOKEN}',
        GERRIT_CHANGE_NUMBER:'${GERRIT_CHANGE_NUMBER}',
        KUBECONFIG:'${KUBECONFIG}',
        USER:'${USER}',
        SELI_ARTIFACTORY_REPO_USER:'${CREDENTIALS_SELI_ARTIFACTORY_USR}',
        SELI_ARTIFACTORY_REPO_PASS:'${CREDENTIALS_SELI_ARTIFACTORY_PSW}',
        SERO_ARTIFACTORY_REPO_USER:'${CREDENTIALS_SERO_ARTIFACTORY_USR}',
        SERO_ARTIFACTORY_REPO_PASS:'${CREDENTIALS_SERO_ARTIFACTORY_PSW}',
        MARKETPLACE_ACCESS_TOKEN: '${MARKETPLACE_TOKEN_FLOW_AUTOMATION}',
        MAVEN_CLI_OPTS: '${MAVEN_CLI_OPTS}',
        OPEN_API_SPEC_DIRECTORY: '${OPEN_API_SPEC_DIRECTORY}',
        POSTGRES_RANDOM_PORT:'${POSTGRES_RANDOM_PORT}',
        REST_RANDOM_PORT:'${PORT_REST}',
        JACOCO_RANDOM_PORT:'${JACOCO_RANDOM_PORT}',
        VHUB_API_TOKEN:'${VHUB_API_TOKEN}',
        XRAY_USER:'${CREDENTIALS_XRAY_SELI_ARTIFACTORY_USR}',
        XRAY_APIKEY:'${CREDENTIALS_XRAY_SELI_ARTIFACTORY_PSW}',
        FOSSA_API_KEY: '${CREDENTIALS_FOSSA_API_KEY}',
        BAZAAR_TOKEN: '${CREDENTIALS_BAZAAR}'
    ])
    .needDockerSocket(true)
    .toString()

def bob = 'python3 bob/bob2.0/bob.py -r ruleset2.0.yaml'
def bob_mimer = 'python3 bob/bob2.0/bob.py -r ruleset2.0.mimer.yaml'

def LOCKABLE_RESOURCE_LABEL = "kaas"

pipeline {
    agent {
        node {
            label NODE_LABEL
        }
    }

    options {
        timestamps()
        timeout(time: 55, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '50', artifactNumToKeepStr: '50'))
    }

    environment {
        TEAM_NAME = "Team_indigo,Team_Saga"
        KUBECONFIG = "${env.WORKSPACE}/.kube/config"
        MAVEN_CLI_OPTS = "-Duser.home=${env.HOME} -B -s ${env.WORKSPACE}/settings.xml"
        OPEN_API_SPEC_DIRECTORY = "flowautomation-service-rest-api/src/main/resources/v1"
        HADOLINT_ENABLED = "true"
        KUBEHUNTER_ENABLED = "true"
        KUBEAUDIT_ENABLED = "true"
        KUBESEC_ENABLED = "true"
        TRIVY_ENABLED = "true"
        XRAY_ENABLED = "true"
        ANCHORE_ENABLED = "true"
        FOSSA_ENABLED = "true"
        MUNIN_UPDATE_ENABLED = "true"
    }

    // Stage names (with descriptions) taken from ADP Microservice CI Pipeline Step Naming Guideline: https://confluence.lmera.ericsson.se/pages/viewpage.action?pageId=122564754
    stages {
        stage('Prepare') {
            steps {
                checkout([$class: 'GitSCM',
                    branches: [
                        [name: "${GERRIT_PATCHSET_REVISION}"]
                    ],
                    extensions: [
                        [$class: 'SubmoduleOption',
                            disableSubmodules: false,
                            parentCredentials: true,
                            recursiveSubmodules: true,
                            reference: '',
                            trackingSubmodules: false],
                        [$class: 'CleanBeforeCheckout']
                    ],
                    userRemoteConfigs: [
                        [url: '${GERRIT_MIRROR}/OSS/com.ericsson.oss.ae/eric-oss-flow-automation']
                    ]
                ])
                sh "${bob} --help"
            }
        }

        stage('Clean') {
            steps {
                echo 'Inject settings.xml into workspace:'
                configFileProvider([configFile(fileId: "${env.SETTINGS_CONFIG_FILE_NAME}", targetLocation: "${env.WORKSPACE}")]) {}
                archiveArtifacts allowEmptyArchive: true, artifacts: 'ruleset2.0.yaml, precodereview.Jenkinsfile'
                sh "${bob} clean"
            }
        }

        stage('Init') {
            steps {
                sh "${bob} init-precodereview"
                sh "${bob_container} docker-compose:docker-compose-config"
                sh "${bob_mimer} mimer-init"
                script {
                    authorName = sh(returnStdout: true, script: 'git show -s --pretty=%an')
                    currentBuild.displayName = currentBuild.displayName + ' / ' + authorName
                }
            }
        }

        stage('Lint') {
            steps {
                parallel(
                    "lint markdown": {
                        sh "${bob} lint:markdownlint lint:vale"
                    },
                    "lint helm": {
                        sh "${bob} lint:helm"
                    },
                    "lint helm design rule checker": {
                        sh "${bob} lint:helm-chart-check"
                    },
                    "lint code": {
                        sh "${bob} lint:license-check"
                    },
                    "lint OpenAPI spec": {
                        sh "${bob} lint:oas-bth-linter"
                    },
                    "lint metrics": {
                        sh "${bob} lint:metrics-check"
                    }
                )
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/*bth-linter-output.html, **/design-rule-check-report.*'
                }
            }
        }

        stage('SDK Validation') {
            steps {
                sh "${bob} validate-sdk"
            }
        }

        stage('Build Jars') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                    sh "${bob} build"
                }
            }
        }

        stage('Docker Images') {
            steps {
                sh "${bob} image"
                sh "${bob} image-dr-check"
            }
            post {
                always {
                    echo "package images"
                    withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                        sh "${bob} package"
                    }
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/image-design-rule-check-report*'
                }
            }
        }

        stage('Test myFirstFlow project') {
            steps {
                sh "${bob} example-flows:test-flow-project-zip"
            }
        }

        stage ("Parallel Stream") {
            parallel {
                stage("Helm Stream") {
                    stages {
                        stage('K8S Resource Lock') {
                            when {
                                expression { env.K8S_TEST == "true" }
                            }
                            options {
                                lock(label: LOCKABLE_RESOURCE_LABEL, variable: 'RESOURCE_NAME', quantity: 1)
                            }
                            environment {
                                K8S_CLUSTER_ID = sh(script: "echo \${RESOURCE_NAME} | cut -d'_' -f1", returnStdout: true).trim()
                                K8S_NAMESPACE = sh(script: "echo \${RESOURCE_NAME} | cut -d',' -f1 | cut -d'_' -f2", returnStdout: true).trim()
                            }
                            stages {
                                stage('Helm Install') {
                                    steps {
                                        echo "Inject kubernetes config file (${env.KUBECONFIG}) based on the Lockable Resource name: ${env.RESOURCE_NAME}"
                                        configFileProvider([configFile(fileId: "${env.K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")]) {}
                                        echo "The following resource is reserved and locked: ${env.RESOURCE_NAME}"

                                        echo "Helm dry run"
                                        withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                                            sh "${bob} helm-dry-run"
                                        }
                                        script{
                                            def namespace = readFile '.bob/var.k8s-namespace'
                                            echo "Create namespace ${namespace}"
                                            sh "${bob} create-namespace"
                                        }

                                        echo "Install or upgrade ci internal postgres"
                                        withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                                            sh "${bob} helm-install-or-upgrade-postgres"
                                        }
                                        script {
                                            if (env.HELM_UPGRADE == "true") {
                                                echo "Upgrade eric-oss-flow-automation"
                                                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                                                    sh "${bob} helm-upgrade"
                                                }
                                            } else {
                                                echo "Install eric-oss-flow-automation"
                                                sh "${bob} helm-install"
                                            }
                                        }
                                        echo "Healthcheck"
                                        sh "${bob} healthcheck"
                                    }
                                    post {
                                        always {
                                            sh "${bob} kaas-info || true"
                                            archiveArtifacts allowEmptyArchive: true, artifacts: 'build/kaas-info.log'
                                        }
                                        unsuccessful {
                                            withCredentials([usernamePassword(credentialsId: 'SERO_ARTIFACTORY', usernameVariable: 'SERO_ARTIFACTORY_REPO_USER', passwordVariable: 'SERO_ARTIFACTORY_REPO_PASS')]) {
                                                sh "${bob} collect-k8s-logs || true"
                                                archiveArtifacts allowEmptyArchive: true, artifacts: 'k8s-logs/*'
                                                sh "${bob} delete-namespace"
                                            }
                                        }
                                    }
                                }
                                stage('K8S Test') {
                                    steps {
                                        sh "${bob} helm-test"
                                    }
                                    post {
                                        unsuccessful {
                                            withCredentials([usernamePassword(credentialsId: 'SERO_ARTIFACTORY', usernameVariable: 'SERO_ARTIFACTORY_REPO_USER', passwordVariable: 'SERO_ARTIFACTORY_REPO_PASS')]) {
                                                sh "${bob} collect-k8s-logs || true"
                                                archiveArtifacts allowEmptyArchive: true, artifacts: 'k8s-logs/*'
                                                echo 'Delete namespace'
                                                sh "${bob} delete-namespace"
                                            }
                                        }
                                        cleanup {
                                            echo 'Cleanup moved as a separate stage of the pipeline'
                                        }
                                    }
                                }

                            }
                        }
                    } // stages for helm stream
                } // Helm Stream
                stage("Docker Compose Stream") {
                    stages {
                        stage('Docker Compose') {
                            environment {
                                POSTGRES_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-postgres")', returnStdout: true).trim()
                                REST_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-rest")', returnStdout: true).trim()
                                JACOCO_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-jacoco")', returnStdout: true).trim()
                            }
                            steps {
                                echo "docker-compose config."
                                sh "${bob_container} docker-compose:docker-compose-config"
                                echo "docker-compose up"
                                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                                    sh "${bob_container} docker-compose:docker-compose-up"
                                }
                            }
                            post {
                                unsuccessful {
                                    echo "docker-compose down -v."
                                    sh "${bob_container} docker-compose:docker-compose-down"
                                }
                            }
                        }

                        stage('Integration, Interface and JSE Tests') {
                            environment {
                                POSTGRES_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-postgres")', returnStdout: true).trim()
                                REST_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-rest")', returnStdout: true).trim()
                                JACOCO_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-jacoco")', returnStdout: true).trim()
                            }
                            steps {
                                echo "Run All Tests."
                                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                                    sh "${bob_container} test:run-all-tests"
                                }
                            }
                        }
                        stage('Test simpleCalculatorFlow flow zip') {
                            steps {
                                sh "${bob} example-flows:test-flow-zip"
                            }
                            post {
                                always {
                                    echo "docker-compose down -v."
                                    sh "${bob_container} docker-compose:docker-compose-down"
                                }
                            }
                        }
                        stage('SonarQube Analysis') {
                            when {
                                expression { env.SQ_ENABLED == "true" }
                            }
                            steps {
                                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                                    withSonarQubeEnv("${env.SQ_SERVER}") {
                                        sh "${bob} sonar-enterprise-pcr"
                                    }
                                }
                            }
                        }
                    } // stages for docker compose stream
                } // Docker Compose Stream
            } // parallel
        } // Streams

        stage('Vulnerability Analysis') {
            parallel{
                stage('Hadolint') {
                    when {
                        expression { env.HADOLINT_ENABLED == "true" }
                    }
                    steps {
                        sh "${bob} hadolint-scan || true"
                        echo "Evaluating Hadolint Scan Resultcodes..."
                        sh "${bob} evaluate-design-rule-check-resultcodes || true"
                        archiveArtifacts "build/hadolint-scan/**.*"
                    }
                }
                stage('Kubehunter') {
                    when {
                        expression { env.KUBEHUNTER_ENABLED == "true" }
                    }
                    options {
                        lock(label: LOCKABLE_RESOURCE_LABEL, variable: 'RESOURCE_NAME', quantity: 1)
                    }
                    environment {
                        K8S_CLUSTER_ID = sh(script: "echo \${RESOURCE_NAME} | cut -d'_' -f1", returnStdout: true).trim()
                    }
                    steps {
                        sh 'echo "${K8S_CLUSTER_ID}"'
                        configFileProvider([configFile(fileId: "${env.K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")]) {}
                        sh 'echo "System: [$system]"'
                        sh 'echo "Kubeconfig: [$KUBECONFIG]"'
                        sh "${bob} kubehunter-scan || true"
                        archiveArtifacts "build/kubehunter-report/**/*"
                    }
                }
                stage('Kubeaudit') {
                    when {
                        expression { env.KUBEAUDIT_ENABLED == "true" }
                    }
                    steps {
                        sh "${bob} kube-audit || true"
                        archiveArtifacts "build/kube-audit-report/**/*"
                    }
                }
                stage('Kubsec') {
                    when {
                        expression { env.KUBESEC_ENABLED == "true" }
                    }
                    steps {
                        sh "${bob} kubesec-scan || true"
                        archiveArtifacts 'build/kubesec-reports/*'
                    }
                }
                stage('Trivy') {
                    when {
                        expression { env.TRIVY_ENABLED == "true" }
                    }
                    steps {
                        sh "${bob} trivy-inline-scan || true"
                        archiveArtifacts 'build/trivy-reports/**.*'
                        archiveArtifacts 'trivy_metadata.properties'
                    }
                }
                stage('X-Ray') {
                    when {
                        expression { env.XRAY_ENABLED == "true" }
                    }
                    steps {
                        sleep(60)
                        withCredentials([usernamePassword(credentialsId: 'XRAY_SELI_ARTIFACTORY', usernameVariable: 'XRAY_USER', passwordVariable: 'XRAY_APIKEY')]) {
                            sh "${bob} fetch-xray-report"
                        }
                        archiveArtifacts 'build/xray-reports/xray_report.json'
                        archiveArtifacts 'build/xray-reports/raw_xray_report.json'
                    }
                }
                stage('Anchore-Grype') {
                    when {
                        expression { env.ANCHORE_ENABLED == "true" }
                    }
                    steps {
                        sh "${bob} anchore-grype-scan || true"
                        archiveArtifacts 'build/anchore-reports/**.*'
                    }
                }
            }
            post {
                unsuccessful {
                    withCredentials([usernamePassword(credentialsId: 'SERO_ARTIFACTORY', usernameVariable: 'SERO_ARTIFACTORY_REPO_USER', passwordVariable: 'SERO_ARTIFACTORY_REPO_PASS')]) {
                        sh "${bob} collect-k8s-logs || true"
                    }
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'k8s-logs/**/*.*'
                }
                cleanup {
                    echo 'Delete namespace'
                    withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                        sh "${bob} delete-namespace"
                    }
                    sh "${bob} cleanup-anchore-trivy-images"
                }
            }
        }

        stage('Generate Vulnerability report V2.0') {
            parallel {
                stage('Generate Vulnerability report V2.0'){
                    steps {
                        sh "${bob} generate-VA-report-V2:no-upload"
                        archiveArtifacts allowEmptyArchive: true, artifacts: 'build/Vulnerability_Report_2.0.md'
                    }
                }
                stage('Upload CPI fragment') {
                    steps {
                        withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY',
                                usernameVariable: 'SELI_ARTIFACTORY_REPO_USER',
                                passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                            sh "${bob} upload-cpi-fragment"
                        }
                    }
                }

                stage('Structure Data') {
                    steps {
                        withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY',
                                usernameVariable: 'SELI_ARTIFACTORY_REPO_USER',
                                passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                            sh "${bob} structure-data"
                            archiveArtifacts 'build/structure-output/*.json'
                        }
                    }
                }
            }
        }

        stage ('FOSSA Analyze') {
            when {
                expression { env.FOSSA_ENABLED == "true" }
            }
            steps {
                withCredentials([string(credentialsId: 'FOSSA_API_token', variable: 'FOSSA_API_KEY')]) {
                    sh "${bob} fossa-analyze"
                }
            }
        }
        stage ('FOSSA Fetch Report') {
            when {
                expression { env.FOSSA_ENABLED == "true" }
            }
            steps {
                withCredentials([string(credentialsId: 'FOSSA_API_token', variable: 'FOSSA_API_KEY')]) {
                    sh "${bob} fossa-scan-status-check"
                    sh "${bob} fetch-fossa-report-attribution"
                }
                archiveArtifacts '*fossa-report.json'
            }
        }
        stage ('FOSSA Dependency Validate') {
            when {
                expression { env.FOSSA_ENABLED == "true" }
            }
            steps {
                withCredentials([string(credentialsId: 'FOSSA_API_token', variable: 'FOSSA_API_KEY')]) {
                    sh "${bob} dependency-validate"
                }
                withCredentials([string(credentialsId: 'munin_token', variable: 'MUNIN_TOKEN')]) {
                    sh "${bob_mimer} search-foss"
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'build/search-foss/**.*'
                }
                sh "${bob_mimer} mimer-dependency-validate"

            }
        }

        stage ('FOSS Validation for Mimer') {
            when {
                expression { env.MUNIN_UPDATE_ENABLED == "true" }
            }
            steps {
                withCredentials([string(credentialsId: 'munin_token', variable: 'MUNIN_TOKEN')]) {
                    sh "${bob_mimer} munin-update-version"
                }
            }
        }

        stage('Generate OAS coverage report') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')])
                {
                    sh "${bob} oas-coverage-report"
                }
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'build/doc/**/*.*'
                }
            }
        }

        stage('Generate Docs') {
            steps {
                withCredentials([string(credentialsId: 'MARKETPLACE_TOKEN_FLOW_AUTOMATION', variable: 'MARKETPLACE_ACCESS_TOKEN'),
                                 usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')])
                {
                    sh "${bob} generate"
                    echo "Upload in Development Docs to marketplace"
                    sh "${bob} doc-marketplace-upload-dev"
                }

                publishHTML (target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: 'build/doc/html/API_Documentation',
                        reportFiles: 'index.html',
                        reportName: 'REST API Documentation'
                ])

                script {
                    def val = readFile '.bob/var.has-openapi-spec-been-modified'
                    if (val.trim().equals("true")) {
                        manager.addInfoBadge("OpenAPI spec has changed. Review the Archived HTML Output files: rest2html*.zip")
                        echo "Sending email to CPI document reviewers distribution list: ${env.CPI_DOCUMENT_REVIEWERS_DISTRIBUTION_LIST}"
                        try {
                            mail to: "${env.CPI_DOCUMENT_REVIEWERS_DISTRIBUTION_LIST}",
                                    from: "${env.GERRIT_PATCHSET_UPLOADER_EMAIL}",
                                    cc: "${env.GERRIT_PATCHSET_UPLOADER_EMAIL}",
                                    subject: "[${env.JOB_NAME}] OpenAPI specification has been updated and is up for review",
                                    body: "The OpenAPI spec documentation has been updated.<br><br>" +
                                            "Please review the patchset and archived HTML output files linked here below:<br><br>" +
                                            "&nbsp;&nbsp;Gerrit Patchset: ${env.GERRIT_CHANGE_URL}<br>" +
                                            "&nbsp;&nbsp;HTML output files: ${env.BUILD_URL}artifact/build/doc/html/API_Documentation/index.html <br><br><br><br>" +
                                            "<b>Note:</b> This mail was automatically sent as part of the following Jenkins job: ${env.BUILD_URL}",
                                    mimeType: 'text/html'
                        } catch(Exception e) {
                            echo "Email notification was not sent."
                            print e
                        }
                    }
                }
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'build/doc/**/*.*'
                }
            }
        }

    } // stages main
    post {
        failure {
            echo 'Delete namespace'
            withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                sh "${bob} delete-namespace"
            }
            echo 'Delete docker images, on failure build'
            sh "${bob} delete-images:delete-internal-image"
            sh "${bob} delete-images:delete-db-image"
        }
        success {
            script {
                sh "${bob} helm-chart-check-report-warnings"
                addHelmDRWarningIcon()
                modifyBuildDescription()
                echo 'Delete docker images'
                sh "${bob} delete-images:delete-internal-image"
                sh "${bob} delete-images:delete-db-image"
            }
        }
        aborted {
            echo 'Delete namespace'
            withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                sh "${bob} delete-namespace"
            }
            echo 'Delete docker images, on aborted build'
            sh "${bob} delete-images:delete-internal-image"
            sh "${bob} delete-images:delete-db-image"
        }
    }
}

def modifyBuildDescription() {

    def CHART_NAME = "eric-oss-flow-automation"
    def DOCKER_IMAGE_NAME = "eric-oss-flow-automation"

    def VERSION = readFile('.bob/var.build-version').trim()

    def CHART_DOWNLOAD_LINK = "https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-ci-internal-helm/${CHART_NAME}/${CHART_NAME}-${VERSION}.tgz"
    def DOCKER_IMAGE_DOWNLOAD_LINK = "https://armdocker.rnd.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-ci-internal/${CHART_NAME}/${VERSION}/"

    currentBuild.description = "Helm Chart: <a href=${CHART_DOWNLOAD_LINK}>${CHART_NAME}-${VERSION}.tgz</a><br>Docker Image: <a href=${DOCKER_IMAGE_DOWNLOAD_LINK}>${DOCKER_IMAGE_NAME}-${VERSION}</a><br>Gerrit: <a href=${env.GERRIT_CHANGE_URL}>${env.GERRIT_CHANGE_URL}</a> <br>"
}

def addHelmDRWarningIcon() {
    def val = readFile '.bob/var.helm-chart-check-report-warnings'
    if (val.trim().equals("true")) {
        echo "WARNING: One or more Helm Design Rules have a WARNING state. Review the Archived Helm Design Rule Check Report: design-rule-check-report.html"
        manager.addWarningBadge("One or more Helm Design Rules have a WARNING state. Review the Archived Helm Design Rule Check Report: design-rule-check-report.html")
    } else {
        echo "No Helm Design Rules have a WARNING state"
    }
}

def getQualityGate() {
    echo "Wait for SonarQube Analysis is done and Quality Gate is pushed back:"
    try {
        timeout(time: 30, unit: 'SECONDS') {
            qualityGate = waitForQualityGate()
        }
    } catch(Exception e) {
        return false
    }

    echo 'If Analysis file exists, parse the Dashboard URL:'
    if (fileExists(file: 'target/sonar/report-task.txt')) {
        sh 'cat target/sonar/report-task.txt'
        def props = readProperties file: 'target/sonar/report-task.txt'
        env.DASHBOARD_URL = props['dashboardUrl']
    }

    if (qualityGate.status.replaceAll("\\s","") == 'IN_PROGRESS') {
        return false
    }

    if (!env.GERRIT_HOST) {
        env.GERRIT_HOST = "gerrit.ericsson.se"
    }

    if (qualityGate.status.replaceAll("\\s","") != 'OK') {
        env.SQ_MESSAGE="'"+"SonarQube Quality Gate Failed: ${DASHBOARD_URL}"+"'"
        if (env.GERRIT_CHANGE_NUMBER) {
            sh '''
                ssh -p 29418 ${GERRIT_HOST} gerrit review --label 'SQ-Quality-Gate=-1' --message ${SQ_MESSAGE} --project ${GERRIT_PROJECT} ${GERRIT_PATCHSET_REVISION}
            '''
        }
        manager.addWarningBadge("Pipeline aborted due to Quality Gate failure, see SonarQube Dashboard for more information.")
        error "Pipeline aborted due to quality gate failure!\n Report: ${env.DASHBOARD_URL}\n Pom might be incorrectly defined for code coverage: https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?pageId=309793813"
    } else {
        env.SQ_MESSAGE="'"+"SonarQube Quality Gate Passed: ${DASHBOARD_URL}"+"'"
        if (env.GERRIT_CHANGE_NUMBER) { // If Quality Gate Passed
            sh '''
                ssh -p 29418 ${GERRIT_HOST} gerrit review --label 'SQ-Quality-Gate=+1' --message ${SQ_MESSAGE} --project ${GERRIT_PROJECT} ${GERRIT_PATCHSET_REVISION}
            '''
        }
    }
    return true
}


// More about @Builder: http://mrhaki.blogspot.com/2014/05/groovy-goodness-use-builder-ast.html
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class BobCommand {
    def bobImage = 'bob.2.0:latest'
    def envVars = [:]
    def needDockerSocket = true

    String toString() {
        def env = envVars
                .collect({ entry -> "-e ${entry.key}=\"${entry.value}\"" })
                .join(' ')

        def cmd = """\
            |docker run
            |--init
            |--rm
            |--workdir \${PWD}
            |--user \$(id -u):\$(id -g)
            |-v \${PWD}:\${PWD}
            |-v /etc/group:/etc/group:ro
            |-v /etc/passwd:/etc/passwd:ro
            |-v \${HOME}:\${HOME}
            |-v /proj/mvn/:/proj/mvn
            |${needDockerSocket ? '-v /var/run/docker.sock:/var/run/docker.sock' : ''}
            |${env}
            |\$(for group in \$(id -G); do printf ' --group-add %s' "\$group"; done)
            |--group-add \$(stat -c '%g' /var/run/docker.sock)
            |${bobImage}
            |"""
        return cmd
                .stripMargin()           // remove indentation
                .replace('\n', ' ')      // join lines
                .replaceAll(/[ ]+/, ' ') // replace multiple spaces by one
    }
}
