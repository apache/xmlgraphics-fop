package org.apache.fop.viewer;


/**
 * Definition für die Übersetzer-Klassen.
 *
 * @version 03.12.99
 * @author Stanislav.Gorkhover@af-software.de
 *
 */
public interface Translator {

  /**
   * Übersetzt ein Wort.
   */
  public String getString(String key);
  /**
   * Ein Translator soll die fehlenden keys hervorheben können.
   */
  public void setMissingEmphasized(boolean b);
  /**
   * Gibt an ob die Übersetzungsquelle gefunden ist.
   */
  public boolean isSourceFound();
  /**
   * Gibt an ob ein Key in der Übersetzungsquelle vorhanden ist.
   */
  public boolean contains(String key);
}