package org.apache.fop.pdf;

public class PDFOptionalFontDescriptor extends PDFFontDescriptor {

	private int missingWidth = -1;
	private int stemH = -1;
	private int xHeight = -1;
	private int leading = -1;
	private int maxWidth = -1;
	private int minWidth = -1;
	private int avgWidth = -1;
	private String panose = null;

	public PDFOptionalFontDescriptor(int number, String basefont, int ascent,
        int descent, int capHeight, int flags, PDFRectangle fontBBox,
		int italicAngle, int stemV, int missingWidth, int stemH,
		int xHeight, int leading, int maxWidth, int minWidth, int avgWidth,
		String panose ) {
		super(number,basefont,ascent,descent,capHeight,flags,fontBBox,
			italicAngle,stemV);
		this.missingWidth = missingWidth;
		this.stemH = stemH;
		this.xHeight = xHeight;
		this.leading = leading;
		this.maxWidth = maxWidth;
		this.minWidth = minWidth;
		this.avgWidth = avgWidth;
		this.panose = panose;
	}

	protected void fillInPDF(StringBuffer p) {
		if (missingWidth > -1) {
			p.append("\n/MissingWidth ");
			p.append(Integer.toString(missingWidth));
			p.append(" ");
		}
		if (stemH > -1) {
			p.append("\n/StemH ");
			p.append(Integer.toString(stemH));
			p.append(" ");
		}
		if (leading > -1) {
			p.append("\n/Leading ");
			p.append(Integer.toString(leading));
			p.append(" ");
		}
		if (maxWidth > -1) {
			p.append("\n/MaxWidth ");
			p.append(Integer.toString(maxWidth));
			p.append(" ");
		}
		if (minWidth > -1) {
			p.append("\n/MinWidth ");
			p.append(Integer.toString(minWidth));
			p.append(" ");
		}
		if (avgWidth > -1) {
			p.append("\n/AvgWidth ");
			p.append(Integer.toString(avgWidth));
			p.append(" ");
		}
		if (xHeight > -1) {
			p.append("\n/XHeight ");
			p.append(Integer.toString(xHeight));
			p.append(" ");
		}
		if ( panose != null ) {
			p.append("\n/Style << /Panose <");
			p.append(panose);
			p.append(">>> ");
		}
	}
}
