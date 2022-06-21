FROM minio/mc
ENTRYPOINT ["/bin/sh", "-c", "mc alias set local http://minio1:9000 minioadmin minioadmin && mc mb local/isms"]
ENTRYPOINT ["/bin/sh", "-c", "mc alias set local http://minio2:9000 minioadmin minioadmin && mc mb local/isms"]
ENTRYPOINT ["/bin/sh", "-c", "mc alias set local http://minio3:9000 minioadmin minioadmin && mc mb local/isms"]
ENTRYPOINT ["/bin/sh", "-c", "mc alias set local http://minio4:9000 minioadmin minioadmin && mc mb local/isms"]