package edu.columbia.cs.sdarts.backend.doc.lucene;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import com.lucene.analysis.Analyzer;
import com.lucene.analysis.LowerCaseTokenizer;
import com.lucene.analysis.StopAnalyzer;
import com.lucene.analysis.StopFilter;
import com.lucene.analysis.TokenStream;
import com.lucene.document.DateField;
import com.lucene.document.Document;
import com.lucene.index.IndexReader;
import com.lucene.index.IndexWriter;
import com.lucene.index.Term;
import com.lucene.index.TermDocs;
import com.lucene.index.TermEnum;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.doc.DocConfig;
import edu.columbia.cs.sdarts.backend.doc.DocConfigBuilder;
import edu.columbia.cs.sdarts.backend.doc.DocConstants;
import edu.columbia.cs.sdarts.backend.doc.DocContentSummaryBuilder;
import edu.columbia.cs.sdarts.backend.doc.DocFieldDescriptor;
import edu.columbia.cs.sdarts.backend.doc.DocMetaAttributesBuilder;
import edu.columbia.cs.sdarts.common.FieldNames;
import edu.columbia.cs.sdarts.common.LSPContentSummary;
import edu.columbia.cs.sdarts.common.LSPField;
import edu.columbia.cs.sdarts.common.LSPMetaAttributeSet;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPModifier;
import edu.columbia.cs.sdarts.common.LSPObject;
import edu.columbia.cs.sdarts.common.LSPTerm;
import edu.columbia.cs.sdarts.common.STARTS;
import edu.columbia.cs.sdarts.frontend.BackEndLSPDescriptor;
import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.frontend.SDARTSConfig;
import edu.columbia.cs.sdarts.frontend.SDARTSConfigBuilder;
import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Used by the {@link edu.columbia.cs.sdarts.backend.doc.lucene.LuceneSearchEngine LuceneSearchEngine}
 * to orchestrate the generation of the
 * index, meta-attributes, and content-summary files. It is also used by
 * the scripts included with SDARTS, <code>textsetup.sh</code> and
 * <code>xmlsetup.sh</code>, to perform
 * this same task when the SDARTS server is not running. See the SDARTS
 * README and Design Document for more details.
 * <p>
 * All told, the class uses and generates
 * the following files, all of them in the
 * <code>SDARTS_HOME/config/<i>backEndLSPName</i></code> directory:
 * <ul>
 * <li><code>/index</code> - a subdirectory containing the Lucene index
 * <li><code>meta-attributes.xml</code> - the meta-attributes for the
 * collection (generated, but can also be edited by hand)
 * <li><code>content-summary.xml</code> - the content-summary for the
 * collection (generated)
 * </ul>
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @author <i>modified by:</i> <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class LuceneSetup {
	/** The default location of STARTS DTD */
	public static final String STARTS_DTD_URL = "http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd";

	/**
	 * Used by the {@link edu.columbia.cs.sdarts.backend.doc.lucene.LuceneSearchEngine LuceneSearchEngine}
	 * if re-indexing is required. It uses the default location for the the STARTS DTD.
	 * @param name the name of the <code>BackEndLSP</code>
	 * @param desc the description of the <code>BackEndLSP</code>
	 * @param overWrite whether to overwrite the meta-attributes file
	 * @param config the <code>TextConfig</code> object, used in index construction
	 * and document extraction
	 */
	public static void setup(String name, String desc, boolean overWrite, DocConfig config, DocumentEnum documentEnum)
		throws BackEndException {
		documentEnum.initialize(config);
		String path = SDARTS.CONFIG_DIRECTORY + File.separator + name;

		// Build index
		System.out.println("-------------------------------------------");
		System.out.println("SDARTS COLLECTION INITIALIZATION FOR: " + name);
		System.out.println("Using edu.columbia.cs.sdarts.backend.doc.lucene engine");
		System.out.println();
		System.out.println("Building index");
		createIndex(path, config, documentEnum);
		System.out.println("Index successfully completed!");

		// Build meta-attributes
		File maf = new File(path + File.separator + DocConstants.META_ATTRIBUTES_FILENAME);
		if (!maf.exists()) {
			System.out.println("MetaAttributes file does not exist; creating default version");
			overWrite = true;
		}
		if (overWrite) {
			System.out.println("Creating metaAttributes file");
			long startMaCreate = System.currentTimeMillis();
			LSPMetaAttributes metaAttributes = createMetaAttributes(name, desc, path, config);
			long stopMaCreate = System.currentTimeMillis();
			System.out.println("Create MetaAttributes object in " + (stopMaCreate - startMaCreate) + " milliseconds");
			System.out.println("Writing MetaAttributes file to disk");
			long startMaWrite = System.currentTimeMillis();
			DocMetaAttributesBuilder dmab = new DocMetaAttributesBuilder();
			dmab.save(name, metaAttributes);
			long stopMaWrite = System.currentTimeMillis();
			System.out.println("Wrote MetaAttributes file in " + (stopMaWrite - startMaWrite) + " milliseconds");
			System.out.println("MetaAttributes successfully written!");
		}

		// Build content summary
		System.out.println("Building content summary");
		System.out.println("Analyzing index....(this may take awhile)");
		long startBuild = System.currentTimeMillis();
		LSPContentSummary cs = createContentSummary(name);
		long stopBuild = System.currentTimeMillis();
		System.out.println("Finished analysis in " + (stopBuild - startBuild) + " milliseconds");
		System.out.println("Writing content summary to disk");
		long startWrite = System.currentTimeMillis();
		DocContentSummaryBuilder dcsb = new DocContentSummaryBuilder();
		dcsb.save(name, cs);
		long stopWrite = System.currentTimeMillis();
		System.out.println("Wrote content summary to disk in " + (stopWrite - startWrite) + " milliseconds");
		System.out.println("Content summary successfully written!");
		System.out.println();
		System.out.println("Collection " + name + " successfully indexed!");
		System.out.println("-------------------------------------------");
	}

	// -------- HELPER METHODS --------
	// ---- INDEX CREATION ----
	private static void createIndex(String path, DocConfig docConfig, DocumentEnum documentEnum)
		throws BackEndException {
		try {
			String indexFilename = path + File.separator + DocConstants.INDEX_FILENAME;
			String[] stopWords = docConfig.getStopWords();
			if (stopWords == null) {
				stopWords = StopAnalyzer.ENGLISH_STOP_WORDS;
			}
			IndexWriter writer = new IndexWriter(indexFilename, new LIBAnalyzer(stopWords), true);
			int batch = 1;

			while (!documentEnum.isEmpty()) {
				System.out.println("Building batch " + batch + " of documents");
				long buildStart = System.currentTimeMillis();
				Document[] documents = documentEnum.getDocuments();
				int numDocs = documents.length;
				long buildStop = System.currentTimeMillis();
				System.out.println("Built in " + (buildStop - buildStart) + " ms.");

				System.out.println("Indexing batch " + batch + " of documents");
				long indexStart = System.currentTimeMillis();
				for (int i = 0; i < numDocs; i++) {
					if (documents[i]!=null) {
						writer.addDocument(documents[i]);
					}
				}
				long indexStop = System.currentTimeMillis();
				System.out.println("Indexed in " + (indexStop - indexStart) + " ms");
				System.out.println();
				documents = null;
				batch++;
			}

			System.out.println("Optimizing index");
			long optimizeStart = System.currentTimeMillis();
			writer.optimize();
			writer.close();
			long optimizeStop = System.currentTimeMillis();
			long optimizeTime = optimizeStop - optimizeStart;
			System.out.println("Completed optimization in " + optimizeTime + " ms");
		} catch (IOException e) {
			throw new BackEndException(e.getMessage());
		}
	}

	// ---- META-ATTRIBUTES CREATION ----
	private static LSPMetaAttributes createMetaAttributes(
		String name,
		String description,
		String path,
		DocConfig docConfig)
		throws BackEndException {
		// Build each statistic
		String source = name;
		DocFieldDescriptor[] fieldDescriptors = docConfig.getFieldDescriptors();
		int numFieldDescriptors = fieldDescriptors.length;
		LSPField[] fields = new LSPField[numFieldDescriptors + 1];
		boolean hasBody = false;
		for (int i = 0; i < numFieldDescriptors; i++) {
			String fieldName = fieldDescriptors[i].getName();
			if (fieldName.equals(FieldNames.BODY_OF_TEXT)) {
				hasBody = true;
			}
			fields[i] = new LSPField(fieldDescriptors[i].getName());
		}
		fields[numFieldDescriptors] = new LSPField(FieldNames.ANY);
		if (!hasBody) {
			LSPField bf = new LSPField(FieldNames.BODY_OF_TEXT);
			LSPField[] temp = new LSPField[numFieldDescriptors + 2];
			temp[0] = bf;
			System.arraycopy(fields, 0, temp, 1, numFieldDescriptors + 1);
			fields = temp;
		}

		int numModifiers = LuceneConstants.MODIFIERS_SUPPORTED.length;
		LSPModifier[] modifiers = new LSPModifier[numModifiers];
		for (int i = 0; i < numModifiers; i++) {
			modifiers[i] = new LSPModifier(LuceneConstants.MODIFIERS_SUPPORTED[i]);
		}
		String[] combos = LuceneConstants.FIELD_MODIFIER_COMBINATIONS;
		int comboSize = combos.length;
		LSPObject[] fieldModifierCombinations = new LSPObject[comboSize];
		for (int i = 0; i < comboSize; i++) {
			if (i % 2 == 0) {
				fieldModifierCombinations[i] = new LSPField(combos[i]);
			} else {
				fieldModifierCombinations[i] = new LSPModifier(combos[i]);
			}
		}
		String queryPartsSupported = "RF";
		String rankingAlgorithmId = "Lucene";
		String classification = "Root";
		String[] tokenizerIdList = new String[] { "com.lucene.analysis.LowerCaseTokenizer" };
		String sampleDatabaseResults = docConfig.getLinkagePrefix() + "/" + "sample.xml";
		String[] stopWordList = docConfig.getStopWords();
		if (stopWordList == null) {
			stopWordList = StopAnalyzer.ENGLISH_STOP_WORDS;
		}
		boolean turnOffStopWords = false;
			LSPMetaAttributeSet metaAttributeSet =
				new LSPMetaAttributeSet("english", name, null, // class defaults to null
	docConfig.getLinkagePrefix(),
		docConfig.getLinkagePrefix() + "/" + DocConstants.CONTENT_SUMMARY_FILENAME,
		STARTS.STANDARD_DATE_FORMAT.format(new Date(System.currentTimeMillis())),
		null,
		description,
		null,
		null);

		// Ready to create
		LSPMetaAttributes metaAttributes =
			new LSPMetaAttributes(
				source,
				fields,
				modifiers,
				fieldModifierCombinations,
				queryPartsSupported,
				LuceneConstants.SCORE_RANGE,
				rankingAlgorithmId,
				tokenizerIdList,
				sampleDatabaseResults,
				stopWordList,
				turnOffStopWords,
				metaAttributeSet,
				classification);

		return metaAttributes;
	}

	// ---- CONTENT-SUMMARY CREATION ----
	private static LSPContentSummary createContentSummary(String backEndLSPName) throws BackEndException {
		String filename =
			SDARTS.CONFIG_DIRECTORY + File.separator + backEndLSPName + File.separator + DocConstants.INDEX_FILENAME;

		try {
			IndexReader reader = IndexReader.open(filename);
			int numDocs = reader.numDocs();

			LSPContentSummary contentSummary = new LSPContentSummary(false, false, true, false, numDocs);

			TermEnum termEnum = reader.terms();
			boolean moreTerms;
			int iterations = 0;
			while ((moreTerms = termEnum.next()) != false) {
				++iterations;
				Term term = termEnum.term();
				String field = term.field();
				String value = term.text().trim();
				String termText = term.text();

				if (field.equals(FieldNames.DATE_LAST_MODIFIED)) {
					termText = STARTS.STANDARD_DATE_FORMAT.format(DateField.stringToDate(termText));
				}

				LSPField lspField = new LSPField(field);
				LSPTerm lspTerm = new LSPTerm(null, null, termText);

				int docFreq = termEnum.docFreq();
				int termFreq = getTermFreq(reader.termDocs(term));

				contentSummary.addTermDocFieldFreqInfo(lspField, lspTerm, termFreq, docFreq);
			}
			termEnum.close();
			reader.close();

			return contentSummary;
		} catch (IOException e) {
			throw new BackEndException(e.getMessage());
		}

	}

	private static int getTermFreq(TermDocs termDocs) throws IOException {
		boolean moreTermDocs = true;
		int freq = 0;
		while ((moreTermDocs = termDocs.next()) != false) {
			freq += termDocs.freq();
		}
		termDocs.close();
		return freq;
	}

	// ------------ MAIN METHOD ------------
	/**
	 * Used by the scripts performing offline indexing, such as
	 * <code>textsetup.sh</code> and <code>xmlsetup.sh</code>.
	 * Here is a list of all arguments to be passed in, and what they do:
	 * <table>
	 * <tr><td><b>Argument</b></td><td>User-entered / hidden</td><td><b>Description</b></td><tr>
	 * <tr><td><i>script name</i></td><td>hidden</td><td>Tells the main method what the name of
	 * the script is, so it can print out a nice usage string if there is an error.</td></tr>
	 * <tr><td>-name</td><td>user-entered</td><td>Signifies that next parameter is name</td></tr>
	 * <tr><td><i>backEndLSPName</i></td><td>user-entered</td><td>The name of the
	 * <code>BackEndLSP</code> that will use the collection to be indexed</td></tr>
	 * <tr><td>-sdurl or -starts-dtd-url</td><td>user-entered, optional</td><td>Indicates STARTS
	 * DTD URL is next</td></tr>
	 * <tr><td><i>starts_dtd_url</i></td><td>user-entered, optional</td><td>The URL
	 * to look for the STARTS DTD (default is http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd)</td></tr>
	 * <tr><td>-o or -overwrite-meta-attributes</td><td>user-entered, optional</td><td>If present, will
	 * cause the script to overwrite the meta-attributes file (default false, in case you wish to
	 * edit it by hand.)</td></tr>
	 * <tr><td>-hidden</td><td>hidden</td><td>Indicates more hidden parameters are coming</td></tr>
	 * <tr><td><i>doc_enum_classname</i></td><td>hidden</td><td>The fully-qualified
	 * classname of the
	 * {@link edu.columbia.cs.sdarts.backend.doc.lucene.DocumentEnum DocumentEnum} implementor
	 * to be used in building the index.</td></tr>
	 * <tr><td><i>SDARTS_HOME</i></td><td>hidden</td><td>The
	 * directory where SDARTS is installed.</td></tr>
	 * </table>
	 * @param args the parameters for the script
	 */
	public static void main(String args[]) {
		String name = null;
		String desc = null;
		boolean overWrite = false;
		String startsDTDUrl = STARTS_DTD_URL;
		String documentEnumClassname = null;
		String docConfigBuilderClassname = null;

		// ----- READ PARAMETERS -----
		int len = args.length;
		String scriptName = null;
		try {
			scriptName = args[0];
			// Basic parameter checking
			if (len == 0 || !args[1].equals("-name") || !args[len - 3].equals("-hidden")) {
				throw new Exception();
			}

			// Grab the name here
			name = args[2];

			int argIndex = 3;

			// If STARTS DTD URL present . . .
			if (isStartsDTDUrl(args[argIndex])) {
				argIndex++;
				startsDTDUrl = args[argIndex];
				argIndex++;
			}

			// Overwrite metadata?
			if (isOverWrite(args[argIndex])) {
				overWrite = true;
				argIndex++;
			}

			// More hidden stuff
			if (!args[argIndex].equals("-hidden")) {
				throw new Exception();
			}
			argIndex++;

			// Get the hidden docenum classname
			documentEnumClassname = args[argIndex];
			argIndex++;

			// Get hidden SDARTS home
			SDARTS.SDARTS_HOME = args[argIndex];
			SDARTS.CONFIG_DIRECTORY = SDARTS.SDARTS_HOME + File.separator + "config";
		} catch (Exception e) {
			System.err.println(
				"Usage: "
					+ scriptName
					+ " -name <name_of_BackEndLSP>\n"
					+ "\t[-sdurl, -starts-dtd-url <url>] \n"
					+ "\t[-o, -overwrite-meta-attributes]\n"
					+ "Where <name_of_BackEndLSP> is the name under which the BackEndLSP is registered.\n"
					+ "\tThe name must be listed in the sdarts_config.xml file.\n"
					+ "-sdurl or -starts-dtd-url is the optional URL of the STARTS DTD\n"
					+ "\t(default is http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd\n"
					+ "-o or -overwrite-meta-attributes, if present will cause the\n "
					+ "\tscript to overwrite the meta-attributes file (default false,\n"
					+ "\tin case you wish to edit it by hand.)\n");
			System.exit(1);
		}

		// Load DocConfig
		DocConfig config = null;
		try {
			DocConfigBuilder dcb = new DocConfigBuilder();
			config = DocConfigBuilder.load(name);
		} catch (Exception e) {
			System.err.println("Could not load configuration");
			e.printStackTrace();
			System.exit(1);
		}

		// Load the SDARTSConfig to get description
		SDARTSConfig sdartsConfig = null;
		try {
			sdartsConfig = SDARTSConfigBuilder.fromXML();
		} catch (Exception e) {
			System.err.println("Problem with sdarts_config.xml file!");
			System.exit(1);
		}
		BackEndLSPDescriptor[] descriptors = sdartsConfig.getBackEndLSPDescriptors();
		int len2 = descriptors.length;
		for (int i = 0; i < len2; i++) {
			BackEndLSPDescriptor descriptor = descriptors[i];
			if (descriptor.getName().equals(name)) {
				desc = descriptor.getDescription();
				break;
			}
		}
		if (desc == null) {
			System.err.println("Could not find " + name + "in sdarts_config.xml!");
			System.exit(1);
		}

		// Dynamically load DocumentEnum
		DocumentEnum documentEnum = null;
		try {
			documentEnum = (DocumentEnum) Class.forName(documentEnumClassname).newInstance();
		} catch (Exception e) {
			System.err.println("Could not load DocumentEnum");
			e.printStackTrace();
			System.exit(1);
		}

		// Prepare XML Writer
		XMLWriter.addSystemDocType(STARTS.NAMESPACE_NAME + ":smeta-attributes", startsDTDUrl);
		XMLWriter.addSystemDocType(STARTS.NAMESPACE_NAME + ":scontent-summary", startsDTDUrl);

		// Run actual setup
		try {
			setup(name, desc, overWrite, config, documentEnum);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// -------- FOR PARSING MAIN() PARAMETERS --------
	private static boolean isOverWrite(String s) {
		return (s.equals("-o") || s.equals("-overwrite-meta-attributes"));
	}

	private static boolean isStartsDTDUrl(String s) {
		return (s.equals("-sdurl") || s.equals("-starts-dtd-url"));
	}

	// -------- NO CONSTRUCTOR --------
	private LuceneSetup() {
	}

	// ------------ INNER CLASSES ------------
	// ----- Used in Lucene index construction
	private static class LIBAnalyzer extends Analyzer {
		private String[] stopWords;

		public LIBAnalyzer(String[] stopWords) {
			this.stopWords = stopWords;
		}

		public TokenStream tokenStream(Reader reader) {
			return new StopFilter(new LowerCaseTokenizer(reader), stopWords);
		}
	}
}
