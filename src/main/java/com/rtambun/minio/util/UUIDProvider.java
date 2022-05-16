package com.rtambun.minio.util;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UUIDProvider {
    public UUID randomUUID() {
        return UUID.randomUUID();
    }
}
