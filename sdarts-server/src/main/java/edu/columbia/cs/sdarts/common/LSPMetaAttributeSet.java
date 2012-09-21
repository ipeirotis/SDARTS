
package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * This helper object fits inside the
 * {@link edu.columbia.cs.sdarts.common.LSPMetaAttributes LSPMetaAttributes}
 * object,
 * which, as per STARTS 1.0, stores all meta-attributes for a specific source.
 * This object has been hardcoded with the <code>mbasic1</code> attributes.
 * Its attributes are separate from <code>LSPMetaAttributes</code>, following
 * the way the STARTS protocol separates them - but really all this class is
 * is a collection of even more attributes.
 * <p>
 * There are getter methods
 * for reading all the attributes after instantiation, but they are rarely
 * used; the framework reads this object using the <code>toXML()</code>
 * method.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by</i> <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class LSPMetaAttributeSet extends LSPObject {
  // ----------- FIELDS ------------
  public static final String typeSet = "mbasic1";
  private String sourceLanguage;
  private String sourceName;
  private String class_t;        // the actual field name is class
  private String linkage;
  private String contentSummaryLinkage;
  private String dateChanged;
  private String dateExpires;
  private String abstrct;
  private String accessConstants;
  private String contact;


  /**
   * See the STARTS 1.0 specification for more details of these parameters
   * Note that <code>linkage</code> and <code>contentSummaryLinkage</code>
   * must not be <code>null</code>, or an error will result. Any of the others
   * can be.
   * <p>
   * @param sourceLanguage the (human) language of the underlying subcollection
   * @param sourceName the name of the subcollection. This is obtainable via
   * the {@link edu.columbia.cs.sdarts.backend.BackEndLSP#getName() getName()} method of
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}
   * @param class_t the class the this collection belongs to
   * @param linkage the "linkage" of the underlying collection - how to reach
   * it directly without SDARTS (usually via a web server, etc.)
   * @param contentSummaryLinkage where to find the content summary. This
   * is a good way to make an XML version of
   * {@link edu.columbia.cs.sdarts.common.LSPContentSummary LSPContentSummary} available without
   * using SDARTS, since typically content summaries are too big for SDLIP to
   * deliver at the present time.
   * @param dateChanged the last time the collection changed
   * @param dateExpires when the collection information expires
   * @param abstrct an abstract of the collection's contents. See if you can
   * guess why the variable can't be called "abstract."
   * @param accessConstants the access constants for the collection
   * @param contact who to talk to about the subcollection
   */
  public LSPMetaAttributeSet (String sourceLanguage, String sourceName,
                              String class_t,
                              String linkage, String contentSummaryLinkage,
                              String dateChanged, String dateExpires,
                              String abstrct, String accessConstants,
                              String contact) {
    if (linkage == null || contentSummaryLinkage == null) {
      throw new InstantiationError (
          "linkage and contentsummarylinkage cannot be null");
    }
    this.sourceLanguage = sourceLanguage;
    this.sourceName = sourceName;
    this.class_t = class_t;
    this.linkage = linkage;
    this.contentSummaryLinkage = contentSummaryLinkage;
    this.dateChanged = dateChanged;
    this.dateExpires = dateExpires;
    this.abstrct = abstrct;
    this.accessConstants = accessConstants;
    this.contact = contact;
  }

  /**
   * Returns the source language of the collection
   * @return the source language of the collection
   */
  public String getSourceLanguage () {
    return sourceLanguage;
  }

  /**
   * Returns the source name of the collection. This is obtainable via
   * the {@link edu.columbia.cs.sdarts.backend.BackEndLSP#getName() getName()} method of
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}
   * @return the source name of the collection
   */
  public String getSourceName () {
    return sourceName;
  }

  /**
   * Returns the class_t of the collection
   * @return the class_t of the collection
   */
  public String getClass_t () {
    return class_t;
  }

  /**
   * Returns the linkage of the collection
   * @return the linkage of the collection
   */
  public String getLinkage () {
    return linkage;
  }

  /**
   * Returns the content summary linkage of the collection
   * @return the content summary linkage of the collection
   */
  public String getContentSummaryLinkage () {
    return contentSummaryLinkage;
  }

  /**
   * Returns the last time the collection changed
   * @return the last time the collection changed
   */
  public String getDateChanged () {
    return dateChanged;
  }

  /**
   * Returns the expiration date of the collection
   * @return the expiration date of the collection
   */
  public String getDateExpires () {
    return dateExpires;
  }

  /**
   * Returns the abstract of the collection
   * @return the abstract of the collection
   */
  public String getAbstract () {
    return abstrct;
  }

  /**
   * Returns the access constants of the collection
   * @return the access constants of the collection
   */
  public String getAccessConstants () {
    return accessConstants;
  }

  /**
   * Returns the contact for the collection
   * @return the contact for the collection
   */
  public String getContact () {
    return contact;
  }

  public void toXML (XMLWriter writer) throws IOException {
    writer.setDefaultFormat();
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    writer.printStartElement ("meta-attribute-set", true);
    writer.printAttribute ("type-set", typeSet);
    writer.printStartElementClose ();
    writer.indent();
    writer.printEntireElement ("class", class_t);
    writer.printEntireElement ("linkage", linkage);
    writer.printEntireElement ("content-summary-linkage",contentSummaryLinkage);
    if (sourceLanguage != null) {
      writer.printEntireElement ("source-language", sourceLanguage);
    }
    if (sourceName != null) {
      writer.printEntireElement ("source-name", sourceName);
    }
    if (dateChanged != null) {
      writer.printEntireElement ("date-changed", dateChanged);
    }
    if (dateExpires != null) {
      writer.printEntireElement ("date-expires", dateExpires);
    }
    if (abstrct != null) {
      writer.printEntireElement ("abstract", abstrct);
    }
    if (accessConstants != null) {
      writer.printEntireElement ("access-constants", accessConstants);
    }
    if (contact != null) {
      writer.printEntireElement ("contact", contact);
    }
    writer.unindent();
    writer.printEndElement ("meta-attribute-set");
    writer.exitNamespace();
  }
}
