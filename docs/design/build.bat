
echo NOTE: Do NOT use jdk1.4 - It doesn't work properly
@echo off

echo Design Doc Build System
echo ----------------

if "%JAVA_HOME%" == "" goto error

set LIBDIR=..\..\lib
set TARGET_CLASSPATH=%LIBDIR%\xerces-1.2.3.jar;%LIBDIR%\batik.jar;%LIBDIR%\ant.jar;%LIBDIR%\buildtools.jar;%LIBDIR%\xalan-2.0.0.jar;%LIBDIR%\bsf.jar;..\..\build\fop.jar;%LIBDIR%\logkit-1.0.jar;%LIBDIR%\avalon-framework-4.0.jar;%LIBDIR%\jimi-1.0.jar
set TARGET_CLASSPATH=%TARGET_CLASSPATH%;%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\lib\classes.zip

set ANT_HOME=%LIBDIR%

%JAVA_HOME%\bin\java.exe -classpath "%TARGET_CLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4

goto end

:error

echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end

rem set TARGET_CLASSPATH=

