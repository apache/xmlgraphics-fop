

BASEDIR:=../../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=


SOURCES=LayoutMasterSet.java \
	PageSequence.java \
	RegionAfter.java \
	RegionBefore.java \
	RegionBody.java \
	Root.java \
	SequenceSpecification.java \
	SequenceSpecifier.java \
	SequenceSpecifierAlternating.java \
	SequenceSpecifierRepeating.java \
	SequenceSpecifierSingle.java \
	SimplePageMaster.java


CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES) allsubs

clean: cleanme cleansubs

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

