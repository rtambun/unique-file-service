This project is example of using elastic search and also minio s3. The purpose of this service is to map a file to 
incident id. It will allow same file name for different incident id to be uniquely stored. Elastic search contain a 
document that stored the mappig of the file name and incident id to a unique id. File will be stored using unique id and 
can be retrieved the same way. This service of mapping file with incident id is introduced on v2. It also has v1 service 
where file is stored as it is.



