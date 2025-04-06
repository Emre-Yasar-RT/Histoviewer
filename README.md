# mip2025-03-hlpy

# Project Histoviewer

This application is used as a website to manage histology tissue pictures. The pictures are displayed with the matching description and magnification and the pictures can be opened in two different detail views.
The functionalities are creating tags and comments to the pictures which can both be removed. The tags can also be merged.
There are search fields to find the pictures by the descriptions, the comments and the tags.
It also has the option to filter the pictures according to the color of the tissue in the picture.
The application also provides a night-mode, French and German translations and the picture size can be adjusted with a slider.
If clicking onto the tag-icon a list of all the existing tags is shown.

## Environment versions on the server
* Docker-Version: 28.0.4, build b8034c0
* Orthanc-Version: 1.12.6
* PostgreSQL-Version: PostgreSQL 13.20
* Nginx-Version: nginx/1.27.4


## Full stack

### Frontend
Typescript and React was used to build the frontend. The Material-UI is used as a library and i18next provides the translation.

## Backend
To build the backend Spring Boot and Maven are used. The application provides RESTful APIs for efficient tag and comment management.

## Docker
To containerize everything Docker is utilized, using docker-compose.yml for multi-container management, Dockerfiles for image definition and application.properties for environment specific configurations, to ensure a seamless deployment.

## Database
To build the database the application uses PostgreSQL to efficiently store comments, tags and the additional metadata.

## Authentication
To authenticate that only registered users can access the application Keycloak provides session management for administrators and users. 

## Orthanc
Orthanc serves as the image database (PACS) in this project, used to store all DICOM-formatted images. The source images were converted to DICOM and uploaded automatically via a Python script (convert_and_upload.py). Orthanc runs in a Docker container and is accessible via HTTP (port 8042) and DICOM protocol (port 4242).

## Monitoring
Monitoring is fully containerized and includes the following components:
- Grafana visualizes all metrics via a configured dashboard and integrates with Keycloak for authentication.
- Prometheus scrapes metrics from cAdvisor at regular intervals.
- cAdvisor collects detailed resource usage metrics per container (CPU, memory, disk I/O).
- Blackbox Exporter performs endpoint availability checks (e.g., HTTP/HTTPS uptime).
- Loki is used for centralized log aggregation and querying.

## Installation
The application is deployed through a GitLab CI/CD pipeline with two stages: build and deploy.
During the build stage, the Java backend is compiled using Maven inside a container, and the resulting JAR is packaged.
In the deployment stage, the project files are synced to the remote Debian server via SSH, and docker compose up --build is executed remotely to start or rebuild all services.
The deployment includes backend, frontend, Orthanc, database, and monitoring services as defined in docker-compose.yml.
After the deployment, the database need to be changed from create to validate.

## Tests
The tests were created on the dev branch and the matching test report is in the folder documentation.

## Information for additional metadata XML files in the future
If new XML files for additional metadata of new pictures need to be added, the ID must be continued and not starting at 1 again.


## Documentation folder
The folder Documentation provides files such as the software requirement specification, meeting protocols and a JavaDoc for the Backend.

## References
Official documentation of the components which were used \
https://stackoverflow.com \
https://www.baeldung.com \
https://www.w3schools.com/ \
https://www.youtube.com/@BoualiAli \
https://www.restack.io/p/spring-boot-answer-disable-cors \
https://www.youtube.com/watch?v=AWBSWlM0JmQ \
https://www.youtube.com/watch?v=vZm0lHciFsQ&t=103s \
https://www.youtube.com/watch?v=MztH6vkeFVk \
https://www.youtube.com/watch?v=cTEtSmNOtlE&t=47s \
https://medium.com/innovaccer-design/rgb-vs-hsb-vs-hsl-demystified-1992d7273d3a \
https://www.rapidtables.com/convert/color/hsl-to-rgb.html \
https://codereview.stackexchange.com/questions/115046/fast-way-to-find-the-most-similar-color-in-an-array \
https://dev.to/bytebodger/determining-the-rgb-distance-between-two-colors-4n91 \
https://groups.google.com/g/orthanc-users
https://discourse.orthanc-server.org/
https://orthanc.uclouvain.be/book/plugins/dicomweb.html
https://medium.com/@varunjain2108/monitoring-docker-containers-with-cadvisor-prometheus-and-grafana-d101b4dbbc84
https://www.youtube.com/watch?v=qP8kir2GUgo
https://docs.docker.com/
https://hub.docker.com/

We leveraged Large Language Models to debug issues and gather optimization suggestions for our code.# Histoviewer
