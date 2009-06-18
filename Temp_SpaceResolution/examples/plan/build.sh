#!/bin/sh
#  Copyright 1999-2004 The Apache Software Foundation
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#  $Id$

# This file should be executable.

echo
echo "Apache FOP: Build System for plan extension"
echo "---------------------------------------------"
echo

if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit 1
fi

if [ "$ANT_HOME" = "" ] ; then
  echo "ERROR: ANT_HOME not found in your environment."
  echo
  echo "Please install Apache Ant first. FOP uses Ant as its build system."
  echo "You can download Apache Ant from http://ant.apache.org"
  echo "1. Download and install Ant"
  echo "2. Set the ANT_HOME environment variable to the directory where you've"
  echo "   placed Ant."
  echo "3. Add "\$ANT_HOME/bin" to your PATH so the "ant" script file"
  echo "   can be be executed from wherever needed."
  echo "4. (optional) Download JUnit and place junit-*.jar in \$ANT_HOME/lib."
  echo "   This enables the JUnit test cases."
  exit 1
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$ANT_HOME" ] &&
    ANT_HOME=`cygpath --unix "$ANT_HOME"`
fi
"$ANT_HOME"/bin/ant "$@"

