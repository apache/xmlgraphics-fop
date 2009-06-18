package org.apache.xml.fop.layout;

public class AlternatingPageMasterFactory extends PageMasterFactory {
	
    private PageMaster pageMasterFirst;
    private PageMaster pageMasterEven;
    private PageMaster pageMasterOdd;
	
    private static final int FIRST = 0;
    private static final int EVEN = 1;
    private static final int ODD = 2;
	
    private int state;
	
    public AlternatingPageMasterFactory(PageMaster first, PageMaster even, PageMaster odd) {
	this.pageMasterFirst = first;
	this.pageMasterEven = even;
	this.pageMasterOdd = odd;
	this.state = FIRST;
    }
	
    public int getHeight() {
	return this.pageMasterFirst.getHeight();
    }
    
    public PageMaster getNextPageMaster() {
	PageMaster pm;
		
	switch (this.state) {
	case EVEN: pm = this.pageMasterEven; this.state = ODD; break;
	case ODD: pm = this.pageMasterOdd; this.state = EVEN; break;
	default: pm = this.pageMasterFirst; this.state = EVEN; 
	}		 
		
	return pm;
    }
	
    public int getWidth() {
	return this.pageMasterFirst.getWidth();
    }
}
