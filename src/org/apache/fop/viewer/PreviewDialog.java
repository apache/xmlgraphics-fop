
package org.apache.fop.viewer;

/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@af-software.de,
  Rainer Steinkuhle: Rainer.Steinkuhle@af-software.de,
  Stanislav Gorkhover: Stanislav.Gorkhover@af-software.de
 */


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.beans.PropertyChangeListener;

import org.apache.fop.layout.*;
import org.apache.fop.render.awt.*;




/**
 * Frame and User Interface for Preview
 */
public class PreviewDialog extends JFrame {


  protected int currentPage = 0;
  protected int pageCount = 0;

  protected AWTRenderer renderer;

  protected IconToolBar toolBar = new IconToolBar();

  protected Command printAction        = new Command("Print", "Print");// { public void doit() {}}
  protected Command firstPageAction    = new Command("First page",   "firstpg") { public void doit() {goToFirstPage(null);}};
  protected Command previousPageAction = new Command("Previous page", "prevpg") { public void doit() {goToPreviousPage(null);}};
  protected Command nextPageAction     = new Command("Next page",     "nextpg") { public void doit() {goToNextPage(null);}};
  protected Command lastPageAction     = new Command("Last page",     "lastpg") { public void doit() {goToLastPage(null);}};

  protected JLabel zoomLabel = new JLabel(); //{public float getAlignmentY() { return 0.0f; }};
  protected JComboBox scale = new JComboBox() {public float getAlignmentY() { return 0.5f; }};

  protected JScrollPane previewArea = new JScrollPane();
  protected JLabel statusBar = new JLabel();
  protected DocumentPanel docPanel;




  public PreviewDialog(AWTRenderer aRenderer) {
    renderer = aRenderer;


    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setSize(new Dimension(379, 476));
    previewArea.setMinimumSize(new Dimension(50, 50));
    statusBar.setText("");
    statusBar.setBackground(new Color(0, 0, 231));
    this.setTitle("FOP: AWT-Preview");

    scale.addItem("25");
    scale.addItem("50");
    scale.addItem("75");
    scale.addItem("100");
    scale.addItem("150");
    scale.addItem("200");

    scale.setMaximumSize(new Dimension(80, 24));
    scale.setPreferredSize(new Dimension(80, 24));

    scale.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        scale_actionPerformed(e);
      }
    });

    scale.setSelectedItem("100");
    renderer.setScaleFactor(100.0);

    zoomLabel.setText("Zoom");

    this.setJMenuBar(setupMenue());

    this.getContentPane().add(toolBar, BorderLayout.NORTH);
    this.getContentPane().add(statusBar, BorderLayout.SOUTH);

    toolBar.add(printAction);
    toolBar.addSeparator();
    toolBar.add(firstPageAction);
    toolBar.add(previousPageAction);
    toolBar.add(nextPageAction);
    toolBar.add(lastPageAction);
    toolBar.addSeparator();
    toolBar.add(zoomLabel, null);
    toolBar.addSeparator();
    toolBar.add(scale, null);

    this.getContentPane().add(previewArea, BorderLayout.CENTER);

    docPanel = new DocumentPanel(renderer, this);


    previewArea.setSize(docPanel.getSize());
    previewArea.getViewport().add(docPanel);
    statusBar.setText("FOTree --> AreaTree ...");

  }


  JMenuBar setupMenue() {
    JMenuBar  menuBar;
    JMenuItem menuItem;
    JMenu     menu;
    JMenu     subMenu;

    menuBar = new JMenuBar();
      menu = new JMenu("File");
        subMenu = new JMenu("OutputFormat");
          subMenu.add(new Command("mHTML"));
          subMenu.add(new Command("mPDF"));
          subMenu.add(new Command("mRTF"));
          subMenu.add(new Command("mTEXT"));
        // menu.add(subMenu);
        // menu.addSeparator();
        menu.add(new Command("Print"));
        menu.addSeparator();
        menu.add(new Command("Exit") { public void doit() {dispose();}} );
      menuBar.add(menu);
      menu = new JMenu("View");
        menu.add(new Command("First page") { public void doit() {goToFirstPage(null);}} );
        menu.add(new Command("Previous page") { public void doit() {goToPreviousPage(null);}} );
        menu.add(new Command("Next page") { public void doit() {goToNextPage(null);}} );
        menu.add(new Command("Last page") { public void doit() {goToLastPage(null);}} );
        menu.addSeparator();
        subMenu = new JMenu("Zoom");
          subMenu.add(new Command("25%") { public void doit() {setScale(25.0);}} );
          subMenu.add(new Command("50%") { public void doit() {setScale(50.0);}} );
          subMenu.add(new Command("75%") { public void doit() {setScale(75.0);}} );
          subMenu.add(new Command("100%") { public void doit() {setScale(100.0);}} );
          subMenu.add(new Command("150%") { public void doit() {setScale(150.0);}} );
          subMenu.add(new Command("200%") { public void doit() {setScale(200.0);}} );
        menu.add(subMenu);
        menu.addSeparator();
        menu.add(new Command("Default zoom") { public void doit() {setScale(100.0);}} );
      menuBar.add(menu);
      menu = new JMenu("Help");
        menu.add(new Command("Index"));
        menu.addSeparator();
        menu.add(new Command("Introduction"));
        menu.addSeparator();
        menu.add(new Command("About"){ public void doit() {startHelpAbout(null);}} );
      menuBar.add(menu);
    return menuBar;
  }


  public void dispose() {
    System.exit(0);
  }


  //Aktion Hilfe | Info durchgeführt

  public void startHelpAbout(ActionEvent e) {
    PreviewDialogAboutBox dlg = new PreviewDialogAboutBox(this);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.show();
  }

  void goToPage(int number) {
    docPanel.setPageNumber(number);
    repaint();
    previewArea.repaint();
    statusBar.setText("Page " + (number + 1) + " of " + pageCount);
  }

  /**
   * Shows the previous page.
   */
  void goToPreviousPage(ActionEvent e) {
    if (currentPage <= 0)
      return;
    currentPage--;
    goToPage(currentPage);
  }


  /**
   * Shows the next page.
   */
  void goToNextPage(ActionEvent e) {
    if (currentPage >= pageCount - 1)
      return;
    currentPage++;
    goToPage(currentPage);
  }

  /**
   * Shows the last page.
   */
  void goToLastPage(ActionEvent e) {

    if (currentPage == pageCount - 1) return;
    currentPage = pageCount - 1;

    goToPage(currentPage);
  }

  /**
   * Shows the first page.
   */
  void goToFirstPage(ActionEvent e) {
    if (currentPage == 0)
      return;
    currentPage = 0;
    goToPage(currentPage);
  }

  void print(ActionEvent e) {
    Properties p = null;

    Container parent = this.getRootPane();
    while ( !( parent instanceof Frame )) parent = parent.getParent();
    Frame f = (Frame)parent;

    PrintJob pj = f.getToolkit().getPrintJob(f, getTitle(), p);
    if(pj == null) return;
    Graphics pg = pj.getGraphics();
    if (pg != null) {
      docPanel.paintAll(pg);
      pg.dispose();
    }
    pj.end();
  }

  public void setScale(double scaleFactor) {

    if (scaleFactor == 25.0)
      scale.setSelectedIndex(0);
    else if (scaleFactor == 50.0)
      scale.setSelectedIndex(1);
    else if (scaleFactor == 75.0)
      scale.setSelectedIndex(2);
    else if (scaleFactor == 100.0)
      scale.setSelectedIndex(3);
    else if (scaleFactor == 150.0)
      scale.setSelectedIndex(4);
    else if (scaleFactor == 200.0)
      scale.setSelectedIndex(5);

    renderer.setScaleFactor(scaleFactor);
    previewArea.invalidate();
    previewArea.repaint();
  }

  void scale_actionPerformed(ActionEvent e) {
    setScale(new Double((String)scale.getSelectedItem()).doubleValue());
  }


  public void setPageCount(int aPageCount) {
    pageCount = aPageCount;
    statusBar.setText("Page 1 of " + pageCount);
  }

}  // class PreviewDialog



















