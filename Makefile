# makefile for fop
#
# use gmake
#
BASEDIR=.

include $(BASEDIR)/Makefile.rules

SUBDIRS=org

CODEGEN=codegen

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

all: codegen allsubs

clean: cleansubs
	rm -f *~

clobber: clean
	rm -rf $(GENDIR)
	rm -rf $(JARTEMP) $(JARTOC) $(JARFILE)
	rm -f $(SRCJAR)



codegen: $(GENDIR) compilegen

compilegen: properties charlist fonts
	cd $(GENDIR) && \
	($(FIND) . -name \*.java -print > javafiletoc) && \
	for javafile in `cat javafiletoc` ; do \
		echo $(JAVAC) $(JAVAC_ARGS) $$javafile ;\
		$(JAVAC) $(JAVAC_ARGS) $$javafile ;\
	done

$(GENDIR): 
	mkdir -p $(GENDIR)/org/apache/xml/fop/fo/properties;
	mkdir -p $(GENDIR)/org/apache/xml/fop/render/pdf/fonts;

properties: $(PROPERTIESXML) $(PROPERTIESXSL)
	cd $(GENDIR) && $(XT) ../$(PROPERTIESXML) ../$(PROPERTIESXSL)

charlist: $(CHARLISTXML) $(CHARLISTXSL)
	cd $(GENDIR) && $(XT) ../$(CHARLISTXML) ../$(CHARLISTXSL)

fonts: $(FONTXML) $(FONTXSL)
	cd $(GENDIR) && for font in $(FONTXML) ; do $(XT) ../$$font ../$(FONTXSL) ; done

dist: all $(JARTEMP) distgen distorg
	rm -f $(JARFILE)
	cd $(JARTEMP) && $(JAR) -cf ../$(JARFILE) *
	rm -rf $(JARTEMP) $(JARTOC)

srcdist: clobber
	$(JAR) -cf $(SRCJAR) .

$(JARTEMP):
	mkdir $(JARTEMP)

distgen:
	cd $(GENDIR) && \
	rm -f $(JARTOC) && \
	($(FIND) . -name \*.class -print > $(JARTOC)) && \
	($(TAR) -cf - -T $(JARTOC) | (cd ../$(JARTEMP); $(TAR) -xf - )) 

distorg:
	rm -f $(JARTOC) && \
	($(FIND) org -name \*.class -print > $(JARTOC)) && \
	($(TAR) -cf - -T $(JARTOC) | (cd $(JARTEMP); $(TAR) -xf - ))

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

