

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
	ListItem.java \
	ListItemBody.java \
	ListItemLabel.java \
	PageNumber.java \
	SimpleLink.java \
	StaticContent.java \
	Table.java \
	TableBody.java \
	TableCell.java \
	TableColumn.java \
	TableRow.java

CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES)

clean: cleanme

cleanme:
	rm -f *.class

$(TARGETS:%=%subs): %subs :
	for dir in $(SUBDIRS) ; do \
		(cd $$dir && pwd && $(MAKE) $(MFLAGS) $*) || exit 1 ; \
	done

