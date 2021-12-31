kubectl delete EnvoyFilter demo-grpc-service-transcoder

echo -n 'apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: demo-grpc-service-transcoder
spec:
  workloadSelector:
    labels:
      app: demo-grpc-service # match your deployment
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        filterChain:
          filter:
            name: envoy.filters.network.http_connection_manager
            subFilter:
              name: envoy.filters.http.router
        portNumber: 50051 # application port
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.http.grpc_json_transcoder
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_json_transcoder.v3.GrpcJsonTranscoder
          services:
          - grpcservice.Calc
          print_options:
            add_whitespace: true
            always_print_enums_as_ints: false
            always_print_primitive_fields: true
            preserve_proto_field_names: false
          convert_grpc_status: true
          proto_descriptor_bin: ' > envoy_filter.yaml

DESC_FILE=$HOME/git/grpc-services/proto/build/descriptors/main.dsc

base64 -w 0 $DESC_FILE >> envoy_filter.yaml


kubectl apply -f envoy_filter.yaml -n number-demo

