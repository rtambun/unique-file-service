This folder is used to set up dependency server for isms minio connector to run locally.
However the Dockerfile to build minio/mc to setup the bucket automatically is not working because ip address of
minio/minio that is targeted should be the network where container created. Docker compose is creating network where
minio/mc and minio/minio is located. Using http://127.0.0.1:9000 will not work as minio/mc doesnt have access to localhost
network. Thus for now the bucket shall be created manually.

This project is using kafka as messaging bus to notify event update from minio when file is updated.
On how to configure the minio server to send notification to kafka server please read minio documentation here:
https://docs.min.io/docs/minio-bucket-notification-guide.html.

TODO: To setup minio client image that will automatically set up minio server to configure the notification event
Following are the command for reference not to go to minio documentation
1. mc alias set {label} http://localhost:9000 minioadmin minioadmin
2. mc admin config get {label} notify_kafka
3. mc admin config set minio notify_kafka topic=minio brokers=kafka:9092 sasl_username= sasl_password= sasl_mechanism=plain client_tls_cert= client_tls_key= tls_client_auth=0 sasl=off tls=off tls_skip_verify=off queue_limit=0 queue_dir= version=
4. mc admin service restart minio
5. mc event add {label}/isms arn:minio:sqs::_:kafka
6. mc event list minio/isms