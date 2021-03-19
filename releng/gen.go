package main

import (
	"bytes"
	"fmt"
	"github.com/redhat-developer/knative-jsongenerator/jsonschema"
	v1 "knative.dev/serving/pkg/apis/serving/v1"
	"os"
	"encoding/json"
)

func dump(v interface{}, apiVersion string, kind string) {
	fmt.Printf("Starting generation of %s %s\n", apiVersion, kind)
	filename := fmt.Sprintf("%s_%s.json", apiVersion, kind)
	reflector := jsonschema.Reflector{}
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
}
