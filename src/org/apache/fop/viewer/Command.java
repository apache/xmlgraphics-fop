package org.apache.fop.viewer;
/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@af-software.de,
  Rainer Steinkuhle: Rainer.Steinkuhle@af-software.de,
  Stanislav Gorkhover: Stanislav.Gorkhover@af-software.de
 */

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.io.File;


/**
 * Klasse für UI-Kommandos. Die Kommandos können in das Menüsystem oder
 * in eine Toolbar eingefügt werden.<br>
 * <code>Commands</code> unterstützen mehrsprachigkeit.<br>
 * Durch überschreiben der Methode <code>doit<code> kann die Klasse customisiert werden.
 * Über die Methode <code>undoit</code> kann Undo-Funktionalität unterstützt werden.<br>
 *
 * @author Juergen.Verwohlt@af-software.de
 * @version 1.0 18.03.99
 */
public class Command extends AbstractAction {

  public static final String IMAGE_DIR = "../viewer/images/";

  public Command(String name) {
    this(name, (ImageIcon)null);
  }

  public Command(String name, ImageIcon anIcon) {
    super(name, anIcon);
  }


  public Command(String name, ImageIcon anIcon, String path) {
    this(name, anIcon);
    File f = new File (IMAGE_DIR + path + ".gif");
    if (!f.exists()) {
      System.err.println("Icon not found: " + f.getAbsolutePath());
    }

  }

  public Command(String name, String iconName) {
    this(name, new ImageIcon(IMAGE_DIR + iconName + ".gif"), iconName);
  }

  public void actionPerformed(ActionEvent e) {
    doit();
  }

  public void doit() {
    System.err.println("Not implemented.");
  }

  public void undoit() {
    System.err.println("Not implemented.");
  }
}
