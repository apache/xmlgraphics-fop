#!/bin/sh

echo
echo "Fop Test"
echo "----------------"
echo

if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit 1
fi
LIBDIR=../../lib
LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/classes.zip:$LIBDIR/ant.jar:$LIBDIR/batik.jar:$LIBDIR/buildtools.jar:$LIBDIR/xerces-1.2.3.jar:$LIBDIR/xalan-2.0.0.jar:$LIBDIR/xalanj1compat.jar:$LIBDIR/bsf.jar:$LIBDIR/avalon-framework-4.0.jar:$LIBDIR/logkit-1.0b4.jar:$LIBDIR/jimi-1.0.jar:$LIBDIR/../build/fop.jar
ANT_HOME=$LIBDIR

echo Building with classpath $CLASSPATH:$LOCALCLASSPATH
echo

echo Starting Tests...
echo

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath "$LOCALCLASSPATH:$CLASSPATH" org.apache.tools.ant.Main $*
