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
LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/classes.zip:$LIBDIR/ant-1.4.1.jar:$LIBDIR/batik.jar:$LIBDIR/buildtools.jar:$LIBDIR/xercesImpl-2.0.1.jar:$LIBDIR/xml-apis.jar:$LIBDIR/xalan-2.3.1.jar:$LIBDIR/bsf.jar:$LIBDIR/avalon-framework-cvs-20020315.jar:$LIBDIR/jimi-1.0.jar:$LIBDIR/../build/fop.jar
ANT_HOME=$LIBDIR

echo Building with classpath $CLASSPATH:$LOCALCLASSPATH
echo

echo Starting Tests...
echo

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath "$LOCALCLASSPATH:$CLASSPATH" org.apache.tools.ant.Main $*
