package org.apache.fop.viewer;

/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
  Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
  Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */


public interface ProgressListener {
  public void progress(int percentage);
  public void progress(String message);
  public void progress(int percentage, String message);
}

