@echo off
REM creates pdf and html documentation


echo Building Fop documentation (pdf,html)
echo ----------------

if "%JAVA_HOME%" == "" goto error

set LIBDIR=..\..\lib
set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\lib\classes.zip;%LIBDIR%\ant.jar;%LIBDIR%\batik.jar;%LIBDIR%\buildtools.jar;%LIBDIR%\xerces-1.2.3.jar;%LIBDIR%\xalan-1.2.2.jar;%LIBDIR%\bsf.jar;%LIBDIR%\avalon-framework-4.0.jar;%LIBDIR%\logkit-1.0b4.jar;%LIBDIR%\jimi-1.0.jar;%LIBDIR%\stylebook.jar;%LIBDIR%\..\build\fop.jar
set ANT_HOME=%LIBDIR%


%JAVA_HOME%\bin\java.exe -Dant.home=%ANT_HOME% -classpath %LOCALCLASSPATH%;%CLASSPATH% org.apache.tools.ant.Main %1 %2 %3 %4 %5


%JAVA_HOME%\bin\java.exe -classpath %LOCALCLASSPATH%;%CLASSPATH% org.apache.stylebook.StyleBook "targetDirectory=../html-docs/" fop.xml style/

goto end

:error

echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end

