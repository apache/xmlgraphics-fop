@echo off

echo Fop Build System
echo ----------------

if "%JAVA_HOME%" == "" goto error

set LIBDIR=lib
set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\lib\classes.zip
for %%j in ( %LIBDIR%\*.jar ) do set LOCALCLASSPATH=%LOCALCLASSPATH%;%%j

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

rem $Id$

rem ============================================================================
rem                    The Apache Software License, Version 1.1
rem ============================================================================
rem 
rem Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
rem 
rem Redistribution and use in source and binary forms, with or without modifica-
rem tion, are permitted provided that the following conditions are met:
rem 
rem 1. Redistributions of source code must retain the above copyright notice,
rem    this list of conditions and the following disclaimer.
rem 
rem 2. Redistributions in binary form must reproduce the above copyright notice,
rem    this list of conditions and the following disclaimer in the documentation
rem    and/or other materials provided with the distribution.
rem 
rem 3. The end-user documentation included with the redistribution, if any, must
rem    include the following acknowledgment: "This product includes software
rem    developed by the Apache Software Foundation (http://www.apache.org/)."
rem    Alternately, this acknowledgment may appear in the software itself, if
rem    and wherever such third-party acknowledgments normally appear.
rem 
rem 4. The names "FOP" and "Apache Software Foundation" must not be used to
rem    endorse or promote products derived from this software without prior
rem    written permission. For written permission, please contact
rem    apache@apache.org.
rem 
rem 5. Products derived from this software may not be called "Apache", nor may
rem    "Apache" appear in their name, without prior written permission of the
rem    Apache Software Foundation.
rem 
rem THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
rem INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
rem FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
rem APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
rem INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
rem DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
rem OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
rem ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
rem (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
rem THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
rem ============================================================================
rem 
rem This software consists of voluntary contributions made by many individuals
rem on behalf of the Apache Software Foundation and was originally created by
rem James Tauber <jtauber@jtauber.com>. For more information on the Apache
rem Software Foundation, please see <http://www.apache.org/>.
