#!/bin/sh
LIBDIR=./lib
LOCALCLASSPATH=$LIBDIR/../build/fop.jar:$LIBDIR/batik.jar:$LIBDIR/xalan-2.0.0.jar:$LIBDIR/xerces-1.2.3.jar:$LIBDIR/avalon-framework-4.0.jar:$LIBDIR/logkit-1.0b4.jar:$LIBDIR/jimi-1.0.jar 
java -cp $CLASSPATH:$LOCALCLASSPATH org.apache.fop.apps.Fop "$@"

