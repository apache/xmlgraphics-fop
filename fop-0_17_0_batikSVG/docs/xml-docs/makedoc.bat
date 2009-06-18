REM creates pdf and html documentation
@echo off

echo Building Fop documentation (pdf,html)
echo ----------------

if "%JAVA_HOME%" == "" goto error

set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\lib\classes.zip;..\..\lib\ant.jar;..\..\lib\w3c.jar;..\..\lib\buildtools.jar;..\..\build\fop.jar;..\..\lib\stylebook.jar
set ANT_HOME=.\lib


%JAVA_HOME%\bin\java.exe -Dant.home=%ANT_HOME% -classpath %LOCALCLASSPATH%;%CLASSPATH% org.apache.tools.ant.Main %1 %2 %3 %4 %5


rem %JAVA_HOME%\bin\java.exe -classpath %LOCALCLASSPATH%;%CLASSPATH% org.apache.stylebook.StyleBook "targetDirectory=../html-docs/" fop.xml style/

goto end

:error

echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end

rem set LOCALCLASSPATH=



