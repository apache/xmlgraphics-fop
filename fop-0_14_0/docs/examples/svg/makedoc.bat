@echo off

echo Building Fop documentation (pdf)
echo ----------------

if "%JAVA_HOME%" == "" goto error

set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\lib\classes.zip;..\..\..\lib\ant.jar;..\..\..\lib\xml.jar;..\..\..\lib;..\..\..\build\fop.jar;..\..\..\lib\w3c.jar
set ANT_HOME=..\..\..\lib


%JAVA_HOME%\bin\java.exe -Dant.home=%ANT_HOME% -classpath %LOCALCLASSPATH%;%CLASSPATH% org.apache.tools.ant.Main %1 %2 %3 %4 %5

goto end

:error

echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end

rem set LOCALCLASSPATH=

