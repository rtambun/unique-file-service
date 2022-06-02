This folder is used to set up dependency server for isms minio connector to run locally.
However the Dockerfile to build minio/mc to setup the bucket automatically is not working because ip address of
minio/minio that is targeted should be the network where container created. Docker compose is creating network where
minio/mc and minio/minio is located. Using http://127.0.0.1:9000 will not work as minio/mc doesnt have access to localhost
network. Thus for now the bucket shall be created manually.