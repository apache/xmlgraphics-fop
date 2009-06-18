

BASEDIR:=../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=

SOURCES=Graphic.java \
	Line.java \
	LineGraphic.java \
	Rect.java \
	RectGraphic.java \
	SVG.java \
	SVGArea.java \
	SVGElementMapping.java \
	SVGLength.java \
	Text.java \
	TextGraphic.java 

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES) allsubs

clean: cleanme cleansubs

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

