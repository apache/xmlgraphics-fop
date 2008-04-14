# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# $Id$ 

BOOK=DnI.xml
CHAPTERS=addareas.xml areatree.xml configuration.xml fonts.xml \
	foptrees.xml fotree.xml getnextbreakposs.xml overview.xml \
	preparation.xml properties.xml rendering.xml
CUSTOM_DOCBOOK_XHTML_STYLESHEET=cust-xhtml-docbook.xsl
CUSTOM_DOCBOOK_HTML_STYLESHEET=cust-html-docbook.xsl
CUSTOM_DOCBOOK_FO_STYLESHEET=cust-fo-docbook.xsl
STYLESHEETS=$(CUSTOM_DOCBOOK_XHTML_STYLESHEET) \
	$(CUSTOM_DOCBOOK_HTML_STYLESHEET) \
	$(CUSTOM_DOCBOOK_FO_STYLESHEET)
BOOKMAKER=BookMaker.java

# destinations
BUILD_DIR=../../../../../build
BOOKMAKERCLASSPATH=$(BUILD_DIR)/classes
BOOKMAKER_CLASS=$(BOOKMAKERCLASSPATH)/BookMaker.class
DNI_BUILD_DIR=$(BUILD_DIR)/site/DnI
XHTML_DIR=$(DNI_BUILD_DIR)/xhtml
HTML_DIR=$(DNI_BUILD_DIR)/html
PDF_DIR=$(DNI_BUILD_DIR)

JAVACLASSES=/usr/share/java
LOCALJAVACLASSES=/usr/local/javaclasses
FOPCLASSPATH=$(JAVACLASSES)/fop.jar:$(LOCALJAVACLASSES)/fop.jar
AVALONCLASSPATH=$(JAVACLASSES)/avalon-framework.jar:$(LOCALJAVACLASSES)/avalon-framework.jar
SAXONCLASSPATH=$(JAVACLASSES)/saxon.jar:$(LOCALJAVACLASSES)/saxon.jar
RESOLVERCLASSPATH=$(JAVACLASSES)/xml-commons-resolver.jar:$(LOCALJAVACLASSES)/xml-commons-resolver.jar
XERCESCLASSPATH=$(JAVACLASSES)/xml-apis.jar:$(JAVACLASSES)/xercesImpl.jar:$(LOCALJAVACLASSES)/xml-apis.jar:$(LOCALJAVACLASSES)/xercesImpl.jar
CLICLASSPATH=$(JAVACLASSES)/commons-cli.jar:$(LOCALJAVACLASSES)/commons-cli.jar

all: xhtml html pdf

xhtml: $(XHTML_DIR)/DnI-xhtml.xml
html: $(HTML_DIR)/DnI.html
fo: $(PDF_DIR)/DnI.fo
pdf: $(PDF_DIR)/DnI.pdf

$(XHTML_DIR)/DnI-xhtml.xml: $(BOOK) $(CHAPTERS) $(CUSTOM_DOCBOOK_XHTML_STYLESHEET) $(BOOKMAKER_CLASS)
	[ -d $(XHTML_DIR) ] || mkdir -p $(XHTML_DIR)
	java $(JAVAFLAGS) \
		-classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) \
		BookMaker $(PROGOPTS) --parameter base.dir $(XHTML_DIR)/ -xml $< -xsl $(CUSTOM_DOCBOOK_XHTML_STYLESHEET) -out $@

$(HTML_DIR)/DnI.html: $(BOOK) $(CHAPTERS) $(CUSTOM_DOCBOOK_HTML_STYLESHEET) $(BOOKMAKER_CLASS)
	[ -d $(HTML_DIR) ] || mkdir -p $(HTML_DIR)
	java $(JAVAFLAGS) \
		-classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) \
		BookMaker $(PROGOPTS) --parameter base.dir $(HTML_DIR)/ -xml $< -xsl $(CUSTOM_DOCBOOK_HTML_STYLESHEET) -out $@

$(PDF_DIR)/DnI.fo:  $(BOOK) $(CHAPTERS) $(CUSTOM_DOCBOOK_FO_STYLESHEET) $(BOOKMAKER_CLASS)
	[ -d $(PDF_DIR) ] || mkdir -p $(PDF_DIR)
	java $(JAVAFLAGS) \
		-classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) \
		BookMaker $(PROGOPTS) -xml $< -xsl $(CUSTOM_DOCBOOK_FO_STYLESHEET) -out $@

$(PDF_DIR)/DnI.pdf: $(BOOK) $(CHAPTERS) $(CUSTOM_DOCBOOK_FO_STYLESHEET) $(BOOKMAKER_CLASS)
	[ -d $(PDF_DIR) ] || mkdir -p $(PDF_DIR)
	java $(JAVAFLAGS) \
		-classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) \
		BookMaker $(PROGOPTS) -xml $< -xsl $(CUSTOM_DOCBOOK_FO_STYLESHEET) -pdf $@

$(BOOKMAKERCLASSPATH)/%.class: %.java
	[ -d $(BOOKMAKERCLASSPATH) ] || mkdir -p $(BOOKMAKERCLASSPATH)
	javac -classpath $(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(CLASSPATH) \
		-d $(BOOKMAKERCLASSPATH) $<

############################ distribution file #########################

CURRENT_DIR=documentation

zip: documentation.zip

documentation.zip: $(BOOK) $(CHAPTERS) $(STYLESHEETS) $(BOOKMAKER) Makefile README
	-rm -f $@
	cd .. && zip $(CURRENT_DIR)/$@ ${^:%=$(CURRENT_DIR)/%}

############################ test section ###############################

TEST=test.xml
TEST_CHAPTERS=getnextbreakposs.xml overview.xml 

test-saxon.html: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_HTML_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) BookMaker $(PROGOPTS) --parameter base.dir $(HTML_DIR)/ -xml $< -xsl $(CUSTOM_DOCBOOK_HTML_STYLESHEET) -out $@

test-saxon.xhtml: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_XHTML_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) BookMaker $(PROGOPTS) --parameter base.dir $(XHTML_DIR)/ -xml $< -xsl $(CUSTOM_DOCBOOK_XHTML_STYLESHEET) -out $@

test-saxon.fo: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_FO_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) BookMaker $(PROGOPTS) -xml $< -xsl $(CUSTOM_DOCBOOK_FO_STYLESHEET) -out $@

test-saxon.pdf: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_FO_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) BookMaker $(PROGOPTS) -xml $< -xsl $(CUSTOM_DOCBOOK_FO_STYLESHEET) -pdf $@

test-saxon-stf.pdf: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_FO_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(XERCESCLASSPATH):$(SAXONCLASSPATH):$(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) XML2PDF_STF $(PROGOPTS) -xml $< -xsl $(CUSTOM_DOCBOOK_FO_STYLESHEET) -pdf $@

test-xalan.html: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_HTML_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) BookMaker $(PROGOPTS) --parameter base.dir $(HTML_DIR)/ -xml $< -xsl $(CUSTOM_DOCBOOK_HTML_STYLESHEET) -out $@

test-xalan.xhtml: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_XHTML_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) BookMaker $(PROGOPTS) --parameter base.dir $(XHTML_DIR)/ -xml $< -xsl $(CUSTOM_DOCBOOK_XHTML_STYLESHEET) -out $@

test-xalan.fo: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_FO_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) BookMaker $(PROGOPTS) -xml $< -xsl $(CUSTOM_DOCBOOK_FO_STYLESHEET) -out $@

test-xalan.pdf: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_FO_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) BookMaker $(PROGOPTS) -xml $< -xsl $(CUSTOM_DOCBOOK_FO_STYLESHEET) -pdf $@

test-xalan-stf.pdf: $(TEST) $(TEST_CHAPTERS) $(CUSTOM_DOCBOOK_FO_STYLESHEET) $(BOOKMAKER_CLASS)
	java $(JAVAFLAGS) -classpath $(FOPCLASSPATH):$(AVALONCLASSPATH):$(RESOLVERCLASSPATH):$(CLICLASSPATH):$(BOOKMAKERCLASSPATH):$(CLASSPATH) XML2PDF_STF $(PROGOPTS) -xml $< -xsl $(CUSTOM_DOCBOOK_FO_STYLESHEET) -pdf $@

