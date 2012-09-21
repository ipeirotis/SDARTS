
package edu.columbia.cs.sdarts.backend.impls.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.omg.CORBA.IntHolder;

import com.lucene.document.DateField;
import com.lucene.document.Document;
import com.lucene.document.Field;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.doc.DocConfig;
import edu.columbia.cs.sdarts.backend.doc.DocFieldDescriptor;
import edu.columbia.cs.sdarts.backend.doc.lucene.DocumentEnum;
import edu.columbia.cs.sdarts.common.FieldNames;
import gnu.regexp.RE;
import gnu.regexp.REMatch;


/**
 * An implementation of {@link edu.columbia.cs.sdarts.backend.doc.lucene.DocumentEnum}
 * used to parse plain text files for indexing by the Lucene search
 * engine. There is no need to instantiate this class; it is automatically
 * used by the {@link edu.columbia.cs.sdarts.backend.impls.text.TextBackEndLSP TextBackEndLSP}.
 * Basically, it uses
 * {@link edu.columbia.cs.sdarts.backend.doc.DocFieldDescriptor DocFieldDescriptors}
 * and the regular expressions contained within them to extract fields from
 * a file.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public final class TextDocumentEnum extends DocumentEnum {

  /**
   * Builds a Lucene <code>Document</code> from a plain text file
   */
  public Document createDocument
      (File f, IntHolder storeTokenCountHere)
    throws BackEndException {
      // Set up general objects
      DocConfig docConfig = getDocConfig();
      Document document = new Document();
      int len = 0;
      StringBuffer buff = new StringBuffer();

      // Read entire document into buffer
      StringBuffer docBuffer = new StringBuffer();
	  BufferedReader br = null;
      int tokenCount = 0;
      try {
        br = new BufferedReader (
          new InputStreamReader (
            new FileInputStream (f)));
        String line = null;
        while ( (line = br.readLine()) != null ) {
          docBuffer.append (line);
          docBuffer.append ("\r\n");
          tokenCount += new StringTokenizer (line, " ").countTokens();
        }
        br.close();
      }
      catch (Exception e) {
        throw new BackEndException (e.getMessage());
      }
      storeTokenCountHere.value = tokenCount;

      // Build fields
      DocFieldDescriptor[] fieldDescriptors =
        docConfig.getFieldDescriptors();
      if (fieldDescriptors != null) {
        len = fieldDescriptors.length;
        for (int i = 0 ; i < len ; i++) {
          DocFieldDescriptor fd = fieldDescriptors[i];
          String  fieldName = fd.getName();
//          int     fieldType = fd.getCode();
		  String  fieldType = fd.getName();
          boolean skipStart = fd.skipStart();
          boolean skipEnd   = fd.skipEnd();

          RE start = fd.getStart();
          RE end   = fd.getEnd();
          REMatch startMatch = start.getMatch (docBuffer);
	
          if (startMatch == null) {
            continue;
          }

          int sEnd   = -1;
          int eStart = -1;
          int eEnd   = -1;
          sEnd   = startMatch.getEndIndex();

          boolean isDate = fd.isDate();
          long dateNum = -1;

          if (!isDate && !skipStart) {
            buff.append (startMatch);
          }

          REMatch endMatch = end.getMatch (docBuffer, sEnd);
          if (endMatch == null) {
            throw new TextParseException ("Missing end match", f, fd.getName(),
                                          start, end);
          }
          
          eStart = endMatch.getStartIndex();
          eEnd   = endMatch.getEndIndex();

          if (isDate) {
            String dateString = docBuffer.substring (sEnd,eStart).trim();
            dateNum = parseDate (dateString);
            document.add (Field.Keyword (fieldName, DateField.timeToString(dateNum)));
          }
          else {
            buff.append (docBuffer.substring (sEnd, eStart));
            if (!skipEnd) {
              buff.append (endMatch);
            }
            if (fieldType == FieldNames.LINKAGE) {
              buff.insert(0, docConfig.getLinkagePrefix());
            }

            // Postprocess text get rid of <, >
            String val = makeValue (buff.toString());
            document.add (new Field (fieldName, val, true, true, true));
          }
          buff.setLength(0);
        }
      }
    return document;
  }
}
