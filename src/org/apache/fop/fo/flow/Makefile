

BASEDIR:=../../../../../..
include $(BASEDIR)/Makefile.rules

SUBDIRS=


SOURCES=Block.java \
	DisplayGraphic.java \
	DisplayRule.java \
	DisplaySequence.java \
	Flow.java \
	InlineSequence.java \
	ListBlock.java \
	ListItemBody.java \
	ListItemLabel.java \
	PageNumber.java \
	StaticContent.java \
	Table.java \
	TableBody.java \
	TableCell.java \
	TableColumn.java \
	TableRow.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES) allsubs

clean: cleanme cleansubs

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

