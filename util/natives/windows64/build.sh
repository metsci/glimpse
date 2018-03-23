#!/bin/bash

set -euo pipefail

x86_64-w64-mingw32-gcc \
  -std=c99 \
  -g \
  -Wall \
  -shared \
  -o MappedFile.dll \
  -I"$JAVA_HOME/include" \
  -I"$JAVA_HOME/include/win32" \
  MappedFile.c
