set XERCES=..\lib\xerces.jar
set XALAN=..\lib\xalan.jar
set BSF=..\lib\bsf.jar

java -cp ..\build\fop-0.16.0-jp.jar;%XERCES%;%XALAN%;%BSF%;..\lib\w3c.jar org.apache.fop.apps.CommandLine -d sample_jpfop.fo sample_jpfop.pdf
