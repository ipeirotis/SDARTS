

package edu.columbia.cs.sdarts.backend.www;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.BackEndLSPAdapter;
import edu.columbia.cs.sdarts.backend.doc.DocMetaAttributesBuilder;
import edu.columbia.cs.sdarts.backend.doc.DocConfig;
import edu.columbia.cs.sdarts.backend.doc.DocConfigBuilder;
import edu.columbia.cs.sdarts.common.LSPContentSummary;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPResults;
import edu.columbia.cs.sdarts.backend.www.contentsummarybuilder.WWWContentSummaryBuilder;

import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.backend.doc.DocConstants;
import java.io.File;



/**
 * The main class of the package. Provides a
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} wrapper for a collection
 * accessible through web-based search engine.
 * <p>
 * A deployer of this class must be sure to write the following files:
 * <code>www_query.xsl</code>, <code>www_results.xsl</code>, and
 * <code>meta_attributes.xml</code>, and put them in the
 * <code>SDARTS_HOME/config/<i>backEndLSPName</i></code> directory.
 * See the SDARTS README and Design Doc for more information about these
 * files.
 * <p>
 * This class uses a
 * {@link edu.columbia.cs.sdarts.backend.www.WWWQueryProcessor WWWQueryProcessor} to
 * perform queries.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class WWWBackEndLSP extends BackEndLSPAdapter {
  private WWWQueryProcessor queryProcessor;
  private LSPMetaAttributes metaAttributes;
  private DocConfig config;
  
  /**
   * Standard initialization. Also, sets up the <code>WWWQueryProcessor</code>.
   * @param name the name of the <code>BackEndLSP</code>, as it will be known
   * in the <code>sdarts_config.xml</code> file
   * @param description description of the <code>BackEndLSP</code>
   * @param queryLanguages the (computer) query languages the subcollection
   * responds to
   */
  public void initialize (String name, String description,
                          String[] queryLanguages)
    throws BackEndException {
    	
		super.initialize (name, description, queryLanguages);
		queryProcessor = new WWWQueryProcessor (name, getMetaAttributes());
		
		File f = new File(SDARTS.CONFIG_DIRECTORY + File.separator + name + File.separator + DocConstants.CONFIG_FILENAME);
		
		if (f.exists())
		{
		
			try {
				
				config = DocConfigBuilder.load(name);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new BackEndException (e.getMessage());
			}	
				  
	      if (config.reIndex()){
			System.out.println("Creating content summary for " + name);
			System.out.println("This process might take a couple of minutes...");
			
			try{
				WWWContentSummaryBuilder prober = new WWWContentSummaryBuilder(config);
	
				//prober.CreateContentSumary(queryProcessor);
				metaAttributes.setClassification(prober.CreateContentSumary(queryProcessor));
				DocMetaAttributesBuilder builder = new DocMetaAttributesBuilder();
				builder.save(name, metaAttributes);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new BackEndException (e.getMessage());
			}
			
	      }
	  }
  }

  /**
   * Currently, there is no support for generating content summaries from
   * a web-based collection, since most such collections do not offer that
   * information. For a collection that does, the programmer should subclass
   * this class and implement this method.
   * @return <code>null</code>
   */
  public LSPContentSummary getContentSummary() throws BackEndException {
    return null;
  }

  public LSPResults query (LSPQuery query) throws BackEndException {
    return queryProcessor.query (query);
  }

  /**
   * Retrieve the meta-attributes of the underlying subcollection. <b>Note:</b>
   * unlike in other <code>BackEndLSPs</code>, the programmer/administrator
   * must write their own <code>meta_attributes.xml</code> file; it is not
   * automatically generated, as there is no way of telling from the remote
   * web collection what the metadata is. This file goes in the
   * <code>SDARTS_HOME/config/<i>backEndLSPName</i></code> directory.
   * Note also that this class uses
   * the {@link edu.columbia.cs.sdarts.backend.doc.DocMetaAttributesBuilder} to load the
   * meta-attributes.
   * @return the meta-attributes, formatted as an <code>LSPMetaAttributes</code>
   * @exception BackEndException if something goes wrong
   */
  public LSPMetaAttributes getMetaAttributes() throws BackEndException {
    if (metaAttributes == null) {
      DocMetaAttributesBuilder builder = new DocMetaAttributesBuilder();
      metaAttributes = builder.load(getName());
    }
    return metaAttributes;
  }

  // Test code, will go away soon
  /*
  public static void main (String args[]) {
    SDARTS.SDARTS_HOME = "d:\\programming\\java\\sdarts\\xml";
    SDARTS.CONFIG_DIRECTORY = SDARTS.SDARTS_HOME + "\\samples";
    try {
      WWWBackEndLSP backend = new WWWBackEndLSP();
      backend.initialize("americanheart","american heart", new String[] {"starts"});

      BufferedReader br =
        new BufferedReader (
          new InputStreamReader (
            new FileInputStream ("d:\\programming\\java\\sdarts\\xml\\samples\\queryTest.xml")));
      String line = null;
      String qstring = "";
      while ( (line = br.readLine()) != null ) {
        qstring += line;
      }

      LSPQuery query = LSPQueryBuilder.fromXML (new StringReader (qstring), 1000, null);
      LSPResults results = backend.query (query);
      System.out.println ("Got " + results.getNumDocs() + " hits");
      //System.out.println (results.toXML());

      BufferedWriter bw =
        new BufferedWriter (
          new OutputStreamWriter (
            new FileOutputStream ("d:\\programming\\java\\sdarts\\xml\\samples\\rzlts.xml")));
      bw.write (results.toXML());
      bw.flush();
      bw.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  */
}