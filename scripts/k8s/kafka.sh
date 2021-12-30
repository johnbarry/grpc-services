kubectl create namespace kafka
#kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
#kubectl apply -f https://strimzi.io/examples/latest/kafka/kafka-ephemeral-single.yaml -n kafka
kubectl create -f kafka_deploy.yaml -n kafka
kubectl apply -f kafka-ephemeral-single.yaml -n kafka

