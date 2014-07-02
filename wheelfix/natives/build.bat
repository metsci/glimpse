@echo off

javah -classpath ../target/classes -d build com.metsci.glimpse.wheelfix.WheelFix

x86_64-w64-mingw32-g++ -std=c++11 -I".\build" -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -c wheelfix.cc -o build\wheelfix64.o
x86_64-w64-mingw32-g++ -static -shared build\wheelfix64.o -o build\wheelfix64.dll

i686-w64-mingw32-g++ -std=c++11 -I".\build" -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -c wheelfix.cc -o build\wheelfix32.o
i686-w64-mingw32-g++ -static -shared build\wheelfix32.o -o build\wheelfix32.dll