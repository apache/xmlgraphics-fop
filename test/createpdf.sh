# this can be used to create pdf output of the testsuite files

$JAVA_HOME/bin/java -cp ../build/fop.jar:../lib/batik.jar:../lib/xalan-2.0.0.jar:../lib/xerces-1.2.3.jar:../lib/jimi-1.0.jar org.apache.fop.tools.TestConverter -pdf -b ./ basictests.xml
