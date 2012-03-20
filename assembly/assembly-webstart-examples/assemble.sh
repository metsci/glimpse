#!/bin/bash
ASSEMBLY_DIR=`dirname "$0"`

# check for the keystore
[ -f "$ASSEMBLY_DIR/glimpse.keystore" ] || ( echo Must have keystore at glimpse.keystore && exit )

# check for password argument
[ -z "$1" ] && echo Must provide keystore password as first argument && exit

mvn -f "${ASSEMBLY_DIR}/../../pom.xml" clean install && mvn -f "${ASSEMBLY_DIR}/pom.xml" clean package -DglimpseKeystorePassword=$1
