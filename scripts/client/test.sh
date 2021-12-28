GITDIR=$HOME/git
GRPC_HOST=$(kubectl get services grpc-demo-grpc | awk '{print $3}' | tail -n +2)
cd "${GITDIR}"/grpc-services/proto/src/main/proto/org/jpb/grpcservice || exit
PROTOFILE=jpbservice.proto
echo "-----------------"
echo "proto..."
cat $PROTOFILE
echo "-----------------"
echo "f1..."
for i in {1..1000}
do
	"${GITDIR}"/grpc/cmake/build/grpc_cli call "${GRPC_HOST}":50051 f1 "number: $i" --protofiles=$PROTOFILE
done
echo "-----------------"
echo "f2..."
for i in {1..1000}
do
	"${GITDIR}"/grpc/cmake/build/grpc_cli call "${GRPC_HOST}":50051 f2 "number: $i" --protofiles=$PROTOFILE
done
cd "${GITDIR}"/grpc-services/scripts/client || exit

