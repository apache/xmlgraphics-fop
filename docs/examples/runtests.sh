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
LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/classes.zip
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/ant.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/xml-apis.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/xercesImpl-2.2.1.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/xalan-2.4.1.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/batik.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/avalon-framework-cvs-20020806.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/bsf.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/jimi-1.0.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/jai_core.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/jai_codec.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/../build/fop.jar

ANT_HOME=$LIBDIR

echo Building with classpath $CLASSPATH:$LOCALCLASSPATH
echo

echo Starting Tests...
echo

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath "$LOCALCLASSPATH:$CLASSPATH" org.apache.tools.ant.Main $*
