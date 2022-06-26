This project is example of using elastic search and also minio s3. The purpose of unique-file-service is to map a file to 
incident id. It will allow same file name for different incident id to be uniquely stored. Elastic search contains a 
document that stored the mapping of the file name and incident id to a unique id. File will be stored using unique id and 
can be retrieved the same way. This service of mapping file with incident id is introduced on v2. It also has v1 service 
where file is stored as it is.

To build this service one dependency, com.jlefebure:spring-boot-starter-minio:1.11-SNAPSHOT, currently is not available in 
maven central. To have it installed on local maven, go to github, https://github.com/rtambun/spring-boot-starter-minio, 
and clone it. Then from the home directory of the project, run command mvn clean install. It will install required jar 
to maven local.

For validation, it is divided into two parts:
1. Unit tests, under test/java/com/rtambun/minio folder
2. Integration test, under test/java/com/rtambun/integration folder

Integration test is test suite consist of test running completely as spring boot along with elasticsearch and minio 
server. This is achieved using testcontainer, https://www.testcontainers.org. In order for testcontainer to work it will 
required docker to be run on machine that is running the service. 

It also allows you to run the service locally. There are two ways of running it locally. Docker compose is used for this.
Under docker/local folder there are two yml file that can be used to run the service:
1. Docker-compose.yml.
2. Docker-compose-local.yml

Docker-compose.yml is used to start dependency server, minio and elasticsearch. This will allow you to run 
unique-file service separately. This can be used when you wanted to debug this service using ide. 

Docker-compose-local.yml is used to start unique-file-service and all its dependency. This can be used to do 
demo of the service. In order for this yml file to work, image of unique service must be stored in local registry or 
install on remote registry. To build the image use Dockerfile that is created on the root project folder. 

Once service is running locally, swagger documentation can be found here, http://localhost:8086/swagger-ui/index.html.