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

LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/classes.zip:../../lib/ant.jar:../../lib:../../build/fop.jar:../../lib/w3c.jar
ANT_HOME=../../lib

echo Building with classpath $CLASSPATH:$LOCALCLASSPATH
echo

echo Starting Tests...
echo

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath "$LOCALCLASSPATH:$CLASSPATH" org.apache.tools.ant.Main $*
