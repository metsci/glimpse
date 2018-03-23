#!/bin/bash

set -euo pipefail

# JAVA_HOME must point to a JDK with Windows include headers

x86_64-w64-mingw32-gcc \
  -std=c99 \
  -g \
  -Wall \
  -shared \
  -o FileMapperWindows64.dll \
  -I"$JAVA_HOME/include" \
  -I"$JAVA_HOME/include/win32" \
  FileMapperWindows64.c
