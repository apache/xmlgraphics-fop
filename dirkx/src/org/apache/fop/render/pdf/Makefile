

BASEDIR:=../../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=fonts

SOURCES= \
	Font.java \
	FontSetup.java \
	PDFRenderer.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES) allsubs

clean: cleanme cleansubs

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

