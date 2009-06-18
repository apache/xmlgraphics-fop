package org.apache.fop.viewer;

/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@af-software.de,
  Rainer Steinkuhle: Rainer.Steinkuhle@af-software.de,
  Stanislav Gorkhover: Stanislav.Gorkhover@af-software.de
 */


import javax.swing.*;
import java.beans.PropertyChangeListener;

public class IconToolBar extends JToolBar {

  public JButton add(Action a) {
    String name = (String) a.getValue(Action.NAME);
    Icon icon = (Icon) a.getValue(Action.SMALL_ICON);
    return add(a, name, icon);
  }

  public JButton add(Action a, String name, Icon icon) {
    JButton b = new JButton(icon);
    b.setToolTipText(name);
    b.setEnabled(a.isEnabled());
    b.addActionListener(a);
    add(b);
    PropertyChangeListener actionPropertyChangeListener =
      createActionChangeListener(b);
    a.addPropertyChangeListener(actionPropertyChangeListener);
    return b;
  }
}


