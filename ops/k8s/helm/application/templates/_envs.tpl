{{/*
Custom envs
*/}}

{{/* Service spread - ensuring only a certain amount of services will be hosted by a single kubernetes node.  */}}
{{ define "application.topologySpreadConstraints" -}}
- maxSkew: {{ .Values.maxSkew | default 6 }}
  topologyKey: kubernetes.io/hostname
  whenUnsatisfiable: DoNotSchedule
  labelSelector:
    matchLabels:
      release: {{ .Release.Name }}
{{ end -}}