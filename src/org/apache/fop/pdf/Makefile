

BASEDIR:=../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=

SOURCES=PDFAction.java \
	PDFAnnotList.java \
	PDFDocument.java \
	PDFFileSpec.java \
	PDFFont.java \
	PDFInfo.java \
	PDFLink.java \
	PDFObject.java \
	PDFPage.java \
	PDFPages.java \
	PDFResources.java \
	PDFRoot.java \
	PDFStream.java \
	PDFFilterException.java \
	PDFFilter.java \
	PDFBinaryStream.java \
	PDFXObject.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES)

clean: cleanme

cleanme:
	rm -f *.class


$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

