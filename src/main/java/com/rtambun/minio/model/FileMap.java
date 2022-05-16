package com.rtambun.minio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "file_map")
public class FileMap {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String incidentId;

    @Field(type = FieldType.Text)
    private String fileName;

    @Field(type = FieldType.Text)
    private String mappedFileName;
}
