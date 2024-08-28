{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-oss-flow-automation.name" }}
  {{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-oss-flow-automation.version" }}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Expand the name of the chart.
*/}}
{{- define "eric-oss-flow-automation.fullname" -}}
{{- if .Values.fullnameOverride -}}
  {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
  {{- $name := default .Chart.Name .Values.nameOverride -}}
  {{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-oss-flow-automation.chart" }}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Define a pullPolicy template for all images in eric-product-info.yaml (DR-D1121-102)
*/}}
{{- define "eric-oss-flow-automation.pullPolicy.global" }}
    {{- $imageId := index . "imageId" -}}
    {{- $values := index . "values" -}}
    {{- $policy := index $values "imageCredentials" $imageId "registry" "imagePullPolicy" -}}
    {{- $defaultPolicy := "IfNotPresent" -}}
    {{- if $values.global -}}
        {{- if $values.global.registry -}}
            {{- if $values.global.registry.imagePullPolicy -}}
                {{- $policy = default $values.global.registry.imagePullPolicy $policy -}}
            {{ end }}
        {{- end -}}
    {{- end -}}
    {{- printf "%s" (or $policy $defaultPolicy) -}}
{{- end -}}

{{/*
Create image pull secrets for global (outside of scope)
*/}}
{{- define "eric-oss-flow-automation.pullSecret.global" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
  {{- if .Values.global.pullSecret -}}
    {{- $pullSecret = .Values.global.pullSecret -}}
  {{- end -}}
  {{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence
*/}}
{{- define "eric-oss-flow-automation.pullSecret" -}}
{{- $pullSecret := (include "eric-oss-flow-automation.pullSecret.global" . ) -}}
{{- if .Values.imageCredentials -}}
  {{- if .Values.imageCredentials.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
  {{- end -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Define a imagePath template for all images in eric-product-info.yaml (DR-D1121-067)
*/}}
{{- define "eric-oss-flow-automation.imagePath" }}
    {{- $imageId := index . "imageId" -}}
    {{- $values := index . "values" -}}
    {{- $files := index . "files" -}}
    {{- $productInfo := fromYaml ($files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := index $productInfo "images" $imageId "registry" -}}
    {{- $repoPath := index $productInfo "images" $imageId "repoPath" -}}
    {{- $name := index $productInfo "images" $imageId "name" -}}
    {{- $tag :=  index $productInfo "images" $imageId "tag" -}}
    {{- if $values.global -}}
        {{- if $values.global.registry -}}
            {{- $registryUrl = default $registryUrl $values.global.registry.url -}}
        {{- end -}}
        {{- if not (kindIs "invalid" $values.global.registry) -}}
            {{- if not (kindIs "invalid" $values.global.registry.repoPath) -}}
                {{- $repoPath = $values.global.registry.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if $values.imageCredentials -}}
        {{- if $values.imageCredentials.registry -}}
            {{- $registryUrl = default $registryUrl $values.imageCredentials.registry.url -}}
        {{- end -}}
        {{- if not (kindIs "invalid" $values.imageCredentials.repoPath) -}}
            {{- $repoPath = $values.imageCredentials.repoPath -}}
        {{- end -}}
        {{- $image := index $values.imageCredentials $imageId -}}
        {{- if $image -}}
            {{- if $image.registry -}}
                {{- $registryUrl = default $registryUrl $image.registry.url -}}
            {{- end -}}
            {{- if not (kindIs "invalid" $image.repoPath) -}}
                {{- $repoPath = $image.repoPath -}}
            {{- end -}}
            {{- if not (kindIs "invalid" $image.tag) -}}
                {{- $tag = $image.tag -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Timezone variable
*/}}
{{- define "eric-oss-flow-automation.timezone" }}
  {{- $timezone := "UTC" }}
  {{- if .Values.global }}
    {{- if .Values.global.timezone }}
      {{- $timezone = .Values.global.timezone }}
    {{- end }}
  {{- end }}
  {{- print $timezone | quote }}
{{- end -}}

{{/*
Return the fsgroup set via global parameter if it's set, otherwise 10000
*/}}
{{- define "eric-oss-flow-automation.fsGroup.coordinated" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.fsGroup -}}
      {{- if .Values.global.fsGroup.manual -}}
        {{ .Values.global.fsGroup.manual }}
      {{- else -}}
        {{- if eq .Values.global.fsGroup.namespace true -}}
          # The 'default' defined in the Security Policy will be used.
        {{- else -}}
          10000
      {{- end -}}
    {{- end -}}
  {{- else -}}
    10000
  {{- end -}}
  {{- else -}}
    10000
  {{- end -}}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "eric-oss-flow-automation.serviceAccountName" -}}
  {{- if .Values.serviceAccount.create }}
    {{- default (include "eric-oss-flow-automation.fullname" .) .Values.serviceAccount.name }}
  {{- else }}
    {{- default "default" .Values.serviceAccount.name }}
  {{- end }}
{{- end }}

{{/*
Define the role reference for security policy
*/}}
{{- define "eric-oss-flow-automation.securityPolicy.reference" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.security -}}
      {{- if .Values.global.security.policyReferenceMap -}}
        {{ $mapped := index .Values "global" "security" "policyReferenceMap" "default-restricted-security-policy" }}
        {{- if $mapped -}}
          {{ $mapped }}
        {{- else -}}
          default-restricted-security-policy
        {{- end -}}
      {{- else -}}
        default-restricted-security-policy
      {{- end -}}
    {{- else -}}
      default-restricted-security-policy
    {{- end -}}
  {{- else -}}
    default-restricted-security-policy
  {{- end -}}
{{- end -}}

{{/*
Define the annotations for security policy
*/}}
{{- define "eric-oss-flow-automation.securityPolicy.annotations" -}}
# Automatically generated annotations for documentation purposes.
{{- end -}}

{{/*
Define Pod Disruption Budget value taking into account its type (int or string)
*/}}
{{- define "eric-oss-flow-automation.pod-disruption-budget" -}}
  {{- if kindIs "string" .Values.podDisruptionBudget.minAvailable -}}
    {{- print .Values.podDisruptionBudget.minAvailable | quote -}}
  {{- else -}}
    {{- print .Values.podDisruptionBudget.minAvailable | atoi -}}
  {{- end -}}
{{- end -}}

{{/*
Define upper limit for TerminationGracePeriodSeconds
*/}}
{{- define "eric-oss-flow-automation.terminationGracePeriodSeconds" -}}
{{- if .Values.terminationGracePeriodSeconds -}}
  {{- toYaml .Values.terminationGracePeriodSeconds -}}
{{- end -}}
{{- end -}}

{{/*
Create a merged set of nodeSelectors from global and service level.
*/}}
{{- define "eric-oss-flow-automation.nodeSelector" -}}
{{- $globalValue := (dict) -}}
{{- if .Values.global -}}
    {{- if .Values.global.nodeSelector -}}
      {{- $globalValue = .Values.global.nodeSelector -}}
    {{- end -}}
{{- end -}}
{{- if .Values.nodeSelector -}}
  {{- range $key, $localValue := .Values.nodeSelector -}}
    {{- if hasKey $globalValue $key -}}
         {{- $Value := index $globalValue $key -}}
         {{- if ne $Value $localValue -}}
           {{- printf "nodeSelector \"%s\" is specified in both global (%s: %s) and service level (%s: %s) with differing values which is not allowed." $key $key $globalValue $key $localValue | fail -}}
         {{- end -}}
     {{- end -}}
    {{- end -}}
    nodeSelector: {{- toYaml (merge $globalValue .Values.nodeSelector) | trim | nindent 2 -}}
{{- else -}}
  {{- if not ( empty $globalValue ) -}}
    nodeSelector: {{- toYaml $globalValue | trim | nindent 2 -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
GAS integration: Flow Automation UI portal home page for config.json
*/}}
{{- define "eric-oss-flow-automation.ui-homepage" }}
{{- $hostUrl := "http://localhost:8080" -}}
{{- if .Values.global -}}
  {{- if .Values.global.hosts -}}
    {{- if .Values.global.hosts.ta -}}
      {{- $hostUrl = printf "https://%s" .Values.global.hosts.ta -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- printf "%s/flow-automation/ui/#flows" $hostUrl -}}
{{- end -}}


{{/*
#################### Section for labels ####################
*/}}

{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-oss-flow-automation.standard-labels" -}}
app: {{ include "eric-oss-flow-automation.name" . }}
app.kubernetes.io/name: {{ include "eric-oss-flow-automation.name" . }}
app.kubernetes.io/version: {{ include "eric-oss-flow-automation.version" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ include "eric-oss-flow-automation.chart" . }}
chart: {{ include "eric-oss-flow-automation.chart" . }}
{{- end -}}

{{/*
Create a user defined label (DR-D1121-068, DR-D1121-060)
*/}}
{{ define "eric-oss-flow-automation.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-oss-flow-automation.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard, Config labels and GAS labels
*/}}
{{- define "eric-oss-flow-automation.labels" -}}
  {{- $standard := include "eric-oss-flow-automation.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-oss-flow-automation.config-labels" . | fromYaml -}}
  {{- $gasLabels := include "eric-oss-flow-automation.gas-labels" . | fromYaml -}}
  {{- $brLabels := include "eric-oss-flow-automation.br-labels" . | fromYaml -}}
  {{- $serviceMeshInject := include "eric-oss-flow-automation.service-mesh-inject" .| fromYaml -}}
  {{- include "eric-oss-flow-automation.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config $gasLabels $brLabels $serviceMeshInject)) | trim }}
{{- end -}}

{{/*
GAS integration: labels
*/}}
{{- define "eric-oss-flow-automation.gas-labels" }}
ui.ericsson.com/part-of: {{ index .Values "gas" "part-of" | quote }}
{{- end -}}

{{/*
BR labels
*/}}
{{- define "eric-oss-flow-automation.br-labels" }}
    {{- if (index .Values "bragent" "enabled") }}
        adpbrlabelkey: {{ .Values.bragent.name }}
    {{- end }}
{{- end }}

{{/*
#################### Section for annotations ####################
*/}}

{{/*
Annotations for the product information (DR-D1121-064, DR-D1121-067).
*/}}
{{- define "eric-oss-flow-automation.product-info" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end }}

{{/*
Create a user defined annotation (DR-D1121-065, DR-D1121-060)
*/}}
{{ define "eric-oss-flow-automation.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-oss-flow-automation.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config
*/}}
{{- define "eric-oss-flow-automation.annotations" -}}
  {{- $productInfo := include "eric-oss-flow-automation.product-info" . | fromYaml -}}
  {{- $prometheusAnn := include "eric-oss-flow-automation.prometheus" . | fromYaml -}}
  {{- $config := include "eric-oss-flow-automation.config-annotations" . | fromYaml -}}
  {{- $gasAnnotations := include "eric-oss-flow-automation.gas-annotations" . | fromYaml -}}
  {{- $serviceMeshInject := include "eric-oss-flow-automation.service-mesh-inject" .| fromYaml -}}
  {{- $serviceMeshVersion := include "eric-oss-flow-automation.service-mesh-version" .| fromYaml -}}
  {{- $istioProxyConfig := include "eric-oss-flow-automation.istio-proxy-config-annotation" .| fromYaml -}}
  {{- $logStreaming := include "eric-oss-flow-automation.directStreamingLabel" .| fromYaml -}}
  {{- $brAnnotations := include "eric-oss-flow-automation.brAnnotations" .| fromYaml -}}
  {{- include "eric-oss-flow-automation.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo  $prometheusAnn $config $gasAnnotations $serviceMeshInject $serviceMeshVersion $istioProxyConfig $logStreaming $brAnnotations)) | trim }}
{{- end -}}

{{/*
backup restore annotations
*/}}
{{ define "eric-oss-flow-automation.brAnnotations" }}
  {{- if (index .Values "bragent" "enabled") }}
        backupType: {{ .Values.bragent.backupTypeList | join ";" }}
  {{- end }}
{{- end }}

{{/*
Create prometheus info
*/}}
{{- define "eric-oss-flow-automation.prometheus" -}}
prometheus.io/path: {{ .Values.prometheus.path | quote }}
prometheus.io/port: {{ .Values.prometheus.port | quote }}
prometheus.io/scrape: {{ .Values.prometheus.scrape | quote }}
{{- end -}}

{{/*
GAS integration: annotations
*/}}
{{- define "eric-oss-flow-automation.gas-annotations" -}}
ui.ericsson.com/protocol: {{ .Values.gas.protocol | quote }}
{{- end -}}

{{/*
DR-D470217-011 This helper defines the annotation which bring the service into the mesh.
*/}}
{{- define "eric-oss-flow-automation.service-mesh-inject" }}
{{- if eq (include "eric-oss-flow-automation.service-mesh-enabled" .) "true" }}
sidecar.istio.io/inject: "true"
{{- else -}}
sidecar.istio.io/inject: "false"
{{- end -}}
{{- end -}}

{{- define "eric-oss-flow-automation.istio-proxy-config-annotation" }}
{{- if eq (include "eric-oss-flow-automation.service-mesh-enabled" .) "true" }}
proxy.istio.io/config: '{ "holdApplicationUntilProxyStarts": true }'
{{- end -}}
{{- end -}}

{{/*
GL-D470217-080-AD
This helper captures the service mesh version from the integration chart to
annotate the workloads so they are redeployed in case of service mesh upgrade.
*/}}
{{- define "eric-oss-flow-automation.service-mesh-version" }}
{{- if eq (include "eric-oss-flow-automation.service-mesh-enabled" .) "true" }}
  {{- if .Values.global.serviceMesh -}}
    {{- if .Values.global.serviceMesh.annotations -}}
      {{ .Values.global.serviceMesh.annotations | toYaml }}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
DR-D470217-007-AD This helper defines whether this service enter the Service Mesh or not.
*/}}
{{- define "eric-oss-flow-automation.service-mesh-enabled" }}
  {{- $globalMeshEnabled := "false" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.serviceMesh -}}
        {{- $globalMeshEnabled = .Values.global.serviceMesh.enabled -}}
    {{- end -}}
  {{- end -}}
  {{- $globalMeshEnabled -}}
{{- end -}}

{{/*
check global.security.tls.enabled
*/}}
{{- define "eric-oss-flow-automation.global-security-tls-enabled" -}}
{{- if  .Values.global -}}
  {{- if  .Values.global.security -}}
    {{- if  .Values.global.security.tls -}}
      {{- .Values.global.security.tls.enabled | toString -}}
    {{- else -}}
      {{- "false" -}}
    {{- end -}}
  {{- else -}}
    {{- "false" -}}
  {{- end -}}
{{- else -}}
  {{- "false" -}}
{{- end -}}
{{- end -}}

{{/*
This helper defines the annotation for define service mesh volume.
*/}}
{{- define "eric-oss-flow-automation.service-mesh-volume" }}
{{- if and (eq (include "eric-oss-flow-automation.service-mesh-enabled" .) "true") (eq (include "eric-oss-flow-automation.global-security-tls-enabled" .) "true") }}
sidecar.istio.io/userVolume: '{"eric-oss-flow-automation-certs-ca-tls":{"secret":{"secretName":"eric-sec-sip-tls-trusted-root-cert"}},"eric-oss-flow-automation-db-pg-certs-tls":{"secret":{"secretName":"{{ include "eric-oss-flow-automation.name" . }}-db-pg-secret","optional":true}},"eric-oss-flow-automation-bro-certs-tls":{"secret":{"secretName":"{{ include "eric-oss-flow-automation.name" . }}-bro-secret","optional":true}}}'
sidecar.istio.io/userVolumeMount: '{"eric-oss-flow-automation-certs-ca-tls":{"mountPath":"/etc/istio/tls-ca","readOnly":true},"eric-oss-flow-automation-db-pg-certs-tls":{"mountPath":"/etc/istio/tls/flow-automation-db-pg/","readOnly":true},"eric-oss-flow-automation-bro-certs-tls":{"mountPath":"/etc/istio/tls/eric-ctrl-bro/","readOnly":true}}'
{{ end }}
{{- end -}}

{{- define "eric-oss-flow-automation.db-pg-service-mesh-secret" }}
apiVersion: siptls.sec.ericsson.com/v1
kind: InternalCertificate
metadata:
  name: {{ include "eric-oss-flow-automation.name" . }}-db-pg-int-cert
  labels:
  {{- include "eric-oss-flow-automation.labels" .| nindent 4 }}
  annotations:
  {{- include "eric-oss-flow-automation.annotations" .| nindent 4 }}
spec:
  kubernetes:
    generatedSecretName: {{ include "eric-oss-flow-automation.name" . }}-db-pg-secret
    certificateName: "cert.pem"
    privateKeyName: "key.pem"
  certificate:
    subject:
      cn: postgres
    issuer:
      reference: {{ include "eric-oss-flow-automation.name" . }}-db-pg-client-ca
    extendedKeyUsage:
      tlsClientAuth: true
      tlsServerAuth: false
{{- end -}}

{{- define "eric-oss-flow-automation.bragent-service-mesh-secret" }}
apiVersion: siptls.sec.ericsson.com/v1
kind: InternalCertificate
metadata:
  name: {{ include "eric-oss-flow-automation.name" . }}-bro-int-cert
  labels:
  {{- include "eric-oss-flow-automation.labels" .| nindent 4 }}
  annotations:
  {{- include "eric-oss-flow-automation.annotations" .| nindent 4 }}
spec:
  kubernetes:
    generatedSecretName: {{ include "eric-oss-flow-automation.name" . }}-bro-secret
    certificateName: "cert.pem"
    privateKeyName: "key.pem"
  certificate:
    subject:
      cn: {{ include "eric-oss-flow-automation.name" . }}
    issuer:
      reference: eric-ctrl-bro-ca
    extendedKeyUsage:
      tlsClientAuth: true
      tlsServerAuth: true
{{- end -}}

{{/*
Define the log streaming method (DR-470222-010)
*/}}
{{- define "eric-oss-flow-automation.streamingMethod" -}}
{{- $streamingMethod := "direct" -}}
{{- if .Values.log -}}
  {{- if .Values.log.streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod }}
  {{- end -}}
{{- end -}}
{{- if .Values.global -}}
  {{- if .Values.global.log -}}
      {{- if .Values.global.log.streamingMethod -}}
        {{- $streamingMethod = .Values.global.log.streamingMethod }}
      {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define the label needed for reaching eric-log-transformer (DR-470222-010)
*/}}
{{- define "eric-oss-flow-automation.directStreamingLabel" -}}
{{- $streamingMethod := (include "eric-oss-flow-automation.streamingMethod" .) -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) }}
logger-communication-type: "direct"
{{- end -}}
{{- end -}}

{{/*
Define logging environment variables (DR-470222-010)
*/}}
{{ define "eric-oss-flow-automation.loggingEnv" }}
{{- $streamingMethod := (include "eric-oss-flow-automation.streamingMethod" .) -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) -}}
  {{- if eq "direct" $streamingMethod }}
- name: STREAMING_METHOD
  value: {{ $streamingMethod | quote }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-http.xml"
  {{- end }}
  {{- if eq "dual" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-dual.xml"
  {{- end }}
- name: LOGSTASH_DESTINATION
  value: {{ .Values.log.logtransformer.host | quote }}
- name: LOGSTASH_PORT
  value: "9080"
- name: SERVICE_ID
  value: {{ include "eric-oss-flow-automation.name" . | quote }}
- name: POD_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
- name: POD_UID
  valueFrom:
    fieldRef:
      fieldPath: metadata.uid
- name: NODE_NAME
  valueFrom:
    fieldRef:
      fieldPath: spec.nodeName
- name: NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
{{- else if eq $streamingMethod "indirect" }}
- name: STREAMING_METHOD
  value: {{ $streamingMethod | quote }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-json.xml"
- name: SERVICE_ID
  value: {{ include "eric-oss-flow-automation.name" . | quote }}
{{- else }}
  {{- fail ".log.streamingMethod unknown" }}
{{- end -}}
{{ end }}

{{/*
This helper defines which out-mesh services are reached by the <service-name>.
*/}}
{{- define "eric-oss-flow-automation.service-mesh-ism2osm-labels" }}
{{- if eq (include "eric-oss-flow-automation.service-mesh-enabled" .) "true" }}
  {{- if eq (include "eric-oss-flow-automation.global-security-tls-enabled" .) "true" }}
eric-oss-flow-automation-db-pg-ism-access: "true"
eric-ctrl-bro-ism-access: "true"
  {{- end }}
{{- end -}}
{{- end -}}

