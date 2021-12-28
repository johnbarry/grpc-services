GITDIR=$HOME/git
GRPC_HOST=$(kubectl get services grpc-demo-grpc | awk '{print $4}' | tail -n +2)
cd "${GITDIR}"/grpc-services/proto/src/main/proto/org/jpb/grpcservice || exit
PROTOFILE=jpbservice.proto
echo "-----------------"
echo "proto..."
cat $PROTOFILE
echo "-----------------"
echo "f1..."
"${GITDIR}"/grpc/cmake/build/grpc_cli call "${GRPC_HOST}":50051 f1 "number: 20" --protofiles=$PROTOFILE
echo "-----------------"
echo "f2..."
"${GITDIR}"/grpc/cmake/build/grpc_cli call "${GRPC_HOST}":50051 f2 "number: 50" --protofiles=$PROTOFILE
cd "${GITDIR}"/grpc-services/scripts/client || exit

