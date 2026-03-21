FROM releases-docker.jfrog.io/jfrog/artifactory-pro:7.133.15

USER root

COPY --chown=artifactory:artifactory ./jfrog-artifactory-license-patcher-1.0.jar /opt/jfrog/artifactory/app/artifactory/jfrog-artifactory-license-patcher-1.0.jar
COPY --chown=artifactory:artifactory ./setenv.sh /opt/jfrog/artifactory/app/artifactory/tomcat/bin/setenv.sh

ENV EXTRA_JAVA_OPTIONS=/opt/jfrog/artifactory/app/artifactory/jfrog-artifactory-license-patcher-1.0.jar
