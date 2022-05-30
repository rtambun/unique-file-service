package com.rtambun.minio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest
public class ServiceTest {
    @Autowired
    private DummyService service = new DummyService();
    @Test
    public void testHelloWorld()
    {
        assertSame("Hello World!", service.helloWorld());
    }
}
