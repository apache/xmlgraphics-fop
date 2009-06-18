# this can be used to create pdf output of the testsuite files


LIBDIR=../lib
LOCALCLASSPATH=$LIBDIR/batik.jar:$LIBDIR/xercesImpl-2.0.1.jar:$LIBDIR/xalan-2.3.1.jar:$LIBDIR/xml-apis.jar
LOCALCLASSPATH=$LOCALCLASSPATH:$LIBDIR/jimi-1.0.jar:$LIBDIR/jai_core.jar:$LIBDIR/jai_codec.jar:$LIBDIR/avalon-framework-cvs-20020806.jar:$LIBDIR/logkit-1.0.jar

$JAVA_HOME/bin/java -cp ../build/fop.jar:$LOCALCLASSPATH org.apache.fop.tools.TestConverter -pdf -b ./ basictests.xml

$JAVA_HOME/bin/java -cp ../build/fop.jar:$LOCALCLASSPATH org.apache.fop.tools.TestConverter -pdf -b ./ bugtests.xml


