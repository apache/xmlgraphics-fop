

BASEDIR:=../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=apps \
	datatypes \
	fo \
	image \
	layout \
	pdf \
	render \
	svg

all: $(CLASSES) allsubs

clean: cleanme cleansubs

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

