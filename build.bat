@echo off
rem $Id$

echo Apache FOP: Build System
echo --------------------------

if "%JAVA_HOME%" == "" goto no_java_home
if "%ANT_HOME%" == "" goto no_ant_home

call %ANT_HOME%\bin\ant.bat %1 %2 %3 %4 %5 %6 %7 %8

goto end

:no_java_home
echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.
goto end

:no_ant_home
echo ERROR: ANT_HOME not found in your environment.
echo Please install Apache Ant first. FOP uses Ant as its build system.
echo You can download Apache Ant from http://ant.apache.org
echo 1. Download and install Ant
echo 2. Set the ANT_HOME environment variable to the directory where you've
echo    placed Ant.
echo 3. Add "%%ANT_HOME%%\bin" to your PATH so the "ant.bat" batch file
echo    can be executed from wherever needed.
echo 4. (optional) Download JUnit and place junit-*.jar in %%ANT_HOME%%\lib.
echo    This enables the JUnit test cases.
echo You will now be directed to the Apache Ant website...
pause
start http://ant.apache.org
goto end

:end
