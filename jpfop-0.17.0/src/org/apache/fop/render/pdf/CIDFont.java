package org.apache.fop.render.pdf;

public abstract class CIDFont extends Font {

	public static final int CIDFontType0 = 0;
	public static final int CIDFontType2 = 2;

	// Required
	public abstract String getCidBaseFont();
	public abstract int getCidType();
	public abstract String getCharEncoding();
	public abstract String getRegistry();
	public abstract String getOrdering();
	public abstract int getSupplement();
	// Optional
	public int getDefaultWidth() { return -1; }
	public Widths getWidths() { return null; }
	public int getWinCharSet() { return -1; }

	// Need For FOP

	/**
	 *Returns CMap Object .
	 *<p>
	 *If this method does not return null , the mapping from character codes
	 *to a font number is performed in FOP . When the getCidType() method
	 *returns CIDFontType2 , this method must not return null .
	 */
	public CMap getCMap() { return null; }
}
