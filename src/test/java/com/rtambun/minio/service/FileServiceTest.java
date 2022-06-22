package com.rtambun.minio.service;

import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.rtambun.minio.model.FileMap;
import com.rtambun.minio.repository.FileMapRepository;
import com.rtambun.minio.util.UUIDProvider;
import com.rtambun.minio.dto.FileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static java.nio.file.Path.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileServiceTest {

    private FileMapRepository mockFileMapRepository;
    private MinioService mockMinioService;
    private UUIDProvider mockUuidProvider;
    private FileService fileService;
    private MockMultipartFile mockMultipartFile;

    @BeforeEach
    public void setUp() {
        mockFileMapRepository = mock(FileMapRepository.class);
        mockMinioService = mock(MinioService.class);
        mockUuidProvider = mock(UUIDProvider.class);
        String url = "http://test/";
        fileService = new FileService(mockFileMapRepository, mockMinioService, mockUuidProvider, url);

        mockMultipartFile = mock(MockMultipartFile.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void addFile_MultiPart_FileNameIsEmptyOrNull(String fileName) throws MinioException {
        when(mockMultipartFile.getOriginalFilename()).thenReturn(fileName);

        FileServiceException ex = assertThrows(FileServiceException.class,
                () -> fileService.addFile("incidentId", mockMultipartFile));

        assertThat(ex.getStatus()).isEqualTo(FileServiceException.FILE_NAME_NOT_PROVIDED);

        verify(mockFileMapRepository, times(0))
                .findFileMapByMappedFileName(anyString());
        verify(mockFileMapRepository, times(0))
                .findFileMapByIncidentIdAndFileName(anyString(), anyString());
        verify(mockMinioService, times(0)).upload(
                any(), any(), anyString(), any());
    }

    @ParameterizedTest
    @NullSource
    @MethodSource(value = "getData_addFile_FileMappingForGivenIncidentIdAndFileName_NotFound")
    void addFile_FileMappingForGivenIncidentIdAndFileName_NotFound_CreateEntryOnDatabase_AndUploadFile(FileMap fileMap)
            throws MinioException, FileServiceException {

        UUID uuid = new UUID(0, 0);
        String expectedMappedFileName = uuid + ".jpg";
        when(mockUuidProvider.randomUUID()).thenReturn(uuid);

        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);
        when(mockFileMapRepository.findFileMapByMappedFileName(any())).thenReturn(fileMap);

        when(mockMultipartFile.getOriginalFilename()).thenReturn("fileName.jpg");
        when(mockMultipartFile.getContentType()).thenReturn("image");

        FileResponse actual =  fileService.addFile("incidentId", mockMultipartFile);

        verify(mockFileMapRepository, times(1))
                .findFileMapByMappedFileName(expectedMappedFileName);
        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "fileName.jpg");

        ArgumentCaptor<FileMap> fileMapArgumentCaptor = ArgumentCaptor.forClass(FileMap.class);
        verify(mockFileMapRepository, times(1)).save(fileMapArgumentCaptor.capture());
        FileMap expectedFileMap = new FileMap(null,
                "incidentId",
                "fileName.jpg",
                expectedMappedFileName);
        assertThat(fileMapArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedFileMap);

        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(mockMinioService, times(1)).upload(
                pathArgumentCaptor.capture(),
                eq(null),
                eq("image"),
                any());

        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo(expectedMappedFileName);

        FileResponse expected = new FileResponse("fileName.jpg", null);
        assertThat(actual).isEqualTo(expected);
    }

    public static Stream<Arguments> getData_addFile_FileMappingForGivenIncidentIdAndFileName_NotFound() {
        return Stream.of(Arguments.of(new FileMap()));
    }

    @Test
    void addFile_IncidentIdNull_UploadFileOnly()
            throws MinioException, FileServiceException {

        UUID uuid = new UUID(0, 0);
        String expectedMappedFileName = uuid + ".jpg";
        when(mockUuidProvider.randomUUID()).thenReturn(uuid);

        when(mockMultipartFile.getOriginalFilename()).thenReturn("fileName.jpg");
        when(mockMultipartFile.getContentType()).thenReturn("image");

        FileResponse actual = fileService.addFile(null, mockMultipartFile);

        verify(mockFileMapRepository, times(0))
                .findFileMapByIncidentIdAndFileName(any(), any());
        verify(mockFileMapRepository, times(0))
                .findFileMapByMappedFileName(any());

        verify(mockFileMapRepository, times(0)).save(any());

        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(mockMinioService, times(1)).upload(
                pathArgumentCaptor.capture(),
                eq(null),
                eq("image"),
                any());

        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo("fileName.jpg");

        FileResponse expected = new FileResponse("fileName.jpg", null);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void addFile_FileMappingForGivenIncidentIdAndFileName_Found_UploadFileOnly()
            throws MinioException, FileServiceException {

        UUID uuid = new UUID(0, 0);
        String expectedMappedFileName = uuid + ".jpg";
        when(mockUuidProvider.randomUUID()).thenReturn(uuid);

        FileMap fileMap = new FileMap("id",
                "incidentId",
                "fileName.jpg",
                expectedMappedFileName);
        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);

        when(mockMultipartFile.getOriginalFilename()).thenReturn("fileName.jpg");
        when(mockMultipartFile.getContentType()).thenReturn("image");

        FileResponse actual = fileService.addFile("incidentId", mockMultipartFile);

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "fileName.jpg");
        verify(mockFileMapRepository, times(0))
                .findFileMapByMappedFileName(any());

        verify(mockFileMapRepository, times(0)).save(any());

        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(mockMinioService, times(1)).upload(
                pathArgumentCaptor.capture(),
                eq(null),
                eq("image"),
                any());

        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo(expectedMappedFileName);

        FileResponse expected = new FileResponse("fileName.jpg", null);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void addFile_FileMappingForGivenIncidentIdAndFileName_Found_UploadFileException()
            throws MinioException {

        UUID uuid = new UUID(0, 0);
        String expectedMappedFileName = uuid + ".jpg";
        when(mockUuidProvider.randomUUID()).thenReturn(uuid);

        FileMap fileMap = new FileMap("id",
                "incidentId",
                "fileName.jpg",
                expectedMappedFileName);
        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);

        when(mockMultipartFile.getOriginalFilename()).thenReturn("fileName.jpg");
        when(mockMultipartFile.getContentType()).thenReturn("image");

        doThrow(new MinioException("test", new Throwable()))
                .when(mockMinioService)
                .upload(any(), any(), anyString(), any());

        FileServiceException ex = assertThrows(FileServiceException.class,
                () -> fileService.addFile("incidentId", mockMultipartFile));
        assertThat(ex.getStatus()).isEqualTo(FileServiceException.CONNECTION_ISSUE);

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "fileName.jpg");
        verify(mockFileMapRepository, times(0))
                .findFileMapByMappedFileName(any());

        verify(mockFileMapRepository, times(0)).save(any());

        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(mockMinioService, times(1)).upload(
                pathArgumentCaptor.capture(),
                eq(null),
                eq("image"),
                any());

        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo(expectedMappedFileName);
    }

    @Test
    void getFile_FileNotMapped_GetFileUsingOriginalName() throws FileServiceException, MinioException {

        FileMap fileMap = null;
        when(mockFileMapRepository.findFileMapByMappedFileName(any())).thenReturn(fileMap);
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockMinioService.get(any())).thenReturn(inputStream);

        FileResponse actual = fileService.getFile(null, "fileName.jpg");

        FileResponse expected = new FileResponse("fileName.jpg", new byte[0]);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(mockMinioService, times(1)).get(pathArgumentCaptor.capture());
        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo("fileName.jpg");
    }

    @Test
    void getFile_FileMapped_GetFileUsingMappedName() throws FileServiceException, MinioException {

        FileMap fileMap = new FileMap("id", "incidentId", "fileName.jpg", "mappedFileName.jpg");
        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockMinioService.get(any())).thenReturn(inputStream);

        FileResponse actual = fileService.getFile("incidentId", "fileName.jpg");

        FileResponse expected = new FileResponse("fileName.jpg", new byte[0]);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "fileName.jpg");
        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(mockMinioService, times(1)).get(pathArgumentCaptor.capture());
        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo("mappedFileName.jpg");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource(value = "getData_getFile_FileMapped_GetFileUsingMappedName_FileMapNotFoundOnDatabase")
    void getFile_FileMapped_GetFileUsingMappedName_FileMapNotFoundOnDatabase(FileMap fileMap)
            throws MinioException, FileServiceException {

        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        when(mockMinioService.get(any())).thenReturn(inputStream);

        FileResponse fileResponse = fileService.getFile("incidentId", "fileName.jpg");

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "fileName.jpg");
        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        //When file not found on the mapped file it will try to read file using original file name.
        verify(mockMinioService, times(1)).get(pathArgumentCaptor.capture());
        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo("fileName.jpg");
    }

    static Stream<Arguments> getData_getFile_FileMapped_GetFileUsingMappedName_FileMapNotFoundOnDatabase () {
        return Stream.of(
                Arguments.of(new FileMap()),
                Arguments.of(new FileMap("", null, null, null)));
    }

    @ParameterizedTest
    @MethodSource(value = "getData_getFile_FileMapped_GetFileFailed")
    void getFile_FileMapped_GetFileFailed_ThrowFileServiceException(Exception ex) throws MinioException {

        FileMap fileMap = new FileMap("id", "incidentId", "fileName.jpg", "mappedFileName.jpg");
        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);
        when(mockMinioService.get(any())).thenThrow(ex);

        FileServiceException fex = assertThrows(FileServiceException.class,
                () -> fileService.getFile("incidentId", "mappedFileName.jpg"));
        assertThat(fex.getStatus()).isEqualTo(FileServiceException.FILE_CANT_BE_READ);

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "mappedFileName.jpg");
        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(mockMinioService, times(1)).get(pathArgumentCaptor.capture());
        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo("mappedFileName.jpg");
    }

    public static Stream<Arguments> getData_getFile_FileMapped_GetFileFailed() {
        return Stream.of(Arguments.of(new MinioException("test", new Throwable())));
    }

    @Test
    void getFile_FileMapped_GetFileFailed_ThrowIOServiceException() throws MinioException, IOException {

        FileMap fileMap = new FileMap("id", "incidentId", "fileName.jpg", "mappedFileName.jpg");
        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);

        InputStream inputStream = new FileInputStream(new ClassPathResource("circle-black-simple.png").getFile());
        inputStream.close();
        when(mockMinioService.get(any())).thenReturn(inputStream);

        FileServiceException fex = assertThrows(FileServiceException.class,
                () -> fileService.getFile("incidentId", "mappedFileName.jpg"));
        assertThat(fex.getStatus()).isEqualTo(FileServiceException.CONNECTION_ISSUE);

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "mappedFileName.jpg");
        ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
        verify(mockMinioService, times(1)).get(pathArgumentCaptor.capture());
        assertThat(pathArgumentCaptor.getValue().toString()).isEqualTo("mappedFileName.jpg");
    }

    @Test
    void deleteFile_IncidentIdIsNull_DeleteUsingFileName()
            throws FileServiceException, MinioException {

        FileMap fileMap = new FileMap("id",
                "incidentId",
                "fileName",
                "mappedFileName");
        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);

        FileResponse actual = fileService.deleteFile(null, "test.jpg");

        FileResponse expected = new FileResponse("test.jpg", null);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockFileMapRepository, times(0))
                .findFileMapByIncidentIdAndFileName(any(), anyString());
        verify(mockMinioService, times(1)).remove(of("test.jpg"));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource(value = "getData_deleteFile_FileIsNotInTheFileMapRepository_DeleteUsingFileName")
    void deleteFile_FileIsNotInTheFileMapRepository_DeleteUsingFileName(FileMap fileMap)
            throws FileServiceException, MinioException {

        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);

        FileResponse actual = fileService.deleteFile("incidentId", "test.jpg");

        FileResponse expected = new FileResponse("test.jpg", null);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "test.jpg");
        verify(mockMinioService, times(1)).remove(of("test.jpg"));
    }

    static Stream<Arguments> getData_deleteFile_FileIsNotInTheFileMapRepository_DeleteUsingFileName() {
        return Stream.of(
                Arguments.of(new FileMap(null, "", "", "")),
                Arguments.of(new FileMap("", "", "", "")));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource(value = "getData_deleteFile_FileIsNotInTheFileMapRepository_DeleteUsingFileName")
    void deleteFile_FileIsNotInTheFileMapRepository_DeleteUsingFileName_ThrowException(FileMap fileMap)
            throws MinioException {

        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any())).thenReturn(fileMap);
        doThrow(new MinioException("any", null)).when(mockMinioService).remove(any());

        FileServiceException ex = assertThrows(FileServiceException.class,
                () -> fileService.deleteFile("incidentId", "test.jpg"));

        assertThat(ex.getStatus()).isEqualTo(FileServiceException.FILE_CANT_BE_FOUND);

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId","test.jpg");
        verify(mockMinioService, times(1)).remove(of("test.jpg"));
    }

    @Test
    void deleteFile_FileIsInFileMapRepository_DeleteUsingMappedFileName()
            throws FileServiceException, MinioException {

        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any()))
                .thenReturn(new FileMap("id",
                        "incidentId",
                        "test.jpg",
                        "mappedFileName.jpg"));

        FileResponse actual = fileService.deleteFile("incidentId", "test.jpg");

        FileResponse expected = new FileResponse("test.jpg", null);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "test.jpg");
        verify(mockMinioService, times(1)).remove(of("mappedFileName.jpg"));
    }

    @Test
    void deleteFile_FileIsInFileMapRepository_DeleteUsingMappedFileNameThrowException()
            throws MinioException {

        when(mockFileMapRepository.findFileMapByIncidentIdAndFileName(any(), any()))
                .thenReturn(new FileMap("id",
                        "incidentId",
                        "test.jpg",
                        "mappedFileName.jpg"));
        doThrow(new MinioException("any", null)).when(mockMinioService).remove(any());

        FileServiceException ex = assertThrows(FileServiceException.class,
                () -> fileService.deleteFile("incidentId", "test.jpg"));

        verify(mockFileMapRepository, times(1))
                .findFileMapByIncidentIdAndFileName("incidentId", "test.jpg");
        verify(mockMinioService, times(1)).remove(of("mappedFileName.jpg"));
    }

}