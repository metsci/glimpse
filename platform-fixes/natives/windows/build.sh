#!/bin/bash

set -euo pipefail


mkdir -p "build"
"$JAVA_HOME/bin/javah" -classpath "../../target/classes" -d "build" "com.metsci.glimpse.platformFixes.WindowsFixes"


mkdir -p "build/64bit"
x86_64-w64-mingw32-g++ -std=c++11 -I"./build" -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/win32" -c "windowsFixes.cc" -o "build/64bit/windowsFixes.o"
x86_64-w64-mingw32-g++ -shared "build/64bit/windowsFixes.o" -o "build/64bit/windowsFixes.dll" -static-libgcc -static-libstdc++ -static -lpthread
cp "build/64bit/windowsFixes.dll" "../../src/main/resources/platformFixes/windows64"


mkdir -p "build/32bit"
i686-w64-mingw32-g++ -std=c++11 -I"./build" -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/win32" -c "windowsFixes.cc" -o "build/32bit/windowsFixes.o"
i686-w64-mingw32-g++ -Wl,--kill-at -shared "build/32bit/windowsFixes.o" -o "build/32bit/windowsFixes.dll" -static-libgcc -static-libstdc++ -static -lpthread
cp "build/32bit/windowsFixes.dll" "../../src/main/resources/platformFixes/windows32"

