package com.rtambun.minio.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileResponseTest {

    @Test
    public void noArgsConstructor() {
        FileResponse fileResponse = new FileResponse();
        assertThat(fileResponse.getFileName()).isNull();
        assertThat(fileResponse.getFileContent()).isNull();

        fileResponse.setFileName("fileName");
        byte[] fileContent = new byte[0];
        fileResponse.setFileContent(fileContent);

        assertThat(fileResponse.getFileName()).isEqualTo("fileName");
        assertThat(fileResponse.getFileContent()).isEqualTo(new byte[0]);
    }

    @Test
    public void allArgsConstructor() {
        FileResponse fileResponse = new FileResponse("fileName", new byte[0]);

        assertThat(fileResponse.getFileName()).isEqualTo("fileName");
        assertThat(fileResponse.getFileContent()).isEqualTo(new byte[0]);
    }
}