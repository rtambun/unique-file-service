package com.rtambun.minio.repository;

import com.rtambun.minio.model.FileMap;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMapRepository extends ElasticsearchRepository<FileMap, String> {

    FileMap findFileMapByIncidentIdAndFileName(String incidentId, String fileName);

    FileMap findFileMapByMappedFileName(String mappedFileName);

}
