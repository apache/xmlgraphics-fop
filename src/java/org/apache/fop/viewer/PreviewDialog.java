/*
 * $Id: PreviewDialog.java,v 1.14 2003/03/07 10:09:58 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.viewer;

//Java
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;

//FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.apps.FOPException;
import org.apache.fop.render.awt.AWTRenderer;

/**
 * AWT Viewer main window.
 * Originally contributed by:
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */
public class PreviewDialog extends JFrame {
    
    /** The Translator for localization */
    protected Translator translator;
    /** The AWT renderer */
    protected AWTRenderer renderer;
    /** The InputHandler associated with this window */
    protected InputHandler inputHandler;
    /** The Driver used for refreshing/reloading the view */
    protected Driver driver;

    private int currentPage = 0;
    private int pageCount = 0;
    private Reloader reloader;
    private JComboBox scale;
    private JLabel processStatus;
    private JLabel pageLabel;
    private JLabel infoStatus;

    /**
     * Creates a new PreviewDialog that uses the given renderer.
     * @param aRenderer the to use renderer
     */
    public PreviewDialog(AWTRenderer aRenderer, InputHandler handler) {
        renderer = aRenderer;
        inputHandler = handler;
        translator = renderer.getTranslator();

        //Commands aka Actions
        Command printAction = new Command(translator.getString("Menu.Print"), "Print") {
            public void doit() {
                print();
            }
        };
        Command firstPageAction = new Command(translator.getString("Menu.First.page"),
                                      "firstpg") {
            public void doit() {
                goToFirstPage();
            }
        };
        Command previousPageAction = new Command(translator.getString("Menu.Prev.page"),
                                         "prevpg") {
            public void doit() {
                goToPreviousPage();
            }
        };
        Command nextPageAction = new Command(translator.getString("Menu.Next.page"), "nextpg") {
            public void doit() {
                goToNextPage();
            }

        };
        Command lastPageAction = new Command(translator.getString("Menu.Last.page"), "lastpg") {
            public void doit() {
                goToLastPage();
            }
        };
        Command reloadAction = new Command(translator.getString("Menu.Reload"), "reload") {
            public void doit() {
                reload();
            }
        };

        setTitle("FOP: AWT-" + translator.getString("Title.Preview"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        //Sets size to be 61%x90% of the screen size
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        //Rather frivolous size - fits A4 page width in 1024x768 screen on my desktop
        setSize(screen.width * 61 / 100, screen.height * 9 / 10);

        //Page view stuff
        pageLabel = new JLabel();
        JScrollPane previewArea = new JScrollPane(pageLabel);
        previewArea.getViewport().setBackground(Color.gray);
        previewArea.setMinimumSize(new Dimension(50, 50));
        getContentPane().add(previewArea, BorderLayout.CENTER);

        //Scaling combobox
        scale = new JComboBox();
        scale.addItem("25%");
        scale.addItem("50%");
        scale.addItem("75%");
        scale.addItem("100%");
        scale.addItem("150%");
        scale.addItem("200%");
        scale.setMaximumSize(new Dimension(80, 24));
        scale.setPreferredSize(new Dimension(80, 24));
        scale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaleActionPerformed(e);
            }
        });
        scale.setSelectedItem("100%");
        renderer.setScaleFactor(100.0);

        //Menu
        setJMenuBar(setupMenu());

        //Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.add(printAction);
        toolBar.add(reloadAction);
        toolBar.addSeparator();
        toolBar.add(firstPageAction);
        toolBar.add(previousPageAction);
        toolBar.add(nextPageAction);
        toolBar.add(lastPageAction);
        toolBar.addSeparator();
        toolBar.add(new JLabel(translator.getString("Menu.Zoom")));
        toolBar.addSeparator();
        toolBar.add(scale);
        getContentPane().add(toolBar, BorderLayout.NORTH);
        //Status bar
        JPanel statusBar = new JPanel();
        processStatus = new JLabel();
        processStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), 
                BorderFactory.createEmptyBorder(0, 3, 0, 0)));
        infoStatus = new JLabel();
        infoStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), 
                BorderFactory.createEmptyBorder(0, 3, 0, 0)));

        statusBar.setLayout(new GridBagLayout());

        processStatus.setPreferredSize(new Dimension(200, 21));
        processStatus.setMinimumSize(new Dimension(200, 21));

        infoStatus.setPreferredSize(new Dimension(100, 21));
        infoStatus.setMinimumSize(new Dimension(100, 21));
        statusBar.add(processStatus,
                      new GridBagConstraints(0, 0, 1, 0, 2.0, 0.0,
                                             GridBagConstraints.CENTER,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 3), 0, 0));
        statusBar.add(infoStatus,
                      new GridBagConstraints(1, 0, 1, 0, 1.0, 0.0,
                                             GridBagConstraints.CENTER,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Creates a new menubar to be shown in this window.
     * @return the newly created menubar
     */
    private JMenuBar setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu(translator.getString("Menu.File"));

        //Adds mostly the same actions, but without icons
        menu.add(new Command(translator.getString("Menu.Print")) {
            public void doit() {
                print();
            }
        });
        // inputHandler must be set to allow reloading
        if (inputHandler != null) {
            menu.add(new Command(translator.getString("Menu.Reload")) {
                public void doit() {
                    reload();
                }
            });
        }
        menu.addSeparator();
        menu.add(new Command(translator.getString("Menu.Exit")) {
            public void doit() {
                dispose();
            }
        });
        menuBar.add(menu);
        menu = new JMenu(translator.getString("Menu.View"));
        menu.add(new Command(translator.getString("Menu.First.page")) {
            public void doit() {
                goToFirstPage();
            }
        });
        menu.add(new Command(translator.getString("Menu.Prev.page")) {
            public void doit() {
                goToPreviousPage();
            }
        });
        menu.add(new Command(translator.getString("Menu.Next.page")) {
            public void doit() {
                goToNextPage();
            }
        });
        menu.add(new Command(translator.getString("Menu.Last.page")) {
            public void doit() {
                goToLastPage();
            }
        });
        menu.add(new Command(translator.getString("Menu.Go.to.Page") + " ...") {
            public void doit() {
                showGoToPageDialog();
            }
        });
        menu.addSeparator();
        JMenu subMenu = new JMenu(translator.getString("Menu.Zoom"));
        subMenu.add(new Command("25%") {
            public void doit() {
                setScale(25.0);
            }
        });
        subMenu.add(new Command("50%") {
            public void doit() {
                setScale(50.0);
            }
        });
        subMenu.add(new Command("75%") {
            public void doit() {
                setScale(75.0);
            }
        });
        subMenu.add(new Command("100%") {
            public void doit() {
                setScale(100.0);
            }
        });
        subMenu.add(new Command("150%") {
            public void doit() {
                setScale(150.0);
            }
        });
        subMenu.add(new Command("200%") {
            public void doit() {
                setScale(200.0);
            }
        });
        menu.add(subMenu);
        menu.addSeparator();
        menu.add(new Command(translator.getString("Menu.Default.zoom")) {
            public void doit() {
                setScale(100.0);
            }
        });
        menuBar.add(menu);
        menu = new JMenu(translator.getString("Menu.Help"));
        menu.add(new Command(translator.getString("Menu.About")) {
            public void doit() {
                startHelpAbout();
            }
        });
        menuBar.add(menu);
        return menuBar;
    }

    /**
     * Shows the About box
     */
    private void startHelpAbout() {
        PreviewDialogAboutBox dlg = new PreviewDialogAboutBox(this, translator);
        //Centers the box
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setVisible(true);
    }

    /**
     * Changes the current visible page
     * @param number the page number to go to
     */
      private void goToPage(int number) {
        currentPage = number;
        renderer.setPageNumber(number);
        showPage();
    }

    /**
     * Shows the previous page.
     */
    private void goToPreviousPage() {
        if (currentPage <= 0) {
            return;
        }
        currentPage--;
        goToPage(currentPage);
    }


    /**
     * Shows the next page.
     */
    private void goToNextPage() {
        if (currentPage >= pageCount - 1) {
            return;
        }
        currentPage++;
        goToPage(currentPage);
    }

    /**
     * Shows the last page.
     */
    private void goToLastPage() {
        if (currentPage == pageCount - 1) {
            return;
        }
        currentPage = pageCount - 1;
        goToPage(currentPage);
    }

    /**
     * Reloads and reformats document.
     */
    private synchronized void reload() {
        if (reloader == null || !reloader.isAlive()) {
            reloader = new Reloader();
            reloader.start();
        }
    }

    /**
     * This class is used to reload document  in
     * a thread safe way.
     */
    private class Reloader extends Thread {
        public void run() {
            if (driver == null) {
                driver = new Driver();
                driver.setRenderer(renderer);
            } else {
                driver.reset();
            }
            
            pageLabel.setIcon(null);
            infoStatus.setText("");
            currentPage = 0;
            //Cleans up renderer - to be done
            //while (renderer.getPageCount() != 0)
            //    renderer.removePage(0);
            try {
                setStatus(translator.getString("Status.Build.FO.tree"));
                driver.render(inputHandler);
                setStatus(translator.getString("Status.Show"));
            } catch (FOPException e) {
                reportException(e);
            }
        }
    }

    /**
     * Shows "go to page" dialog and then goes to the selected page
     */
    private void showGoToPageDialog() {
        GoToPageDialog d = new GoToPageDialog(this,
            translator.getString("Menu.Go.to.Page"), translator);
        d.setLocation((int)getLocation().getX() + 50,
                      (int)getLocation().getY() + 50);
        d.setVisible(true);
        currentPage = d.getPageNumber();
        if (currentPage < 1 || currentPage > pageCount) {
            return;
        }
        currentPage--;
        goToPage(currentPage);
    }

    /**
     * Shows the first page.
     */
    private void goToFirstPage() {
        if (currentPage == 0) {
            return;
        }
        currentPage = 0;
        goToPage(currentPage);
    }

    /**
     * Prints the document
     */
    private void print() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPageable(renderer);
        if (pj.printDialog()) {
            try {
                pj.print();
            } catch (PrinterException pe) {
                pe.printStackTrace();
            }
        }
    }

    /**
     * Scales page image
     */
    private void setScale(double scaleFactor) {
        if (scaleFactor == 25.0) {
            scale.setSelectedIndex(0);
        } else if (scaleFactor == 50.0) {
            scale.setSelectedIndex(1);
        } else if (scaleFactor == 75.0) {
            scale.setSelectedIndex(2);
        } else if (scaleFactor == 100.0) {
            scale.setSelectedIndex(3);
        } else if (scaleFactor == 150.0) {
            scale.setSelectedIndex(4);
        } else if (scaleFactor == 200.0) {
            scale.setSelectedIndex(5);
        }
        renderer.setScaleFactor(scaleFactor);
        showPage();
    }

    private void scaleActionPerformed(ActionEvent e) {
        String item = (String)scale.getSelectedItem();
        setScale(Double.parseDouble(item.substring(0, item.indexOf('%'))));
    }

    /**
     * Sets message to be shown in the status bar in a thread safe way.
     * @param message the message
     */
    public void setStatus(String message) {
        SwingUtilities.invokeLater(new ShowStatus(message));
    }

    /**
     * This class is used to show status in a thread safe way.
     */
    private class ShowStatus implements Runnable {
        /**
         * The message to display
         */
        private String message;
        /**
         * Constructs  ShowStatus thread
         * @param message message to display
         */
        public ShowStatus(String message) {
            this.message = message;
        }
        
        public void run() {
            processStatus.setText(message.toString());
        }
    }

    /**
     * Starts rendering process and shows the current page.
     */
    public void showPage() {
        ShowPageImage viewer = new ShowPageImage();
        if (SwingUtilities.isEventDispatchThread()) {
            viewer.run();
        } else {
            SwingUtilities.invokeLater(viewer);
        }
    }


    /**
     * This class is used to update the page image
     * in a thread safe way.
     */
    private class ShowPageImage implements Runnable {
        /**
         * The run method that does the actual updating
         */
        public void run() {
            BufferedImage pageImage = null;
            Graphics graphics = null;

//          renderer.render(currentPage);
            pageImage = renderer.getLastRenderedPage();
            if (pageImage == null)
                return;
            graphics = pageImage.getGraphics();
            graphics.setColor(Color.black);
            graphics.drawRect(0, 0, pageImage.getWidth() - 1,
                              pageImage.getHeight() - 1);

            pageLabel.setIcon(new ImageIcon(pageImage));
            pageCount = renderer.getPageCount();

            //Updates status bar
            infoStatus.setText(translator.getString("Status.Page") + " "
                + (currentPage + 1) + " "
                + translator.getString("Status.of") + " " + pageCount);
        }
    }

    /**
     * Opens standard Swing error dialog box and reports given exception details.
     * @param e the Exception
     */
    public void reportException(Exception e) {
        String msg = translator.getString("Exception.Occured");
        setStatus(msg);
        JOptionPane.showMessageDialog(
            getContentPane(),
            "<html><b>" + msg + ":</b><br>"
                + e.getClass().getName() + "<br>"
                + e.getMessage() + "</html>", 
            translator.getString("Exception.Error"),
            JOptionPane.ERROR_MESSAGE
        );
    }
}

