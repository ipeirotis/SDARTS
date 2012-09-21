package edu.columbia.cs.sdarts.backend.doc;

import edu.columbia.cs.sdarts.common.FieldNames;
import gnu.regexp.RE;


/**
 * A configuration class describing how a field is found in a document.
 * <code>DocFieldDescriptors</code> are stored within a
 * {@link edu.columbia.cs.sdarts.backend.doc.DocConfig DocConfig} class, and are created
 * along with it by a
 * {@link edu.columbia.cs.sdarts.backend.doc.DocConfigBuilder DocConfigBuilder}.
 * A developer using this package can read each <code>DocFieldDescriptor</code>
 * to decide how to parse documents.
 * <p>
 * See the <a href="http://www.cs.columbia.edu/~dli2test/dtd/doc_config.dtd">
 * doc_config DTD </a> for a description of how <code>DocFieldDescriptors</code>
 * are stored as XML inside a <code>doc_config.xml</code> document.
 * <p>
 * Parsing is achieved by leveraging the <code>RE</code> regular expression class
 * from the <a href="http://www.cacas.org/~wes/java/">gnu.regexp</a> library.
 * One regular expression is used to describe where the field starts, and
 * another is used to describe where it ends. If the field is a date field, the
 * descriptor can store multiple instances of
 * <code>java.text.SimpleDateFormat</code>. Each of these indicates to the
 * back-end developer possible ways the document data contained within the
 * regular expression might describe the date. The developer ought to try
 * applying each of these, in the order in which they are stored, until one
 * succeeds in parsing the field data.
 * <p>
 * The regular expressions are implemented using theopen-source
 * <a href="http://www.cacas.org/~wes/java"><code>gnu.regexp</code></a> 
 * library.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class DocFieldDescriptor {
  private String name;
//  private int    code;
  private RE start;
  private RE end;
  private boolean skipStart;
  private boolean skipEnd;


  /**
   * Create a new <code>DocFieldDescriptor</code>
   * @param name the name of the field. This should come from
   * {@link edu.columbia.cs.sdarts.common.FieldNames}.
   * @param skipStart whether the text matching the field start
   * regular expression should be skipped, or added to the field content
   * @param skipEnd whether the text matching the field end
   * regular expression should be skipped, or added to the field content
   */
  public DocFieldDescriptor (String name,
                              boolean skipStart, boolean skipEnd) {
//    if (!FieldNames.isLegal (name)) {
//      throw new IllegalArgumentException ("illegal field name: " + name);
//    }
    this.name = name;
//    this.code = FieldNames.nameToCode (name);
    this.skipStart                 = skipStart;
    this.skipEnd                   = skipEnd;
  }

  /**
   * Return the name of the descriptor, which will be one of the names
   * from {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}.
   * @return the name of the descriptor
   */
   public String getName () {
     return name;
   }

//   /**
//    * Return the code of the descriptor, which will be one of the codes
//    * from {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}.
//    * @return the code of the descriptor
//    */
//   public int getCode () {
//     return code;
//   }

  /**
   * Set the regular expression indicating where the field starts
   * @param start the regular expression
   */
  public void setStart (RE start) {
    this.start = start;
  }

  /**
   * Return the regular expression indicating where the field starts
   * @return the regular expression indicating where the field starts
   */
  public RE getStart() {
    return start;
  }

  /**
   * Set the regular expression indicating where the field starts
   * @param start the regular expression
   */
  public void setEnd (RE end) {
    this.end = end;
  }

  /**
   * Return the regular expression indicating where the field starts
   * @return the regular expression indicating where the field starts
   */
  public RE getEnd() {
    return end;
  }

  /**
   * Return whether this is a date field, by calling the
   * {@link edu.columbia.cs.sdarts.common.FieldNames#isDate(String) isDate()} method
   * of <code>FieldNames</code>.
   * @return whether this is a date field
   */
  public boolean isDate() {
    return (FieldNames.isDate (name));
  }

  /**
   * Indicates whether the text matching the field start
   * regular expression should be skipped, or added to the field content
   * @return whether the text matching the field start
   * regular expression should be skipped, or added to the field content
   */
  public boolean skipStart () {
    return skipStart;
  }

  /**
   * Indicates whether the text matching the field end
   * regular expression should be skipped, or added to the field content
   * @return whether the text matching the field end
   * regular expression should be skipped, or added to the field content
   */
  public boolean skipEnd () {
    return skipEnd;
  }
}
