/**
 * A very simple exception class designed to notify classes in
 * the <code>sdarts.frontend</code> that something has gone wrong
 * in the <code>sdarts.backend</code> layer. Should not be passed
 * above the frontend layer. Future versions might have some
 * kind of error code system but at the moment there is none.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
package edu.columbia.cs.sdarts.backend;

public class BackEndException extends Exception {

  public BackEndException() {
    super ();
  }

  public BackEndException (String msg) {
    super (msg);
  }
}
