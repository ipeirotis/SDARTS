package edu.columbia.cs.sdarts.backend.doc.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Stack;

import org.omg.CORBA.IntHolder;

import com.lucene.document.DateField;
import com.lucene.document.Document;
import com.lucene.document.Field;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.doc.DocConfig;
import edu.columbia.cs.sdarts.common.FieldNames;
/**
 * An abstract class that enables the
 * {@link edu.columbia.cs.sdarts.backend.doc.lucene.LuceneSearchEngine LuceneSearchEngine}
 * to extract the <code>com.lucene.document.Documents</code> from a collection,
 * when it is constructing an index.
 * <p>
 * Due to the high memory overhead of loading, parsing, and creating Lucene
 * <code>Documents</code>, these documents are only extracted in batches.
 * The user of this class should do the following:
 * <ul>
 * <li>Instantiate and initialize, using a
 * {@link edu.columbia.cs.sdarts.backend.doc.DocConfig DocConfig}
 * <li>Keep calling the {@link #getDocuments() getDocuments()} method,
 * until the {@link #isEmpty() isEmpty()} method returns <code>true</code>.
 * </ul>
 * This class reports its progress to <code>stdout</code> as it runs.
 * <p>
 * All of the above functionality, including the batching, understanding
 * which files to access, and some postprocessing (see below) is built into
 * this class.
 * <p>
 * The abstract portion is the
 * {@link #createDocument(File, org.omg.CORBA.IntHolder) createDocument()}
 * method, which creates one Lucene <code>Document</code> from one file.
 * This portion varies depanding on the format of the underlying document.
 * This class also includes some helper methods for a developer implementing
 * <code>createDocument()</code>: the {@link #parseDate(String)} method,
 * which turns an incoming <code>String</code> into a format that Lucene
 * can understand, and the {@link #makeValue(String)} method, which will
 * replace illegal XML entities like &lt;, &gt;, and &amp; with their
 * encoded substitutes. You should use these methods frequently inside
 * an implementation of <code>createDocument()</code>.
 * <p>
 * Writing an implementation of this method is non-trivial and requires
 * knowledge of Lucene itself - visit <a href="http://www.lucene.com">the
 * Lucene web site</a> to learn how to program with Lucene.
 * <p>
 * The post-processing specifies default values for certain fields, if they
 * were not specified in the <code>DocConfig</code> used:
 * <ul>
 * <li>linkage: the linkage prefix from the <code>DocConfig</code>, plus
 * a "/", plus the filename
 * <li>title: the filename
 * <li>date-last-modified: the date last modified of the file
 * <li>body-of-text: the entire body of the file
 * </ul>

 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public abstract class DocumentEnum {
	// ------------ FIELDS ------------
	// -------- CONSTANTS --------
	/** The maximum number of documents to be returned in a batch. Right now
	 * this is fixed, but it may be dynamically determined in a later
	 * release
	 */
	public static final int BATCH_SIZE = 200;

	// -------- DATA --------
	private DocConfig docConfig;
	private int iterations = 0;
	//private File[] files;
	private Stack directoryStack;
	private Stack fileStack;
	private FileFilter fileFilter;
	private int numBatches;
	private int fileIndex;
	private int oddBatchSize;

	// ------------ METHODS ------------
	/**
	 * Create a new <code>DocumentEnum</code> and initialize it with
	 * a <code>DocConfig</code>, which tells it how to parse the documents
	 * @param docConfig the <code>DocConfig</code> with which to initialize
	 */
	public final void initialize(DocConfig docConfig) throws BackEndException {
		this.docConfig = docConfig;
		this.directoryStack = new Stack();
		this.fileStack=new Stack();

		int numFiles = countFileList(docConfig.getPaths(), docConfig.getExtensions(), docConfig.recursive());
		System.out.println("#Files:"+numFiles);

		if (numFiles == 0) {
			throw new BackEndException("no files to index");
		}

		numBatches = numFiles / BATCH_SIZE;
		if (numBatches == 0) {
			numBatches = 1;
			oddBatchSize = numFiles;
		} else {
			oddBatchSize = (numFiles % BATCH_SIZE);
			if (oddBatchSize == 0) {
				oddBatchSize = BATCH_SIZE;
			} else {
				numBatches++;
			}
		}
	}

	/**
	 * Whether the <code>DocumentBuilder</code> has run out of Lucene
	 * <code>Documents</code> to return.
	 * @return whether the <code>DocumentBuilder</code> has run out of Lucene
	 * <code>Documents</code> to return.
	 */
	public boolean isEmpty() {
		return (numBatches == 0);
	}

	/**
	 * Return the <code>DocConfig</code> used for initializing
	 * @return the <code>DocConfig</code> used for initializing
	 */
	public DocConfig getDocConfig() {
		return docConfig;
	}

	/**
	 * Load, parse, and return a batch of Lucene <code>Documents</code>
	 * from the underlying collection.
	 * @return another batch of Lucene <code>Documents</code>, or
	 * <code>null</code> if the <code>DocumentEnum</code> has run out
	 * of <code>Documents</code>.
	 * @exception BackEndException if something goes wrong
	 */
	public final Document[] getDocuments() throws BackEndException {
		if (isEmpty()) {
			return null;
		}

		int len = 0;
		if (numBatches == 1) {
			len = oddBatchSize;
		} else {
			len = BATCH_SIZE;
		}

		Document[] documents = new Document[len];
		for (int i = 0; i < len; i++) {
			//File f = files[fileIndex];
			File f = this.getFile();
			if (f==null) continue;
			IntHolder holder = new IntHolder();
			Document d= null;
			try { 
				d = createDocument(f, holder);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (d!=null) {
				documents[i] = postProcess(f, d, docConfig.getLinkagePrefix(), holder.value);
			} else {
				documents[i] = null;
			}
			System.out.println(++iterations + ": read fields for " + f.getName());
			fileIndex++;
		}

		numBatches--;

		return documents;
	}

	/**
	 * The abstract method specifying how an incoming file is actually
	 * parsed into a Lucene <code>Document</code>. An implementation of
	 * this method ought to make use of the <code>parseDate()</code>
	 * and <code>makeValue()</code> methods.
	 * @param file the <code>File</code> to turn into a Lucene
	 * <code>Document</code>
	 * @param storeTokenCountHere an OUT parameter; an implementor of this
	 * method should write the number of tokens in the file into the
	 * <code>value</code> field of this <code>IntHolder</code>
	 * @return a Lucene <code>Document</code> generated from the file
	 * @exception BackEndException if something goes wrong
	 */
	public abstract Document createDocument(File file, IntHolder storeTokenCountHere) throws BackEndException;

	/**
	 * A helper method for implementations of <code>createDocument()</code>.
	 * Turns an incoming <code>String</code> into a numerical format that
	 * Lucene can understand. This should be applied to all <code>Strings</code>
	 * found in a file being parsed that are going into date fields.
	 * @param dateString the <code>String</code> storing some kind of date
	 * @return a numerical encoding of this that Lucene can understand
	 */
	public final long parseDate(String dateString) throws BackEndException {
		SimpleDateFormat[] formats = docConfig.getDateFormats();
		long dateNum = -1;
		int numFormats = formats.length;
		for (int j = 0; j < numFormats; j++) {
			try {
				Date docDate = formats[j].parse(dateString);
				dateNum = docDate.getTime();
				break;
			} catch (ParseException e) {
				if (j == (numFormats - 1)) {
					throw new BackEndException("Could not parse date format!");
				} else {
					continue;
				}
			}
		}
		return dateNum;
	}

	/**
	 * A helper method for implementations of <code>createDocument()</code>.
	 * Will build a <code>String</code> from the incoming parameters,
	 * and will replace &lt;, &gt;, and &amp; characters with their entity names
	 * so they do not form illegal XML expressions. Apply this to nearly
	 * every <code>String</code> you read from the the file!
	 * @param val the <code>String</code> to clean up
	 * @return a <code>String</code> whose illegal XML entities have been
	 * replaced by encodings.
	 */
	public String makeValue(String val) {
		// Check to see if we need to do anything
		boolean foundGTLT = false;
		char[] illegalChars = new char[] { '<', '>', '&', '\'', '"' };
		int icLen = illegalChars.length;
		for (int i = 0; i < icLen; i++) {
			if (val.indexOf((int) illegalChars[i]) != -1) {
				foundGTLT = true;
				break;
			}
		}

		if (foundGTLT) {
			StringBuffer sb = new StringBuffer(val.length());
			int len = val.length();
			for (int i = 0; i < len; i++) {
				char c = val.charAt(i);
				if (c == '<') {
					sb.append("&lt;");
				} else if (c == '>') {
					sb.append("&gt;");
				} else if (c == '&') {
					sb.append("&amp;");
				} else if (c == '\'') {
					sb.append("&apos;");
				} else if (c == '"') {
					sb.append("&quot;");
				} else {
					sb.append(c);
				}
			}
			val = sb.toString();
		}

		return val;
	}

	// -------- HELPER METHODS --------
	private final Document postProcess(File file, Document document, String linkagePrefix, int tokenCount) throws BackEndException {
		boolean linkageSpecified = false;
		boolean titleSpecified = false;
		boolean dateSpecified = false;
		boolean bodySpecified = false;

		// Go through fields in document to find out what has not been specified
		for (Enumeration e = document.fields(); e.hasMoreElements();) {
			Field f = (Field) e.nextElement();
			if (f.name().equals(FieldNames.LINKAGE)) {
				linkageSpecified = true;
			} else if (f.name().equals(FieldNames.TITLE)) {
				titleSpecified = true;
			} else if (f.name().equals(FieldNames.DATE_LAST_MODIFIED)) {
				dateSpecified = true;
			} else if (f.name().equals(FieldNames.BODY_OF_TEXT)) {
				bodySpecified = true;
			}
		}

		// If no specified linkage, use the part of document's
		//  path that's after the path to the whole collection
		// and create an HTML link
		if (!linkageSpecified) {
			String paths[] = docConfig.getPaths();
			for (int i = 0; i < paths.length; i++) {
				if (file.getPath().startsWith(paths[i])) {
					String linkage = linkagePrefix + file.getPath().substring(paths[i].length());
					document.add(new Field(FieldNames.LINKAGE, linkage, true, true, true));
				}
			}
		}

		// If no specified title field, use linkage
		if (!titleSpecified) {
			document.add(new Field(FieldNames.TITLE, file.getName(), true, true, true));
		}

		// If no specified date, use document date
		if (!dateSpecified) {
			document.add(Field.Keyword(FieldNames.DATE_LAST_MODIFIED, DateField.timeToString(file.lastModified())));
		}

		// If no body of text, use entire text
		if (!bodySpecified) {
			try {
				BufferedReader bodyReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				document.add(Field.Text(FieldNames.BODY_OF_TEXT, bodyReader));
			} catch (IOException e) {
				throw new BackEndException(e.getMessage());
			}
		}

		// IMPORTANT STORE OTHER INFO THAT LSPDOC NEEDS
		document.add(Field.UnIndexed(LuceneConstants.LUCENE_DOC_SIZE, "" + ((int) Math.ceil((double) file.length() / (double) 1024))));
		document.add(Field.UnIndexed(LuceneConstants.LUCENE_DOC_COUNT, "" + tokenCount));

		return document;
	}

	private int countFileList(String[] paths, final String[] extensions, final boolean recursive) {
		//List files = new LinkedList();
		int files = 0;

		// Create filter if needed
		//FilenameFilter filenameFilter = null;
		this.fileFilter = null;
		if (extensions != null) {
			fileFilter = new FileFilter() {
				public boolean accept(File file) {
					if (recursive && file.isDirectory()) {
						return true;
					}
					String name = file.getName(); 
					int lastDot = name.lastIndexOf(".");
					for (int i = 0; i < extensions.length; i++) {
						if (lastDot == -1) {
							if (extensions[i].equals(".")) {
								return true;
							}
						} else if (name.substring(lastDot).equals(extensions[i])) {
							return true;
						}
					}
					return false;
				}
			};
		} else {
			fileFilter = new FileFilter() {
				public boolean accept(File f) {
					return true;
				}
			};
		}

		// Iterate thru paths
		int numPaths = paths.length;
		//System.err.println("P:"+numPaths);
				
		int depth=1;
		for (int i = 0; i < numPaths; i++) {
			
			File f = new File(paths[i]);
			if (!f.exists())  {
				System.err.println("Cannot find path:"+paths[i]);
				continue;
			}
			if (f.isDirectory()) {
				File[] qualifyingFiles = f.listFiles(this.fileFilter);
				files += CountFiles(qualifyingFiles, this.fileFilter, recursive, depth);
				this.directoryStack.push(f);
			} else {
				if (this.fileFilter.accept(f)) {
					files++;
				} else {
					//System.out.println("File:"+f.getName()+" does not qualify");
				}
			}
		}

		return files;

	}

	private File getFile() {
		
		// First we try to get something from
		// the list of available files, but if
		// the FileStack is empty, we will fill it
		// with files from the entries in DirectoryStack
		while (this.fileStack.isEmpty()) {
			if (!this.directoryStack.isEmpty()) {  
				File dir = (File)directoryStack.pop();
				File[] files = dir.listFiles(fileFilter);
				for (int i=0; i<files.length; i++) {
					if (files[i].isFile()) {
						this.fileStack.push(files[i]);
					}
				}
			} else {
				System.err.println("PANOS, 7/19/2003: This is an error!");
				return null;
			}
		}
		
		File f = (File)fileStack.pop();
		return f;
		
	}

	private int CountFiles(File[] files, FileFilter fileFilter, boolean recursive, int depth) {
		int numFiles = 0;
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory() && recursive) {
				System.out.print("Directory:"+f.getName());
				this.directoryStack.push(f);
				int n = CountFiles(f.listFiles(fileFilter), fileFilter, recursive, depth+1);
				numFiles += n;
				System.out.println(" has "+n+" files.");
			} else if (!f.isDirectory() && f.exists()) {
				numFiles++;
			}
		}
		
		return numFiles;
		
	}

//	private List bfImpl(File[] files, FileFilter fileFilter, boolean recursive) {
//		List fileList = new LinkedList();
//		int numFiles = files.length;
//		if (numFiles == 0) {
//			return null;
//		}
//		for (int i = 0; i < numFiles; i++) {
//			File f = files[i];
//			if (f.isDirectory() && recursive) {
//				List tmpList = bfImpl(f.listFiles(fileFilter), fileFilter, recursive);
//				if (tmpList != null) {
//					fileList.addAll(tmpList);
//				}
//			} else {
//				fileList.add(f);
//			}
//		}
//		return fileList;
//	}
}
