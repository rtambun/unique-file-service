FROM minio/mc
ENTRYPOINT ["/bin/sh", "-c", "mc alias set local http://127.0.0.1:9000 minioadmin minioadmin && mc mb local/isms"]