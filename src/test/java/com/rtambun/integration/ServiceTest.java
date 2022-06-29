package com.rtambun.integration;

import com.rtambun.minio.DummyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ServiceTest extends InitIntegrationTest{

    @Autowired
    private DummyService service = new DummyService();
    @Test
    public void testHelloWorld()
    {
        assertSame("Hello World!", service.helloWorld());
    }
}
