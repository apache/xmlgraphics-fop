package org.apache.xml.fop.layout;

public class RepeatingPageMasterFactory extends PageMasterFactory {
	
    private PageMaster pageMasterFirst;
    private PageMaster pageMasterRepeating;
	
    private static final int FIRST = 0;
    private static final int REST = 1;
	
    private int state;
	
    public RepeatingPageMasterFactory(PageMaster first, PageMaster repeating) {
	this.pageMasterFirst = first;
	this.pageMasterRepeating = repeating;
	this.state = FIRST;
    }
	
    public int getHeight() {
	return this.pageMasterFirst.getHeight();
    }
	
    public PageMaster getNextPageMaster() {
	PageMaster pm;
		
	switch (this.state) {
	case REST: pm = this.pageMasterRepeating; this.state = REST; break;
	default: pm = this.pageMasterFirst; this.state = REST; 
	}		 
	
	return pm;
    }
	
    public int getWidth() {
	return this.pageMasterFirst.getWidth();
    }
}
