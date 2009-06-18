package org.apache.fop.viewer;

import java.util.*;
import java.io.*;


/**
 * Die Klasse <code>SecureResourceBundle</code> ist ein Resourceundle, das im Falle eines fehlenden
 * Eintrages keinen Absturz verursacht, sondern die Meldung
 * <strong>Key <i>key</i> not found</strong> zurückgibt.
 * @see PropertyResourceBundle
 *
 * @author Stanislav.Gorkhover@af-software.de
 * @version 1.0 18.03.1999
 */
public class SecureResourceBundle extends ResourceBundle implements Translator {

  // Fehlende keys mit einer Meldung zurückgeben.
  private boolean isMissingEmphasized = false;

  //private Properties lookup = new Properties();
  private LoadableProperties lookup = new LoadableProperties();

  private boolean isSourceFound = true;

  public void setMissingEmphasized(boolean flag) {
    isMissingEmphasized = flag;
  }

  /**
  * Kreiert ein ResourceBundle mit der Quelle in <strong>in</strong>.
  */

  public SecureResourceBundle(InputStream in) {
    try {
      lookup.load(in);
    } catch(Exception ex) {
      System.out.println("Abgefangene Exception: " + ex.getMessage());
      isSourceFound = false;
    }
  }



  public Enumeration getKeys() {
    return lookup.keys();
  }



  /**
  * Händelt den abgefragten Key, liefert entweder den zugehörigen Wert oder eine Meldung.
  * Die <strong>null</strong> wird nie zurückgegeben.
  * Schreibt die fehlenden Suchschlüssel in die Protokoll-Datei.
  * @return <code>Object</code><UL>
  * <LI>den zu dem Suchschlüssel <strong>key</strong> gefundenen Wert, falls vorhanden, <br>
  * <LI>Meldung <strong>Key <i>key</i> not found</strong>, falls der Suchschlüssel fehlt
  * und die Eigenschaft "jCatalog.DevelopmentStartModus" in der ini-Datei aus true gesetzt ist.
  * <LI>Meldung <strong>Key is null</strong>, falls der Suchschlüssel <code>null</code> ist.
  * </UL>
  *
  */
  public Object handleGetObject(String key) {

    if (key == null)
      return "Key is null";

    Object obj = lookup.get(key);
    if (obj != null)
      return obj;
    else {
      if (isMissingEmphasized) {
        System.out.println(getClass().getName() + ": missing key: " + key);
        return getMissedRepresentation(key.toString());
      }
      else
        return key.toString();
    }
  }

  /**
   * Stellt fest, ob es den Key gibt.
   */
  public boolean contains(String key) {
    return (key == null || lookup.get(key) == null) ? false : true;
  }


  private String getMissedRepresentation(String str) {
    return "<!" + str + "!>";
  }

  public boolean isSourceFound() {
    return isSourceFound;
  }

}