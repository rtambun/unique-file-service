This project is example of using elastic search and also minio s3. The purpose of this service is to map a file to 
incident id. It will allow same file name for different incident id to be uniquely stored. Elastic search contains a 
document that stored the mapping of the file name and incident id to a unique id. File will be stored using unique id and 
can be retrieved the same way. This service of mapping file with incident id is introduced on v2. It also has v1 service 
where file is stored as it is.

To build this service one dependency, com.jlefebure:spring-boot-starter-minio:1.11-SNAPSHOT, that is not available in 
maven central. To have it installed on local maven, go to github, https://github.com/rtambun/spring-boot-starter-minio, 
and clone it. Then from the home directory of the project, run command mvn clean install. It will install required jar 
to maven local.