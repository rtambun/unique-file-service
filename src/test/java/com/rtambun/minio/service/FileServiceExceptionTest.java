package com.rtambun.minio.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FileServiceExceptionTest {

    @ParameterizedTest
    @ValueSource(ints = {FileServiceException.FILE_CANT_BE_READ,
            FileServiceException.CONNECTION_ISSUE,
            FileServiceException.FILE_NAME_NOT_PROVIDED,
            FileServiceException.FILE_CANT_BE_FOUND})
    public void allArgsConstructor(int status) {
        FileServiceException fileServiceException = new FileServiceException(status);
        assertThat(fileServiceException.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @MethodSource(value = "getData_getResponseEntityForByteArray")
    public void getResponseEntityForByteArray(int status, HttpStatus expectedHttpStatus) {
        HttpStatus actualHttpStatus = FileServiceException.mapExceptionToHttpStatus(
                new FileServiceException(status), null);
        assertThat(actualHttpStatus).isEqualTo(expectedHttpStatus);
    }

    static Stream<Arguments> getData_getResponseEntityForByteArray() {
        return Stream.of(Arguments.of(FileServiceException.FILE_CANT_BE_READ, HttpStatus.NOT_FOUND),
                Arguments.of(FileServiceException.CONNECTION_ISSUE, HttpStatus.INTERNAL_SERVER_ERROR),
                Arguments.of(FileServiceException.FILE_NAME_NOT_PROVIDED, HttpStatus.UNPROCESSABLE_ENTITY),
                Arguments.of(FileServiceException.FILE_CANT_BE_FOUND, HttpStatus.NOT_FOUND),
                Arguments.of(5, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}