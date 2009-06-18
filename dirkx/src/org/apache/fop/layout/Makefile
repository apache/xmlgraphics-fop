

BASEDIR:=../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=

SOURCES=AlternatingPageMasterFactory.java \
	Area.java \
	AreaContainer.java \
	AreaTree.java \
	BlockArea.java \
	Box.java \
	DisplaySpace.java \
	FontInfo.java \
	FontMetric.java \
	FontState.java \
	InlineArea.java \
	InlineSpace.java \
	LineArea.java \
	Page.java \
	PageMaster.java \
	PageMasterFactory.java \
	Region.java \
	RepeatingPageMasterFactory.java \
	RuleArea.java \
	SinglePageMasterFactory.java \
	Space.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES) allsubs

clean: cleanme cleansubs

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

