g++ -std=c++11 -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -c wheelfix.cc -o wheelfix.o
g++ -static -shared wheelfix.o -o wheelfix.dll