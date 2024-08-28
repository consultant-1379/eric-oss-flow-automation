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
        K8S_NAMESPACE: '${K8S_NAMESPACE}',
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
        BAZAAR_TOKEN: '${CREDENTIALS_BAZAAR}',
        GERRIT_USERNAME: '${GERRIT_USERNAME}',
        GERRIT_PASSWORD: '${GERRIT_PASSWORD}'
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
        timeout(time: 75, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '50', artifactNumToKeepStr: '50'))
    }

    environment {
        RELEASE = "true"
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
    }

    // Stage names (with descriptions) taken from ADP Microservice CI Pipeline Step Naming Guideline: https://confluence.lmera.ericsson.se/pages/viewpage.action?pageId=122564754
    stages {
        stage('Prepare') {
            steps {
                checkout([$class: 'GitSCM',
                    branches: [
                        [name: "master"]
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
                        [url: '${GERRIT_CENTRAL}/OSS/com.ericsson.oss.ae/eric-oss-flow-automation']
                    ]
                ])
                sh "${bob} --help"
            }
        }

        stage('Clean') {
            steps {
                echo 'Inject settings.xml into workspace:'
                configFileProvider([configFile(fileId: "${env.SETTINGS_CONFIG_FILE_NAME}", targetLocation: "${env.WORKSPACE}")]) {}
                archiveArtifacts allowEmptyArchive: true, artifacts: 'ruleset2.0.yaml, publish.Jenkinsfile'
                sh "${bob} clean"
            }
        }

        stage('Init') {
            steps {
                sh "${bob} init-drop"
                sh "${bob_container} docker-compose:docker-compose-config"
                archiveArtifacts 'artifact.properties'
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
                    echo "Download latest war file fa ui"
                }
            }
        }

        stage('Image') {
            steps {
                sh "${bob} image"
                sh "${bob} image-dr-check"
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/image-design-rule-check-report*'
                }
            }
        }

        stage('Test myFirstFlow project') {
            steps {
                sh "${bob} example-flows:test-flow-project-zip"
            }
        }

        stage('Docker Compose FDRE') {
            environment {
                POSTGRES_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-postgres")', returnStdout: true).trim()
                REST_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-rest")', returnStdout: true).trim()
                JACOCO_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-jacoco")', returnStdout: true).trim()
            }
            steps {
                echo "docker-compose-sdk tag"
                sh "${bob_container} docker-compose-sdk:image-tag-internal-fa-latest"
                sh "${bob_container} docker-compose-sdk:image-tag-internal-fa-db-latest"
                echo "docker-compose up"
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                    sh "${bob_container} docker-compose-sdk:docker-compose-up-fdre"
                }
                echo "Checking v1/flows availability"
                sh "${bob_container} docker-compose-sdk:get-flows-fdre"
            }
            post {
                always {
                    echo "docker-compose down -v."
                    sh "${bob_container} docker-compose-sdk:docker-compose-down-fdre"

                    echo "docker-compose ps"
                    sh "${bob_container} docker-compose-sdk:docker-compose-ps-check"
                }
            }
        }

        stage('Integration, Interface and JSE Tests') {
            environment {
                POSTGRES_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-postgres")', returnStdout: true)
                REST_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-rest")', returnStdout: true)
                JACOCO_RANDOM_PORT = sh(script: 'echo $(cat ".bob/var.port-jacoco")', returnStdout: true)
            }
            steps {
                echo "docker-compose config."
                sh "${bob_container} docker-compose:docker-compose-config"
                echo "docker-compose up."
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                    sh "${bob_container} docker-compose:docker-compose-up"
                }

                echo "Run All Tests."
                sh "${bob_container} test:run-all-tests"

                echo "Test simpleCalculatorFlow flow zip"
                sh "${bob} example-flows:test-flow-zip"
            }
            post {
                always {
                    echo "docker-compose down -v."
                    sh "${bob_container} docker-compose:docker-compose-down"
                    echo "Delete the eric-oss-flow-automation-dev testing image"
                    sh "${bob} delete-images:delete-internal-dev-image"
                    sh "${bob} delete-images:delete-db-dev-image"
                }
            }
        }

        stage('SonarQube') {
            when {
                expression { env.SQ_ENABLED == "true" }
            }
            steps {
                withSonarQubeEnv("${env.SQ_SERVER}") {
                    sh "${bob} sonar-enterprise-release"
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                        sh "${bob} package"
                    }
                }
            }
        }

        stage('K8S Resource Lock') {
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
                        echo "Inject kubernetes config file (${env.K8S_CLUSTER_ID}) based on the Lockable Resource name: ${env.RESOURCE_NAME}"
                        configFileProvider([configFile(fileId: "${K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")]) {}
                        echo "The namespace (${env.K8S_NAMESPACE}) is reserved and locked based on the Lockable Resource name: ${env.RESOURCE_NAME}"

                        echo "Helm dry run"
                        withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                            sh "${bob} helm-dry-run"
                        }
                        echo "Create namespace"
                        sh "${bob} create-namespace"

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
                        sh "${bob} generate-VA-report-V2:no-upload" // [IDUN-21488] Upload VA report to VHUB -> Change to ${bob} generate-VA-report-V2:upload after registering product on vhub
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

        stage('Generate Docs') {
            steps {
                withCredentials([string(credentialsId: 'MARKETPLACE_TOKEN_FLOW_AUTOMATION', variable: 'MARKETPLACE_ACCESS_TOKEN'),
                                 usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')])
                {
                    sh "${bob} generate"
                    echo "Upload release Docs to marketplace"
                    sh "${bob} doc-marketplace-upload-release"
                }

                publishHTML (target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: 'build/doc/html/API_Documentation',
                        reportFiles: 'index.html',
                        reportName: 'REST API Documentation'
                ])
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'build/doc/**/*.*'
                }
            }
        }

        stage('Publish SDK Artifacts') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                    echo "Upload flow project zips"
                    sh "${bob} example-flows:upload-flow-project-zips"

                    echo "Upload flow package zips"
                    sh "${bob} example-flows:upload-flow-zips"
                }
            }
        }

        stage('Publish') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                    sh "${bob} publish"
                    sh "${bob} publish-md-oas"
                }
            }
            post {
                cleanup {
                    sh "${bob} delete-images"
                }
            }
        }
    }
    post {
        success {
            withCredentials([usernamePassword(credentialsId: 'GERRIT_PASSWORD',
                usernameVariable: 'GERRIT_USERNAME', passwordVariable: 'GERRIT_PASSWORD')])
            {
                sh "${bob} create-git-tag"
                bumpVersion()
            }
            script {
                sh "${bob} helm-chart-check-report-warnings"
                sendHelmDRWarningEmail()
                modifyBuildDescription()
            }
        }
        failure {
            sh "${bob} delete-images"
            echo 'Delete namespace'
            withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                sh "${bob} delete-namespace"
            }
        }
        aborted {
            sh "${bob} delete-images"
            echo 'Delete namespace'
            withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                sh "${bob} delete-namespace"
            }
        }
    }
}

def modifyBuildDescription() {

    def CHART_NAME = "eric-oss-flow-automation"
    def DOCKER_IMAGE_NAME = "eric-oss-flow-automation"

    def VERSION = readFile('.bob/var.build-version').trim()
    def RELEASE_VERSION = readFile('.bob/var.release-version').trim()

    def CHART_DOWNLOAD_LINK = "https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm/${CHART_NAME}/${CHART_NAME}-${RELEASE_VERSION}.tgz"
    def DOCKER_IMAGE_DOWNLOAD_LINK = "https://armdocker.rnd.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-drop/${CHART_NAME}/${VERSION}/"

    currentBuild.description = "Helm Chart: <a href=${CHART_DOWNLOAD_LINK}>${CHART_NAME}-${RELEASE_VERSION}.tgz</a><br>Docker Image: <a href=${DOCKER_IMAGE_DOWNLOAD_LINK}>${DOCKER_IMAGE_NAME}-${VERSION}</a><br>Gerrit: <a href=${env.GERRIT_CHANGE_URL}>${env.GERRIT_CHANGE_URL}</a> <br>"
}

def sendHelmDRWarningEmail() {
    def val = readFile '.bob/var.helm-chart-check-report-warnings'
    if (val.trim().equals("true")) {
        echo "WARNING: One or more Helm Design Rules have a WARNING state. Review the Archived Helm Design Rule Check Report: design-rule-check-report.html"
        manager.addWarningBadge("One or more Helm Design Rules have a WARNING state. Review the Archived Helm Design Rule Check Report: design-rule-check-report.html")
        echo "Sending an email to Helm Design Rule Check distribution list: ${env.HELM_DR_CHECK_DISTRIBUTION_LIST}"
        try {
            mail to: "${env.HELM_DR_CHECK_DISTRIBUTION_LIST}",
            from: "${env.GERRIT_PATCHSET_UPLOADER_EMAIL}",
            cc: "${env.GERRIT_PATCHSET_UPLOADER_EMAIL}",
            subject: "[${env.JOB_NAME}] One or more Helm Design Rules have a WARNING state. Review the Archived Helm Design Rule Check Report: design-rule-check-report.html",
            body: "One or more Helm Design Rules have a WARNING state. <br><br>" +
            "Please review Gerrit and the Helm Design Rule Check Report: design-rule-check-report.html: <br><br>" +
            "&nbsp;&nbsp;<b>Gerrit master branch:</b> https://gerrit.ericsson.se/gitweb?p=${env.GERRIT_PROJECT}.git;a=shortlog;h=refs/heads/master <br>" +
            "&nbsp;&nbsp;<b>Helm Design Rule Check Report:</b> ${env.BUILD_URL}artifact/.bob/design-rule-check-report.html <br><br>" +
            "For more information on the Design Rules and ADP handling process please see: <br>" +
            "&nbsp;&nbsp; - <a href='https://confluence.lmera.ericsson.se/display/AA/Helm+Chart+Design+Rules+and+Guidelines'>Helm Design Rule Guide</a><br>" +
            "&nbsp;&nbsp; - <a href='https://confluence.lmera.ericsson.se/display/ACD/Design+Rule+Checker+-+How+DRs+are+checked'>More Details on Design Rule Checker</a><br>" +
            "&nbsp;&nbsp; - <a href='https://confluence.lmera.ericsson.se/display/AA/General+Helm+Chart+Structure'>General Helm Chart Structure</a><br><br>" +
            "<b>Note:</b> This mail was automatically sent as part of the following Jenkins job: ${env.BUILD_URL}",
            mimeType: 'text/html'
        } catch(Exception e) {
            echo "Email notification was not sent."
            print e
        }
    }
}

/*  increase pom & prefix version - minor number
    e.g.  1.0.0 -> 1.1.0
*/
def bumpVersion() {
    env.oldPatchVersionPrefix = readFile ".bob/var.build-version"
    env.VERSION_PREFIX_CURRENT = env.oldPatchVersionPrefix.trim()
    // increase minor number to version_prefix
    sh 'docker run --rm -v $PWD/VERSION_PREFIX:/app/VERSION -w /app --user $(id -u):$(id -g) armdocker.rnd.ericsson.se/proj-eric-oss-drop/utilities/bump minor'
    env.versionPrefix = readFile "VERSION_PREFIX"
    env.newMinorVersionPrefix = env.versionPrefix.trim() + "-SNAPSHOT"
    env.VERSION_PREFIX_UPDATED = env.newMinorVersionPrefix.trim()
    echo "pom version has been bumped from ${VERSION_PREFIX_CURRENT} to ${VERSION_PREFIX_UPDATED}"
    sh """
        find ./ -name 'pom.xml' -type f -exec sed -i -e "0,/${VERSION_PREFIX_CURRENT}/s//${VERSION_PREFIX_UPDATED}/" {} \\;
        git add VERSION_PREFIX
        find ./ -name 'pom.xml' -type f -exec git add pom.xml {} \\;
        find ./ -name 'generatedProject-root-pom.xml' -type f -exec sed -i -e "0,/${VERSION_PREFIX_CURRENT}/s//${VERSION_PREFIX_UPDATED}/" {} \\;
        find ./ -name 'generatedProject-root-pom.xml' -type f -exec git add {} \\;
        git status
        git commit -m "Automatically updating VERSION_PREFIX to ${versionPrefix}"
        git remote set-url --push origin ssh://gerrit.ericsson.se:29418/OSS/com.ericsson.oss.ae/eric-oss-flow-automation
        git push origin HEAD:master
    """
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
