#!/bin/sh

echo Fop Build System
echo ----------------

LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:./lib/ant.jar:./lib/w3c.jar:./lib/buildtools.jar
ANT_HOME=./lib

echo Building with classpath $LOCALCLASSPATH

echo Starting Ant...

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath $LOCALCLASSPATH:$CLASSPATH org.apache.tools.ant.Main -buildfile build-jpfop.xml $*
