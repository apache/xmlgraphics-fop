#!/bin/sh
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
LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$JAVA_HOME/lib/classes.zip
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/ant-1.5.1.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/xml-apis.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/xercesImpl-2.2.1.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/xalan-2.4.1.jar

# antRun must be executable (can't do this in build.xml because Ant uses antRun
# to do the chmod)
chmod +rx $LIBDIR/bin/antRun

ANT_HOME=$LIBDIR

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  LOCALCLASSPATH=`cygpath --path --windows "$LOCALCLASSPATH"`
fi

echo
echo Building with classpath $LOCALCLASSPATH
echo Starting Ant...
echo

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath "$LOCALCLASSPATH" org.apache.tools.ant.Main $*
