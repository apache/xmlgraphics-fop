package org.apache.xml.fop.layout;

abstract public class PageMasterFactory {
	
    private PageMasterFactory next;
	
    abstract public int getHeight();
	
    abstract public int getWidth();

    public PageMasterFactory getNext() {
	return this.next;
    }

    abstract public PageMaster getNextPageMaster();
	
    public void setNext(PageMasterFactory pmf) {
	this.next = pmf;
    }
}
