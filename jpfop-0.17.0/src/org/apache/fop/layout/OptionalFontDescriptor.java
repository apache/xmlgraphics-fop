package org.apache.fop.layout;

public interface OptionalFontDescriptor extends FontDescriptor {

    //Optional
    public int getXHeight();
	public int getMissingWidth();
	public int getStemH();
	public int getLeading();
	public int getMaxWidth();
	public int getMinWidth();
	public int getAvgWidth();
	public String getPanose();
}
