package org.apache.fop.viewer;

/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@af-software.de,
  Rainer Steinkuhle: Rainer.Steinkuhle@af-software.de,
  Stanislav Gorkhover: Stanislav.Gorkhover@af-software.de
 */



import java.awt.*;
import javax.swing.*;


import org.apache.fop.layout.*;
import org.apache.fop.render.awt.*;


 /**
  * Diese Komponente stellt im Dialog das Dokument dar.
  */
public class DocumentPanel extends JComponent {
  static final int V_BORDER = 80;
  static final int H_BORDER = 70;

  protected AWTRenderer renderer;
  protected PreviewDialog previewDialog;
  protected AreaTree areaTree;
  protected int pageNumber = 0;

  protected int docWidth;
  protected int docHeight;
  protected Color myColor = Color.lightGray;

  public DocumentPanel(AWTRenderer aRenderer, PreviewDialog p) {
    previewDialog = p;
    renderer = aRenderer;
    renderer.setComponent(this);
  }

  public void updateSize(int aPageNumber, double aFactor) {
    if (areaTree == null)
      return;

    Page aPage = (Page)areaTree.getPages().elementAt(aPageNumber);
    docWidth  = aPage.getWidth() / 1000;
    docHeight = aPage.getHeight() / 1000;
    setSize((int)(aFactor * aPage.getWidth() / 1000.0 + 2*V_BORDER),
            (int)(aFactor * aPage.getHeight()/ 1000.0 + 2*H_BORDER));
  }

  public void setAreaTree(AreaTree tree) {
    areaTree = tree;
    updateSize(pageNumber, 1.0);
    getParent().getParent().doLayout();
  }


  public void paintComponent(Graphics g) {
    Color ownColor = g.getColor();
    g.setColor(myColor);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(ownColor);
    g.translate(V_BORDER, H_BORDER);

    renderer.setGraphics((Graphics2D)g);
    if (areaTree != null) {
      try {
        renderer.render(areaTree, pageNumber);
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    g.translate(-V_BORDER, -H_BORDER);
  }

  public void setPageNumber(int number) {
    pageNumber = number;
  }

  public Dimension getPreferredSize() {
    return getSize();
  }

  public void setPageCount(int pageCount) {
    previewDialog.setPageCount(pageCount);
  }

}

