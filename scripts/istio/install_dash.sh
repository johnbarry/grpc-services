cd istio-*
kubectl apply -f samples/addons
kubectl rollout status deployment/kiali -n istio-system
cd ..
istioctl dashboard kiali
