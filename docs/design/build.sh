#! /bin/sh
# $Id$

LIBDIR=../../lib
TARGET_CLASSPATH=$LIBDIR/ant.jar:\
$LIBDIR/buildtools.jar:\
$LIBDIR/xalan-2.0.0.jar:\
$LIBDIR/xerces-1.2.3.jar:\
$LIBDIR/bsf.jar:\
../../build/fop.jar:\
$LIBDIR/logkit-1.0.jar:\
$LIBDIR/avalon-framework-4.0.jar:\
$LIBDIR/batik.jar:\
$LIBDIR/jimi-1.0.jar

if [ "$JAVA_HOME" != "" ] ; then
   TARGET_CLASSPATH=$TARGET_CLASSPATH:$JAVA_HOME/lib/tools.jar
else
   echo "Error: The JAVA_HOME environment variable is not set."
fi

java -classpath $TARGET_CLASSPATH org.apache.tools.ant.Main $*
