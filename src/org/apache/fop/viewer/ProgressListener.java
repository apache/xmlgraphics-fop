package org.apache.fop.viewer;

/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@af-software.de,
  Rainer Steinkuhle: Rainer.Steinkuhle@af-software.de,
  Stanislav Gorkhover: Stanislav.Gorkhover@af-software.de
 */



public interface ProgressListener {
  public void progress(int percentage);
  public void progress(String message);
  public void progress(int percentage, String message);
}

