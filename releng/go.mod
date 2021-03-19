module github.com/redhat-developer/knative-jsongenerator

go 1.15

require (
	github.com/alecthomas/jsonschema v0.0.0-20200217214135-7152f22193c9
	github.com/iancoleman/orderedmap v0.0.0-20190318233801-ac98e3ecb4b0
	k8s.io/apimachinery v0.19.7
	knative.dev/pkg v0.0.0-20210215165523-84c98f3c3e7a
	knative.dev/serving v0.21.0
)

replace (
	k8s.io/api => k8s.io/api v0.18.8
	k8s.io/apimachinery => k8s.io/apimachinery v0.18.8
	k8s.io/cli-runtime => k8s.io/cli-runtime v0.18.8
	k8s.io/client-go => k8s.io/client-go v0.18.8
)
