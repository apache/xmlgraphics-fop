# makefile for fop
#
# use gmake
#
BASEDIR=.

APIDOCDIR=docs/api

include $(BASEDIR)/Makefile.rules

SUBDIRS=src

CODEGEN=src/codegen

GENDIR=generated

JARTEMP=jartemp

JARTOC=jartoc

JARFILE=fop.jar

SRCJAR=fopsrc.jar

FONTXMLBASE=Courier.xml \
	Courier-Oblique.xml \
	Courier-Bold.xml \
	Courier-BoldOblique.xml \
	Helvetica.xml \
	Helvetica-Oblique.xml \
	Helvetica-Bold.xml \
	Helvetica-BoldOblique.xml \
	Times-Roman.xml \
	Times-Italic.xml \
	Times-Bold.xml \
	Times-BoldItalic.xml 

FONTXML=$(FONTXMLBASE:%=$(CODEGEN)/%)

FONTXSL=$(CODEGEN)/font-file.xsl

PROPERTIESXML=$(CODEGEN)/properties.xml
PROPERTIESXSL=$(CODEGEN)/properties.xsl
CHARLISTXML  =$(CODEGEN)/charlist.xml
CHARLISTXSL  =$(CODEGEN)/code-point-mapping.xsl

PACKAGES=org.apache.fop.apps \
	org.apache.fop.datatypes \
	org.apache.fop.fo \
	org.apache.fop.fo.flow \
	org.apache.fop.fo.pagination \
	org.apache.fop.image \
	org.apache.fop.layout \
	org.apache.fop.pdf \
	org.apache.fop.render \
	org.apache.fop.render.awt \
	org.apache.fop.render.pdf \
	org.apache.fop.render.xml \
	org.apache.fop.svg \
	org.apache.fop.viewer \
	org.apache.fop.fo.properties \
	org.apache.fop.render.pdf.fonts


all: codegen allsubs

clean: cleansubs
	rm -f *~

clobber: clean
	rm -rf $(GENDIR)
	rm -rf $(JARTEMP) $(JARTOC) $(JARFILE)
	rm -f $(SRCJAR)
	rm -rf $(APIDOCDIR)

codegen: $(GENDIR) compilegen

compilegen: properties charlist fonts
	cd $(GENDIR) && \
	($(FIND) . -name \*.java -print > javafiletoc) && \
	for javafile in `cat javafiletoc` ; do \
		echo $(JAVAC) $(GEN_JAVAC_ARGS) $$javafile ;\
		$(JAVAC) $(GEN_JAVAC_ARGS) $$javafile ;\
	done

$(GENDIR): 
	mkdir -p $(GENDIR)/org/apache/fop/fo/properties;
	mkdir -p $(GENDIR)/org/apache/fop/render/pdf/fonts;

properties: $(PROPERTIESXML) $(PROPERTIESXSL)
	cd $(GENDIR) && $(XALAN) -IN ../$(PROPERTIESXML) -XSL ../$(PROPERTIESXSL)

charlist: $(CHARLISTXML) $(CHARLISTXSL)
	cd $(GENDIR) && $(XALAN) -IN ../$(CHARLISTXML) -XSL ../$(CHARLISTXSL)

fonts: $(FONTXML) $(FONTXSL)
	cd $(GENDIR) && for font in $(FONTXML) ; do $(XALAN) -IN ../$$font -XSL ../$(FONTXSL) ; done

docs: all $(APIDOCDIR) 
	$(JAVADOC) $(JAVADOC_ARGS) $(PACKAGES)

dist: all $(JARTEMP) distgen distorg
	rm -f $(JARFILE)
	cd $(JARTEMP) && $(JAR) -cf ../$(JARFILE) *
	rm -rf $(JARTEMP) $(JARTOC)

srcdist: clobber
	$(JAR) -cf $(SRCJAR) .

$(JARTEMP):
	mkdir $(JARTEMP)

$(APIDOCDIR):
	mkdir $(APIDOCDIR)

distgen:
	cd $(GENDIR) && \
	rm -f $(JARTOC) && \
	($(FIND) . -name \*.class -print > $(JARTOC)) && \
	($(TAR) -cf - -T $(JARTOC) | (cd ../$(JARTEMP); $(TAR) -xf - )) 

distorg:
	cd src && \
	rm -f $(JARTOC) && \
	($(FIND) org -name \*.class -print > $(JARTOC)) && \
	($(TAR) -cf - -T $(JARTOC) | (cd ../$(JARTEMP); $(TAR) -xf - ))

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done


