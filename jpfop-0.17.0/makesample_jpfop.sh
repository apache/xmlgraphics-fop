#!/bin/sh

XERCES=../lib/xerces.jar
XALAN=../lib/xalan.jar
BSF=../lib/bsf.jar

$JAVA_HOME/bin/java -cp ../build/fop-0.16.0-jp.jar:$XERCES:$XALAN:$BSF:../lib/w3c.jar org.apache.fop.apps.CommandLine -d sample_jpfop.fo sample_jpfop.pdf

