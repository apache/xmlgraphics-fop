package org.apache.fop.render.awt;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.util.Hashtable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.awt.font.TextAttribute;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent; 

/**
 * This class demonstrates how to line-break and draw a paragraph 
 * of text using LineBreakMeasurer and TextLayout.
 *
 * This class constructs a LineBreakMeasurer from an
 * AttributedCharacterIterator.  It uses the LineBreakMeasurer
 * to create and draw TextLayouts (lines of text) which fit within 
 * the Component's width.
 */

public class LineBreakSample extends JApplet {

    // The LineBreakMeasurer used to line-break the paragraph.
    private LineBreakMeasurer lineMeasurer;

    // The index in the LineBreakMeasurer of the first character
    // in the paragraph.
    private int paragraphStart;

    // The index in the LineBreakMeasurer of the first character
    // after the end of the paragraph.
    private int paragraphEnd;

    private static final Hashtable map = new Hashtable();
    static {
        map.put(TextAttribute.FAMILY, "Utopia");
        map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        map.put(TextAttribute.WIDTH, TextAttribute.WIDTH_CONDENSED);
        map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        map.put(TextAttribute.FOREGROUND, Color.red);
        map.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        map.put(TextAttribute.SIZE, new Float(18.0));
    }  

    private static AttributedString vanGogh = new AttributedString(
        "Many people believe that Vincent van Gogh painted his best works " +
        "during the two-year period he spent in Provence. Here is where he " +
        "painted The Starry Night--which some consider to be his greatest " +
        "work of all. However, as his artistic brilliance reached new heights " +
        "in Provence, his physical and mental health plummeted. ", map);
                                
    public void init() {
	buildUI(getContentPane());
    }

    public void buildUI(Container container){
        LineBreakPanel lineBreakPanel = new LineBreakPanel();
	container.add(lineBreakPanel, BorderLayout.CENTER);
    }
            
    class LineBreakPanel extends JPanel {

      public LineBreakPanel() {
          //setFont(Font.decode("utopia-plain-14"));
          AttributedCharacterIterator paragraph = vanGogh.getIterator();
          paragraphStart = paragraph.getBeginIndex();
          paragraphEnd = paragraph.getEndIndex();

          // Create a new LineBreakMeasurer from the paragraph.
          lineMeasurer = new LineBreakMeasurer(paragraph, 
                                new FontRenderContext(null, true, true));
      }

      public void paintComponent(Graphics g) {

	super.paintComponent(g);
        setBackground(Color.white);

        Graphics2D graphics2D = (Graphics2D) g;

        // Set formatting width to width of Component.
	Dimension size = getSize();
        float formatWidth = (float) size.width;

        float drawPosY = 0;

        lineMeasurer.setPosition(paragraphStart);

        // Get lines from lineMeasurer until the entire
        // paragraph has been displayed.
        while (lineMeasurer.getPosition() < paragraphEnd) {

            // Retrieve next layout.
            TextLayout layout = lineMeasurer.nextLayout(formatWidth);
            // Move y-coordinate by the ascent of the layout.
            drawPosY += layout.getAscent();

            // Compute pen x position.  If the paragraph is 
            // right-to-left, we want to align the TextLayouts
            // to the right edge of the panel.
            float drawPosX;
            if (layout.isLeftToRight()) {
                drawPosX = 0;
            }
            else {
                drawPosX = formatWidth - layout.getAdvance();
            }

            // Draw the TextLayout at (drawPosX, drawPosY).
            layout.draw(graphics2D, drawPosX, drawPosY);

            // Move y-coordinate in preparation for next layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }

      }
    }

    public static void main(String[] args) {

        JFrame f = new JFrame("HitTestSample");
            
        f.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        LineBreakSample controller = new LineBreakSample();
	controller.buildUI(f.getContentPane());        
        f.setSize(new Dimension(400, 250));
        f.setVisible(true);
    }

}
