@echo off

echo Fop Test
echo ----------------

if "%JAVA_HOME%" == "" goto error


set LIBDIR=..\..\lib
set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\lib\classes.zip
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\ant-1.5.1.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\xml-apis.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\xercesImpl-2.2.1.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\xalan-2.4.1.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\batik.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\avalon-framework-cvs-20020806.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\jimi-1.0.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\jai_core.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\jai_codec.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\..\build\fop.jar
set ANT_HOME=%LIBDIR%

echo Starting Tests ...

%JAVA_HOME%\bin\java.exe -Dant.home=%ANT_HOME% -classpath %LOCALCLASSPATH%;%CLASSPATH% org.apache.tools.ant.Main %1 %2 %3 %4 %5

goto end

:error

echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end

rem set LOCALCLASSPATH=

