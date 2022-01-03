GITDIR=$HOME/git
GRPC_HOST=$(kubectl get services demo-grpc-service-grpc | awk '{print $3}' | tail -n +2)
cd "${GITDIR}"/grpc-services/proto/src/main/proto/org/jpb/grpcservice || exit
PROTOFILE=Calc.proto
echo "-----------------"
echo "proto..."
cat $PROTOFILE
echo "-----------------"
#echo "f1..."
#for i in {1..5}
#do
	#"${GITDIR}"/grpc/cmake/build/grpc_cli call "${GRPC_HOST}":50051 f1 "number: $i" # --protofiles=$PROTOFILE
#done
#echo "-----------------"
#echo "f2..."
#for i in {1..5}
#do
	##"${GITDIR}"/grpc/cmake/build/grpc_cli call "${GRPC_HOST}":50051 f2 "number: $i" # --protofiles=$PROTOFILE
	#echo curl -H 'Content-Type: application/json' -H 'Accept: application/json' "http://${GRPC_HOST}:50051/f2?number=$i"
	#curl -H 'Content-Type: application/json' -H 'Accept: application/json' "http://${GRPC_HOST}:50051/f2?number=$i"
	#echo
#done

cd "${GITDIR}"/grpc-services/scripts/client
echo "------------------------"
echo "------------------------"
date +"%M:%S:%N"
echo "sending POST to "   "http://$GRPC_HOST:50051/grpcservice.Calc/f1"
curl --http2 --raw -v -X POST \
    "http://$GRPC_HOST:50051/grpcservice.Calc/f1" \
    -H 'content-type: application/json' \
    -d '{
    "number": "200",
    "lineage": {
       "correlationId": "abc"
    }
}'
date +"%M:%S:%N"
exit 0
echo "------------------------"
echo "------------------------"
echo "sending GET to "   "http://$GRPC_HOST:50051/grpcservice.Calc/f1/"
echp curl -v  -H 'Content-Type: application/grpc'  "http://$GRPC_HOST:50051/grpcservice.Calc/f1/number=200"
curl -v --raw -H 'Content-Type: application/grpc'  "http://$GRPC_HOST:50051/grpcservice.Calc/f1/number=200"

echo "------------------------"
echo curl -v  -H 'Content-Type: application/grpc'  "http://$GRPC_HOST:50051/Calc/f1/number=200"
curl -v  -H 'Content-Type: application/grpc'  "http://$GRPC_HOST:50051/Calc/f1/number=200"

echo "------------------------"
echo curl -v  -H 'Content-Type: application/grpc'  "http://$GRPC_HOST:50051/f1/number=200"
curl -v  -H 'Content-Type: application/grpc'  "http://$GRPC_HOST:50051/f1/number=200"
