#!/bin/bash
ASSEMBLY_DIR=`dirname "$0"`
mvn -f "${ASSEMBLY_DIR}/../../pom.xml" clean install && mvn -f "${ASSEMBLY_DIR}/pom.xml" clean package
