#!/bin/bash
ASSEMBLY_DIR=`dirname "$0"`

# check for the keystore
[ -f "$ASSEMBLY_DIR/glimpse-server-keystore.pkcs12" ] || ( echo Must have keystore at glimpse-server-keystore.pkcs12 && exit )

# check for password argument
[ -z "$1" ] && echo Must provide keystore password as first argument && exit

mvn -f "${ASSEMBLY_DIR}/../../pom.xml" clean install && mvn -f "${ASSEMBLY_DIR}/pom.xml" clean package -DglimpseKeystorePassword=$1
