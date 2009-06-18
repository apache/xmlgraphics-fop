

BASEDIR:=../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=

SOURCES=PDFDocument.java \
	PDFFont.java \
	PDFInfo.java \
	PDFObject.java \
	PDFPage.java \
	PDFPages.java \
	PDFResources.java \
	PDFRoot.java \
	PDFStream.java \
	PDFXObject.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES) allsubs

clean: cleanme cleansubs

cleanme:
	rm -f *.class


$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

