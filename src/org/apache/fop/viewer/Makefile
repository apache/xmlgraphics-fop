

BASEDIR:=../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=

SOURCES=Command.java \
	DocumentPanel.java \
	IconToolBar.java \
	LoadableProperties.java \
	MessageException.java \
	MessagesDialog.java \
	PreviewDialog.java \
	PreviewDialogAboutBox.java \
	ProgressListener.java \
	SecureResourceBundle.java \
	Translator.java \
	UserMessage.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES)

clean: cleanme

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done


