package org.apache.xml.fop.layout;

public class SinglePageMasterFactory extends PageMasterFactory {
	
    private PageMaster pageMaster;
	
    private static final int FIRST = 0;
    private static final int DONE = 1;
	
    private int state;
	
    public SinglePageMasterFactory(PageMaster pageMaster) {
	this.pageMaster = pageMaster;
	this.state = FIRST;
    }
	
    public int getHeight() {
	return this.pageMaster.getHeight();
    }
	
    public PageMaster getNextPageMaster() {
	PageMaster pm;
		
	switch (this.state) {
	case FIRST: pm = this.pageMaster; this.state = DONE; break;
	default: pm = null;
	}
	
	return pm;
    }
    public int getWidth() {
	return this.pageMaster.getWidth();
    }
}
