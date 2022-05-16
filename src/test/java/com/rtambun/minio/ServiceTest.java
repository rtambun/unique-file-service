package com.rtambun.minio;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ServiceTest {
    @Autowired
    private DummyService service = new DummyService();
    @Test
    public void testHelloWorld()
    {
        Assert.assertSame("Hello World!", service.helloWorld());
    }
}
