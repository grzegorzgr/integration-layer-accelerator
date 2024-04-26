{{ define "application.topologySpreadConstraints" -}}
- maxSkew: {{ .Values.global.maxSkew | default 6 }}
  topologyKey: kubernetes.io/hostname
  whenUnsatisfiable: DoNotSchedule
  labelSelector:
    matchLabels:
      release: {{ .Release.Name }}
{{ end -}}