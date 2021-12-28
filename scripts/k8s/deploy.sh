eval $(minikube -p minikube docker-env)
d=`pwd`
cd ../..
./gradlew bootBuildImage
cd $d || exit
kubectl apply -f deployment.yaml
