#!/bin/sh
echo
echo "Plan Build System"
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
LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/classes.zip:$LIBDIR/ant.jar:$LIBDIR/batik.jar:$LIBDIR/buildtools.jar:$LIBDIR/xerces-1.4.3.jar:$LIBDIR/xalan-2.2D11.jar:../../build/fop.jar:$LIBDIR/avalon-framework-cvs-20020315.jar
ANT_HOME=$LIBDIR

echo
CP=$LOCALCLASSPATH
echo Building with classpath $CP


echo Starting Ant...
echo

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath "$CP" org.apache.tools.ant.Main $*
