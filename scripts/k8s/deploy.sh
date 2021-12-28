eval $(minikube -p minikube docker-env)
d=`pwd`
cd ../..
./gradlew bootBuildImage
cd $d || exit
kubectl create namespace jpbtest
kubectl config set-context --current --namespace=jpbtest
kubectl label namespace jpbtest istio-injection=enabled --overwrite
echo "set namespace to .."
kubectl config view --minify | grep namespace:
kubectl apply -f deployment.yaml
