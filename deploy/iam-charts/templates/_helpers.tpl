{{/* vim: set filetype=mustache: */}}

{{/* ----- IAM WEB definitions. ----- */}}

{{/* Expand the name of the chart. */}}
{{- define "iam-web.name" -}}
{{- printf "iam-web" }}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "iam-web.fullname" -}}
{{- printf "iam-web-%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/* Create chart name and version as used by the chart label. */}}
{{- define "iam-web.chart" -}}
{{- printf "iam-web-%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/* ----- IAM FACADE definitions. ----- */}}

{{- define "iam-facade.name" -}}
{{- printf "iam-facade" }}
{{- end -}}

{{- define "iam-facade.fullname" -}}
{{- printf "iam-facade-%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "iam-facade.chart" -}}
{{- printf "iam-facade-%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/* ----- IAM DATA definitions. ----- */}}

{{- define "iam-data.name" -}}
{{- printf "iam-data" }}
{{- end -}}

{{- define "iam-data.fullname" -}}
{{- printf "iam-data-%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "iam-data.chart" -}}
{{- printf "iam-data-%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
