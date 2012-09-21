

package edu.columbia.cs.sdarts.common;

/**
 * A "constants container" for all legal STARTS field names and codes.
 * Whenever referring to STARTS fields by name or code, <b>always use
 * these constants</b>. This minimizes the impact of change, should
 * a field name ever change.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */

public class FieldNames {

  // NOTE TO DEVELOPERS - KEEP THE STRING NAMES, THE INT CODES, AND
  // THE STRING ARRAY IN THE SAME ORDER, OR ELSE THE CODETONAME()
  // AND NAMETOCODE() METHODS WILL BREAK DOWN!


  public static final String TITLE                   = "title";
  public static final String AUTHOR                  = "author";
  public static final String BODY_OF_TEXT            = "body-of-text";
  public static final String DOCUMENT_TEXT           = "document-text";
  public static final String DATE_LAST_MODIFIED      = "date-last-modified";
  public static final String ANY                     = "any";
  public static final String LINKAGE                 = "linkage";
  public static final String LINKAGE_TYPE            = "linkage-type";
  public static final String CROSS_REFERENCE_LINKAGE = "cross-reference-linkage";
  public static final String LANGUAGE                = "language";
  public static final String FREE_FORM_TEXT          = "free-form-text";
//  public static final String MESH_TERM				 = "mesh-term";
//  public static final String TITLE_ABSTRACT			 = "title-abstract";
//  public static final String JOURNAL_TITLE			 = "journal-title";
//  public static final String PUBLICATION_TYPE		 = "publication-type";
//  public static final String MESH_MAJOR_TOPIC		 = "mesh-major-topic";
//  public static final String MESH_SUBHEADING		 = "mesh-subheading";
//  public static final String MESH_WITH_SUBHEADING	 = "mesh-with-subheading";
//
//  public static final int TITLE_CODE                   = 0;
//  public static final int AUTHOR_CODE                  = 1;
//  public static final int BODY_OF_TEXT_CODE            = 2;
//  public static final int DOCUMENT_TEXT_CODE           = 3;
//  public static final int DATE_LAST_MODIFIED_CODE      = 4;
//  public static final int ANY_CODE                     = 5;
//  public static final int LINKAGE_CODE                 = 6;
//  public static final int LINKAGE_TYPE_CODE            = 7;
//  public static final int CROSS_REFERENCE_LINKAGE_CODE = 8;
//  public static final int LANGUAGE_CODE                = 9;
//  public static final int FREE_FORM_TEXT_CODE          = 10;
//  public static final int MESH_TERM_CODE			   = 11;
//  public static final int TITLE_ABSTRACT_CODE		   = 12;
//  public static final int JOURNAL_TITLE_CODE		   = 13;
//  public static final int PUBLICATION_TYPE_CODE		   = 14;
//  public static final int MESH_MAJOR_TOPIC_CODE		   = 15;
//  public static final int MESH_SUBHEADING_CODE		   = 16;
//  public static final int MESH_WITH_SUBHEADING_CODE		   = 17;  
//
//  public static final String[] FIELD_NAMES =
//    {TITLE, AUTHOR, BODY_OF_TEXT, DOCUMENT_TEXT, DATE_LAST_MODIFIED,
//      ANY, LINKAGE, LINKAGE_TYPE, CROSS_REFERENCE_LINKAGE, LANGUAGE,
//      FREE_FORM_TEXT, MESH_TERM, TITLE_ABSTRACT, JOURNAL_TITLE,
//	  PUBLICATION_TYPE, MESH_MAJOR_TOPIC, MESH_SUBHEADING, MESH_WITH_SUBHEADING};
//
//  /**
//   * Convert a field code to its name
//   * @code the field code
//   * @return the field name
//   */
//  public static final String codeToName (int code) {
//    return FIELD_NAMES [code];
//  }
//
//  /**
//   * Convert a field name to its code
//   * @name the field name
//   * @return the field code
//   */
//  public static final int nameToCode (String name) {
//    int len = FIELD_NAMES.length;
//    for (int i = 0 ; i < len ; i++) {
//      if (FIELD_NAMES[i].equals (name)) {
//        return i;
//      }
//    }
//    
//    throw new IllegalArgumentException ("field: " + name);
//  }

  public static boolean isDate (String fieldName) {
    return (fieldName.equals (DATE_LAST_MODIFIED));
  }

//  public static boolean isLegal (int code) {
//    return (code >=0 && code < FIELD_NAMES.length);
//  }


  private FieldNames () {}
}
