cd istio-*
kubectl apply -f samples/bookinfo/networking/bookinfo-gateway.yaml
cd ..
istioctl analyze

