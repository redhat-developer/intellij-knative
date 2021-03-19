package main

import (
	"bytes"
	"fmt"
	"github.com/redhat-developer/knative-jsongenerator/jsonschema"
	"knative.dev/serving/pkg/apis/serving/v1"
	knative "knative.dev/pkg/apis"
	k8sv1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"os"
	"encoding/json"
	"reflect"
)

func arrayOrStringMapper(i reflect.Type) *jsonschema.Type {
	if (i == reflect.TypeOf(k8sv1.Duration{})) {
		return &jsonschema.Type{
			Type: "string",
			Pattern: "^[-+]?([0-9]*(\\.[0-9]*)?(ns|us|µs|μs|ms|s|m|h))+$",
		}
	}
	if (i == reflect.TypeOf(k8sv1.Time{})) {
		return &jsonschema.Type{
			Type: "string",
			Format: "data-time",
		}
	}
	if (i == reflect.TypeOf(knative.VolatileTime{})) {
		return &jsonschema.Type{
			Type: "string",
			Format: "data-time",
		}
	}
	return nil
}

func dump(v interface{}, apiVersion string, kind string) {
	fmt.Printf("Starting generation of %s %s\n", apiVersion, kind)
	filename := fmt.Sprintf("%s_%s.json", apiVersion, kind)
	reflector := jsonschema.Reflector{
		TypeMapper: arrayOrStringMapper,
	}
	reflect := reflector.Reflect(v)
	JSON, _ := reflect.MarshalJSON()
	file, _ := os.Create(filename)
	defer file.Close()
	var out bytes.Buffer
	json.Indent(&out, JSON, "", "  ")
	out.WriteTo(file)
	index, _ := os.OpenFile("index.properties", os.O_WRONLY|os.O_APPEND, 0)
	index.WriteString(filename)
	index.WriteString("\n")
}

func main() {
	os.Create("index.properties")
	os.Mkdir("serving.knative.dev", os.ModePerm)

	dump(&v1.Service{}, "serving.knative.dev/v1", "Service")
	dump(&v1.ServiceList{}, "serving.knative.dev/v1", "ServiceList")
	dump(&v1.Revision{}, "serving.knative.dev/v1", "Revision")
	dump(&v1.RevisionList{}, "serving.knative.dev/v1", "RevisionList")
	dump(&v1.Configuration{}, "serving.knative.dev/v1", "Configuration")
	dump(&v1.ConfigurationList{}, "serving.knative.dev/v1", "ConfigurationList")
	dump(&v1.Route{}, "serving.knative.dev/v1", "Route")
	dump(&v1.RouteList{}, "serving.knative.dev/v1", "RouteList")
}
