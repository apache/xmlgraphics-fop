

BASEDIR:=../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=

SOURCES=FopImageException.java \
	FopImage.java \
	ImageArea.java \
	FopImageFactory.java \
	FopImageConsumer.java \
	GifJpegImage.java \
	JimiImage.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES)

clean: cleanme

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

