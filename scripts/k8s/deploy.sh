eval $(minikube -p minikube docker-env)
d=`pwd`
cd ../..
./gradlew bootBuildImage
cd $d || exit
kubectl create namespace number-demo
kubectl config set-context --current --namespace=number-demo
kubectl label namespace number-demo istio-injection=enabled --overwrite
echo "set namespace to .."
kubectl config view --minify | grep namespace:
kubectl apply -f deployment.yaml
