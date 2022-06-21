package com.rtambun.minio.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationPropertiesTest {

    private Environment environment;
    private ApplicationProperties applicationProperties;

    @BeforeEach
    public void setUp() {
        environment = mock(Environment.class);
        applicationProperties = new ApplicationProperties(environment);
    }

    @Test
    public void getConfigValue_ok() {
        when(environment.getProperty(any())).thenReturn("test");

        String actual = applicationProperties.getConfigValue("not test");

        verify(environment, times(1)).getProperty("not test");

        assertThat(actual).isEqualTo("test");
    }
}