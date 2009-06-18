@echo off

echo Fop Build System
echo ----------------

if "%JAVA_HOME%" == "" goto error

set LIBDIR=lib
set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\lib\classes.zip;%LIBDIR%\ant.jar;%LIBDIR%\batik.jar;%LIBDIR%\buildtools.jar;%LIBDIR%\xerces-1.4.3.jar;%LIBDIR%\xalan-2.2D11.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\jimi-1.0.jar;%LIBDIR%\avalon-framework-cvs-20020315.jar

set ANT_HOME=%LIBDIR%

echo Building with classpath %LOCALCLASSPATH%

echo Starting Ant...

%JAVA_HOME%\bin\java.exe -Dant.home=%ANT_HOME% -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5

goto end

:error

echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end

rem set LOCALCLASSPATH=

pause;
