#! /bin/sh
# $Id$

LIBDIR=../../lib  
TARGET_CLASSPATH=$LIBDIR/ant.jar:\
$LIBDIR/buildtools.jar:\
$LIBDIR/xalan-1.2.2.jar:\
$LIBDIR/xerces-1.2.3.jar:\
$LIBDIR/bsf.jar:\
../../build/fop.jar:\
$LIBDIR/logkit-1.0b4.jar:\
$LIBDIR/avalon-framework-4.0.jar:\
$LIBDIR/batik.jar:\
$LIBDIR/optional.jar:\
$LIBDIR/jimi-1.0.jar:

if [ "$JAVA_HOME" != "" ] ; then
   TARGET_CLASSPATH=$TARGET_CLASSPATH:$JAVA_HOME/lib/tools.jar
else
   echo "Error: The JAVA_HOME environment variable is not set."
fi
            
java -classpath $TARGET_CLASSPATH -Djboss.home=$JBOSS_HOME org.apache.tools.ant.Main $*
