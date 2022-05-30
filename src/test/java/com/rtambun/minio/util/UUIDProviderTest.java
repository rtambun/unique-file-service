package com.rtambun.minio.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UUIDProviderTest {

    @Test
    public void randomUUID_ok() {
        UUIDProvider uuidProvider = new UUIDProvider();
        assertThat(uuidProvider.randomUUID().toString()).isNotNull();
    }

}