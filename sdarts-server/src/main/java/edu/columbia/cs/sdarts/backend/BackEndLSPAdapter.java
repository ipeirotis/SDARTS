package edu.columbia.cs.sdarts.backend;


/**
 * This slightly more concrete implementation of <code>BackEndLSP</code>
 * includes storage and getter implementations for
 * for the most basic, SDLIP-defined characteristics of the wrapped
 * subcollection. It sets these properties through the
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP#initialize(String,String,String[])}
 * method. This method should still be overridden by any subclass, to
 * provide additional initialization. The overriding <code>initialize()</code>
 * should use <code>super</code> to call onto this method here.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public abstract class BackEndLSPAdapter implements BackEndLSP {
  private String name;
  private String description;
  private String[] queryLanguages;

  /**
   * Sets the basic <code>BackEndLSP</code> properties.
   * This is also a good place for the <code>BackEndLSP</code> to load any
   * configuration files, indexes, etc., that it might need, and to configure
   * itself accordingly. Thus, this method should still be overridden by any subclass, to
   * provide additional initialization. The overriding <code>initialize()</code>
   * should use <code>super</code> to call onto this method here.
   * <p>
   * @param name the name of the subcollection
   * @param description the description of the subcollection
   * @param queryLanguages the (computer) query languages the subcollection
   * responds to
   * @exception BackEndException if something goes wrong
   */
  public void initialize (String name, String description,
                          String[] queryLanguages)
    throws BackEndException {
      this.name = name;
      this.description = description;
      this.queryLanguages = queryLanguages;
  }


  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String[] getQueryLanguages() {
    return queryLanguages;
  }
}
