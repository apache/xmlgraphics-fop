

BASEDIR:=../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=

SOURCES=AWTCommandLine.java \
	CommandLine.java \
	Driver.java \
	ErrorHandler.java \
	FOPException.java \
	Version.java \
	XTCommandLine.java



CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES)

clean: cleanme

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

