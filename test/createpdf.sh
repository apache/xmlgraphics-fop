# this can be used to create pdf output of the testsuite files


$JAVA_HOME/bin/java -cp ../build/fop.jar:../lib/batik.jar:../lib/xalan-2.3.1.jar:../lib/xercesImpl-2.0.1.jar:../lib/xml-apis.jar:../lib/avalon-framework-4.0.jar:../lib/logkit-1.0.jar:../lib/jimi-1.0.jar org.apache.fop.tools.TestConverter -pdf -b ./ basictests.xml

$JAVA_HOME/bin/java -cp ../build/fop.jar:../lib/batik.jar:../lib/xalan-2.3.1.jar:../lib/xercesImpl-2.0.1.jar:../lib/xml-apis.jar:../lib/avalon-framework-4.0.jar:../lib/logkit-1.0.jar:../lib/jimi-1.0.jar org.apache.fop.tools.TestConverter -pdf -b ./ bugtests.xml


