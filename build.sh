#!/bin/sh

# $Id$

# ============================================================================
#                    The Apache Software License, Version 1.1
# ============================================================================
# 
# Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without modifica-
# tion, are permitted provided that the following conditions are met:
# 
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 
# 3. The end-user documentation included with the redistribution, if any, must
#    include the following acknowledgment: "This product includes software
#    developed by the Apache Software Foundation (http://www.apache.org/)."
#    Alternately, this acknowledgment may appear in the software itself, if
#    and wherever such third-party acknowledgments normally appear.
# 
# 4. The names "FOP" and "Apache Software Foundation" must not be used to
#    endorse or promote products derived from this software without prior
#    written permission. For written permission, please contact
#    apache@apache.org.
# 
# 5. Products derived from this software may not be called "Apache", nor may
#    "Apache" appear in their name, without prior written permission of the
#    Apache Software Foundation.
# 
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
# FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
# APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
# DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
# OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
# ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
# THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
# ============================================================================
# 
# This software consists of voluntary contributions made by many individuals
# on behalf of the Apache Software Foundation and was originally created by
# James Tauber <jtauber@jtauber.com>. For more information on the Apache
# Software Foundation, please see <http://www.apache.org/>.

# This file should be executable.

echo
echo "Fop Build System"
echo "----------------"
echo

if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit 1
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

LIBDIR=lib
LOCALCLASSPATH="$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/classes.zip"
for jar in $LIBDIR/*.jar; do
    LOCALCLASSPATH="$LOCALCLASSPATH:$jar"
done

ANT_HOME="$LIBDIR"
# antRun must be executable (can't do this in build.xml because Ant uses antRun
# to do the chmod)
chmod +rx "$LIBDIR/bin/antRun"


# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  LOCALCLASSPATH=`cygpath --path --windows "$LOCALCLASSPATH"`
fi

echo
echo Building with classpath $LOCALCLASSPATH
echo Starting Ant...
echo

"$JAVA_HOME"/bin/java -Dant.home="$ANT_HOME" -classpath "$LOCALCLASSPATH" org.apache.tools.ant.Main "$@"

