

BASEDIR:=../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=flow \
	pagination 


SOURCES=FONode.java \
	FOText.java \
	FOTreeBuilder.java \
	FObj.java \
	FObjMixed.java \
	Property.java \
	PropertyList.java \
	PropertyListBuilder.java \
	StandardElementMapping.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES) allsubs

clean: cleanme cleansubs

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

