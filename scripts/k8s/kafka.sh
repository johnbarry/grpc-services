kubectl create namespace kafka
#kubectl label namespace kafka istio-injection=enabled --overwrite  
kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
kubectl apply -f https://strimzi.io/examples/latest/kafka/kafka-ephemeral-single.yaml -n kafka
