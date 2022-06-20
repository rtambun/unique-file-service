FROM minio/mc
ENTRYPOINT ["/bin/sh", "-c", "mc alias set local http://minio:9000 minioadmin minioadmin && mc mb local/isms"]