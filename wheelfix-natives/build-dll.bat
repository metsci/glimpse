@echo off
g++ -std=c++11 -I".\build" -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -c wheelfix.cc -o build\wheelfix.o
g++ -static -shared build\wheelfix.o -o build\wheelfix.dll