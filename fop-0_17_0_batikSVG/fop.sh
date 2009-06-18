#!/bin/sh
java -cp build/fop.jar:lib/w3c.jar:lib/xalan-2.0.0.jar:lib/xerces-1.2.3.jar:lib/jimi-1.0.jar org.apache.fop.apps.Fop "$@"

