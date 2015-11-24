@echo off

if not exist build mkdir build
javah -classpath ../../target/classes -d build com.metsci.glimpse.platformFixes.WindowsFixes



if not exist build\64bit mkdir build\64bit

x86_64-w64-mingw32-g++ -std=c++11 -I".\build" -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -c windowsFixes.cc -o build\64bit\windowsFixes.o
x86_64-w64-mingw32-g++ -shared build\64bit\windowsFixes.o -o build\64bit\windowsFixes.dll
copy /y build\64bit\windowsFixes.dll ..\..\src\main\resources\platformFixes\windows64

x86_64-w64-mingw32-g++ -print-file-name=libstdc++-6.dll > build\64bit\tmp
set /p LIBSTDCXX_64=<build\64bit\tmp
copy /y "%LIBSTDCXX_64:/=\%" ..\..\src\main\resources\platformFixes\windows64

x86_64-w64-mingw32-g++ -print-file-name=libgcc_s_seh-1.dll > build\64bit\tmp
set /p LIBGCC_64=<build\64bit\tmp
copy /y "%LIBGCC_64:/=\%" ..\..\src\main\resources\platformFixes\windows64



if not exist build\32bit mkdir build\32bit

i686-w64-mingw32-g++ -std=c++11 -I".\build" -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -c windowsFixes.cc -o build\32bit\windowsFixes.o
i686-w64-mingw32-g++ -shared build\32bit\windowsFixes.o -o build\32bit\windowsFixes.dll
copy /y build\32bit\windowsFixes.dll ..\..\src\main\resources\platformFixes\windows32

i686-w64-mingw32-g++ -print-file-name=libwinpthread-1.dll > build\32bit\tmp
set /p LIBPTHREAD_32=<build\32bit\tmp
copy /y "%LIBPTHREAD_32:/=\%" ..\..\src\main\resources\platformFixes\windows32

i686-w64-mingw32-g++ -print-file-name=libstdc++-6.dll > build\32bit\tmp
set /p LIBSTDCXX_32=<build\32bit\tmp
copy /y "%LIBSTDCXX_32:/=\%" ..\..\src\main\resources\platformFixes\windows32

i686-w64-mingw32-g++ -print-file-name=libgcc_s_dw2-1.dll > build\32bit\tmp
set /p LIBGCC_32=<build\32bit\tmp
copy /y "%LIBGCC_32:/=\%" ..\..\src\main\resources\platformFixes\windows32
