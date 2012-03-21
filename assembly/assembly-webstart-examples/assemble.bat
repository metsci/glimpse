@echo off
set ASSEMBLY_DIR=%~dp0
if "%ASSEMBLY_DIR:~-1%" == "\" set ASSEMBLY_DIR=%ASSEMBLY_DIR:~0,-1%

rem check for the keystore
if not exist "%ASSEMBLY_DIR%\glimpse-server-keystore.pkcs12" echo Must have keystore at glimpse.keystore && exit /B 1

rem check for password argument
if "%~1" == "" echo Must provide keystore password as first argument && exit /B 1

mvn -f "%ASSEMBLY_DIR%\..\..\pom.xml" clean install && mvn -f "%ASSEMBLY_DIR%\pom.xml" clean package -DglimpseKeystorePassword=%~1
