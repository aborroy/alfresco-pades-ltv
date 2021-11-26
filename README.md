## Alfresco PAdES-LTV

This project includes a sample Spring Boot application demonstrating the features of [Alfresco Java SDK](https://github.com/Alfresco/alfresco-java-sdk).

* **Event API** is used to listen to PDF documents created under a given Alfresco Repository folder
* **REST API Wrapper** is used to get document content from Repository and to update the content with a `PAdES-LTV` document

Additionally, [Apache PDFBox examples](https://github.com/apache/pdfbox/tree/trunk/examples) code is used to create the `PAdES-LTV` format.

>> PAdES (PDF Advanced Electronic Signatures) is a set of restrictions and extensions to PDF and ISO 32000-1 making it suitable for Advanced Electronic Signature. PAdES recognizes that digitally-signed documents may be used or archived for many years – even many decades. At any time in the future, in spite of technological and other advances, it must be possible to validate the document to confirm that the signature was valid at the time it was signed – a concept known as Long-Term Validation (LTV).

An external [RFC-3161 TSA](https://en.wikipedia.org/wiki/Trusted_timestamping) is required, using http://tsa.baltstamp.lt by default.

## Local testing

### Build Spring Boot application

Default Maven command can be used.

```
$ mvn clean package
```

Maven will create a running Spring Boot application in `target` folder

### Starting

Existing ACS 7.1 (Community or Enterprise) must be running before starting this application. At least, Alfresco Repository and ActiveMQ services are required.

```
$ java -jar target/alfresco-pades-ltv-1.0.0.jar
```

### Testing

Add a PDF document to Alfresco Repository folder `Shared/Archive` using ACA, Share or some other API.

Alfresco PAdES-LTV will download the PDF, compute the PAdES-LTV and update the original PDF in Alfresco with the result as a major version.


## Deployment with ACS Stack

### Build Docker Image

This project provides a sample `Dockerfile` to build a Spring Boot Docker Image.

```
$ docker build . -t alfresco-pades-ltv
```

### Adding Alfresco PAdES-LTV to Docker Compose

Once Docker Image is available in your local Docker Repository, it can be added as a new service in ACS `docker-compose.yml` file.

```
  alfresco-pades-ltv:
    image: alfresco-pades-ltv
    environment:
      CONTENT_SERVICE_URL: "http://alfresco:8080"
      SPRING_ACTIVEMQ_BROKER_URL: "tcp://activemq:61616"
      TSA_URL: "http://tsa.baltstamp.lt"
```

Start ACS Stack from folder containing `docker-compose.yml` file.

```
$ docker-compose up --build --force-recreate
```
