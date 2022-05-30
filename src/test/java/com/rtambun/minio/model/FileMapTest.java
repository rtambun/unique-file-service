package com.rtambun.minio.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileMapTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void noArgsConstructor() {
        FileMap fileMap = new FileMap();

        assertThat(fileMap.getId()).isNull();
        assertThat(fileMap.getIncidentId()).isNull();
        assertThat(fileMap.getFileName()).isNull();
        assertThat(fileMap.getMappedFileName()).isNull();

        fileMap.setId("id");
        fileMap.setIncidentId("incidentId");
        fileMap.setFileName("fileName");
        fileMap.setMappedFileName("mappedFileName");

        assertThat(fileMap.getId()).isEqualTo("id");
        assertThat(fileMap.getIncidentId()).isEqualTo("incidentId");
        assertThat(fileMap.getFileName()).isEqualTo("fileName");
        assertThat(fileMap.getMappedFileName()).isEqualTo("mappedFileName");
    }

    @Test
    public void allArgsConstructor() {
        FileMap fileMap = new FileMap("id", "incidentId", "fileName", "mappedFileName");

        assertThat(fileMap.getId()).isEqualTo("id");
        assertThat(fileMap.getIncidentId()).isEqualTo("incidentId");
        assertThat(fileMap.getFileName()).isEqualTo("fileName");
        assertThat(fileMap.getMappedFileName()).isEqualTo("mappedFileName");
    }

}

