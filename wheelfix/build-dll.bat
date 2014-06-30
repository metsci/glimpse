gcc -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -c wheelfix.c -o wheelfix.o
gcc -shared wheelfix.o -o wheelfix.dll