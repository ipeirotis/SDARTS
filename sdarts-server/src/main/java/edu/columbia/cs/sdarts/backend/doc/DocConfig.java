package edu.columbia.cs.sdarts.backend.doc;

import java.text.SimpleDateFormat;

/**
 * An object representation of the <code>doc_config.xml</code> descriptor
 * file that a {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} built on the
 * <code>sdarts.backend.doc</code> package uses to configure itself.
 * The format for the configuration file is described in the
 * <a href="http://www.cs.columbia.edu/~dli2test/dtd/doc_config.dtd">
 * text_config DTD</a>.
 * <p>
 * This descriptor tells the <code>BackEndLSP</code> whether to re-index
 * the collection every time the <code>BackEndLSP</code> is instantiated
 * (re-indexing includes building a meta-attributes and content-summary),
 * what paths to look for documents in, whether these paths are recursive,
 * what extensions the document filenames have, what is the linkage,
 * stopwords, etc.
 * <p>
 * The <code>DocConfig</code> also includes one or more
 * {@link edu.columbia.cs.sdarts.backend.doc.DocFieldDescriptor DocFieldDescriptors}, which
 * describe the fields in a document and how to parse them.
 * <p>
 * A <code>DocConfig</code> is instantiated by loading it from
 * the stored file using a
 * {@link edu.columbia.cs.sdarts.backend.doc.DocConfigBuilder DocConfigBuilder} instance.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class DocConfig {
  private boolean               reIndex;
  private boolean               recursive;
  private String                backEndLSPName;
  private String[]              paths;
  private String[]              extensions;
  private String                language;
  private String                linkagePrefix;
  private String                linkageType;
  private String[]              stopWords;
  private DocFieldDescriptor[]  fieldDescriptors;
  private SimpleDateFormat[]    dateFormats;
  private String				classificationSchemaPath;
  private double				specificityThreshold;
  private long					maxDocumentsPerQuery;
  private String				cacheLocation;

  /**
   * Create a new <code>DocConfig</code>
   * @param reIndex whether the collection should be re-indexed when
   * the <code>BackEndLSP</code> is created
   * @param recursive whether the document paths should be recursively
   * traversed, looking into all subdirectories of the paths specified
   */
  public DocConfig (boolean reIndex, boolean recursive) {
    this.reIndex   = reIndex;
    this.recursive = recursive;
  }

  /**
   * Sets the name of the <code>BackEndLSP</code> that will be configured
   * with this <code>DocConfig</code>
   * @param backEndLSPName the name
   */
  public void setBackEndLSPName (String backEndLSPName) {
    this.backEndLSPName = backEndLSPName;
  }

  /**
   * Returns the name of the <code>BackEndLSP</code> that will be configured
   * with this <code>DocConfig</code>
   * @return the name of the <code>BackEndLSP</code> that will be configured
   * with this <code>DocConfig</code>
   */
  public String getBackEndLSPName () {
    return backEndLSPName;
  }

  /**
   * Whether the document paths should be recursively
   * traversed, looking into all subdirectories of the paths specified
   * @return whether the document paths should be recursively
   * traversed, looking into all subdirectories of the paths specified
   */
  public boolean recursive () {
    return recursive;
  }

  /**
   * Whether the collection should be re-indexed when
   * the <code>BackEndLSP</code> is created
   * @return whether the collection should be re-indexed when
   * the <code>BackEndLSP</code> is created
   */
  public boolean reIndex () {
    return reIndex;
  }

  /**
   * Set the filename extensions to look for in documents to index
   * @param extensions the extensions
   */
  public void setExtensions (String[] extensions) {
    this.extensions = extensions;
  }

  /**
   * Return the filename extensions to look for in documents to index
   * @return the filename extensions to look for in documents to index
   */
  public String[] getExtensions() {
    return extensions;
  }

  /**
   * Return the paths that a <code>BackEndLSP</code> should follow to look
   * for documents in the collection
   * @return the paths that a <code>BackEndLSP</code> should follow to look
   * for documents in the collection
   */
  public String[] getPaths () {
    return paths;
  }

  /**
   * Set the paths that a <code>BackEndLSP</code> should follow to look
   * for documents in the collection
   * @param paths the paths
   */
  public void setPaths (String[] paths) {
    this.paths = paths;
  }

  /**
   * Set the field descriptors, which describe what fields to look for
   * in a document, and where to find them
   * @param fieldDescriptors the descriptors
   */
  public void setFieldDescriptors (DocFieldDescriptor[] fieldDescriptors) {
    this.fieldDescriptors = fieldDescriptors;
  }

  /**
   * Return the field descriptors, which describe what fields to look for
   * in a document, and where to find them
   * @return the field descriptors, which describe what fields to look for
   * in a document, and where to find them
   */
  public DocFieldDescriptor[] getFieldDescriptors() {
    return fieldDescriptors;
  }

  /**
   * Set the stop words to ignore when indexing documents
   * @param stopWords the stop words
   */
  public void setStopWords (String[] stopWords) {
    this.stopWords = stopWords;
  }

  /**
   * Return the stop words to ignore when indexing documents
   * @return the stop words to ignore when indexing documents
   */
  public String[] getStopWords() {
    return stopWords;
  }

  /**
   * Set the (human) language of the collection
   * @param language the language
   */
  public void setLanguage (String language) {
    this.language = language;
  }

  /**
   * Return the (human) language of the collection
   * @return the (human) language of the collection
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets the linkage prefix. In STARTS, "linkage" is the method
   * (usually http) for retrieving the document. The linkage prefix
   * is usually a URL of some kind
   * @param linkagePrefix the linkage prefix
   */
  public void setLinkagePrefix (String linkagePrefix) {
    this.linkagePrefix = linkagePrefix;
  }

  /**
   * Return the linkage prefix of the collection
   * @return the linkage prefix of the collection
   */
  public String getLinkagePrefix () {
    return linkagePrefix;
  }

  /**
   * Sets the type of linkage (see the STARTS spec)
   * @param linkageType the type of linkage
   */
  public void setLinkageType (String linkageType) {
    this.linkageType = linkageType;
  }

  /**
   * Return the type of linkage (see the STARTS Spec)
   * @return the type of linkage (see the STARTS Spec)
   */
  public String getLinkageType() {
    return linkageType;
  }

    /**
   * Set the list of possible date formats for the field.
   * @param dateFormats the date formats
   */
  public void setDateFormats (SimpleDateFormat[] dateFormats) {
    this.dateFormats = dateFormats;
  }

  /**
   * Return the list of possible date formats for the field.
   * @return the list of possible date formats for the field.
   */
  public SimpleDateFormat[] getDateFormats () {
    return dateFormats;
  }
  
  public void setClassificationSchemaPath(String path){
  	this.classificationSchemaPath = path;
  }
  
  public String getClassificationSchemaPath(){
  	return this.classificationSchemaPath;
  }
  
  public void setSpecificityThreshold(String specificity){
  	this.specificityThreshold = Double.valueOf(specificity).doubleValue() ;
  }
  
  public double getSpecificityThreshold(){
    return this.specificityThreshold;
  }
    
  public void setMaxDocumentPerQuery(String specificity){
  	this.maxDocumentsPerQuery = Long.valueOf(specificity).longValue();
  }
  
  public long getMaxDocumentPerQuery(){
	return this.maxDocumentsPerQuery;
  }
  
  public void setCacheLocation(String location){
	  this.cacheLocation = location;
	}
  
	public String getCacheLocation(){
	  return this.cacheLocation;
	}
}
