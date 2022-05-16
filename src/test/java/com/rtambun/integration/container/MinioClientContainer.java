package com.rtambun.integration.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;

public class MinioClientContainer extends GenericContainer<MinioClientContainer>{

    public final static String ALIAS = "local";

    private static MinioClientContainer minioClientContainer;

    private MinioClientContainer(String minioBucket,
                                 String minioRootUser,
                                 String minioRootPassword,
                                 String minioContainerUrl) {
        super("minio/mc");

        // Minio Java SDK uses s3v4 protocol by default, need to specify explicitly for mc
        String createAlias = String.format("mc alias set %s %s %s %s",
                ALIAS, "http://" + minioContainerUrl, minioRootUser, minioRootPassword);
        String createBucket = String.format("mc mb %s/%s", ALIAS, minioBucket);
        String cmd = createAlias + " && " + createBucket;

        withStartupCheckStrategy(new OneShotStartupCheckStrategy());
        withCreateContainerCmdModifier(containerCommand -> containerCommand
                        .withTty(true)
                        .withEntrypoint("/bin/sh", "-c", cmd));
    }

    public static MinioClientContainer startMinioClientContainer(String minioBucket,
                                                  String minioRootUser,
                                                  String minioRootPassword,
                                                  String minioContainerUrl) {
        if (minioClientContainer == null) {
            minioClientContainer = new MinioClientContainer(minioBucket,
                    minioRootUser,
                    minioRootPassword,
                    minioContainerUrl);
            minioClientContainer.start();
        }
        return minioClientContainer;
    }

    public static void stopMinioClientContainer() {
        if (minioClientContainer != null) {
            minioClientContainer.stop();
            minioClientContainer = null;
        }
    }
}
