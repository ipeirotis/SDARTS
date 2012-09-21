
package edu.columbia.cs.sdarts.common;

import java.io.Reader;

import org.xml.sax.AttributeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.util.SDARTSHandlerBase;
import edu.columbia.cs.sdarts.util.UnsynchStack;
import edu.columbia.cs.sdarts.frontend.SDARTSConfigBuilder;
import edu.columbia.cs.sdarts.frontend.SDARTSConfig;
import edu.columbia.cs.sdarts.common.STARTS;
/**
 * Creates an {@link edu.columbia.cs.sdarts.common.LSPMetaAttributes LSPMetaAttributes}.
 * Its concrete method specifies how to read in STARTS XML to create
 * the meta-attributes, while its abstract methods specify how a
 * meta-attributes ought to be saved or loaded.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public abstract class LSPMetaAttributesBuilder {
  /**
   * An implementation of this method should load an
   * <code>LSPMetaAttributes</code> from some kind of persistent storage
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} to whom the
   * <code>LSPMetaAttributes</code> belongs
   * @return an <code>LSPMetaAttributes</code> that has been loaded
   */
  public abstract LSPMetaAttributes load (String backEndLSPName)
    throws BackEndException;

  /**
   * An implementation of this method should save an
   * <code>LSPMetaAttributes</code> to some kind of persistent storage
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} to whom the
   * <code>LSPMetaAttributes</code> belongs
   * @param metaAttributes the <code>LSPMetaAttributes</code> to be saved
   */
  public abstract void save (String backEndLSPName, LSPMetaAttributes metaAttributes)
    throws BackEndException;

  /**
   * Will create an <code>LSPMetaAttributes</code> by reading in STARTS
   * XML from the specified <code>Reader</code>
   * @param reader the <code>Reader</code> from which to read the STARTS
   * XML to build to <code>LSPMetaAttributes</code>
   */
  public LSPMetaAttributes fromXML (Reader reader) throws BackEndException {
    try {
      LMABHandler handler = new LMABHandler ();
      LSPMetaAttributes attribs = handler.parse (reader);
      handler = null;
      return attribs;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new BackEndException (e.getMessage());
    }
  }

  private class LMABHandler extends SDARTSHandlerBase {
    // ------------ FIELDS ------------
    // -------- CONSTANTS --------
    // -------- PARSING --------
    private int     state;
    private boolean inFMCombo;
    private static final int NONE                         = -1;
    private static final int SMETA_ATTRIBUTES             = 0;
    private static final int SOURCE                       = 1;
    private static final int FIELDS_SUPPORTED             = 2;
    private static final int FIELD                        = 3;
    private static final int MODIFIERS_SUPPORTED          = 4;
    private static final int MODIFIER                     = 5;
    private static final int FIELD_MODIFIER_COMBINATIONS  = 6;
    private static final int QUERY_PARTS_SUPPORTED        = 7;
    private static final int SCORE_RANGE                  = 8;
    private static final int RANKING_ALGORITHM_ID         = 9;
    private static final int TOKENIZER_ID_LIST            = 10;
    private static final int TOKENIZER                    = 11;
    private static final int SAMPLE_DATABASE_RESULTS      = 12;
    private static final int STOP_WORD_LIST               = 13;
    private static final int TURNOFF_STOP_WORDS           = 14;
    private static final int WORD                         = 15;
    private static final int META_ATTRIBUTE_SET           = 16;
    private static final int CLASS_T                      = 17;
    private static final int LINKAGE                      = 18;
    private static final int CONTENT_SUMMARY_LINKAGE      = 19;
    private static final int SOURCE_LANGUAGE              = 20;
    private static final int SOURCE_NAME                  = 21;
    private static final int DATE_CHANGED                 = 22;
    private static final int DATE_EXPIRES                 = 23;
    private static final int ABSTRACT                     = 24;
    private static final int ACCESS_CONSTANTS             = 25;
    private static final int CONTACT                      = 26;
	private static final int CLASSIFICATION				  = 27;

    private LSPMetaAttributes metaAttributes;
    private String source;
    private UnsynchStack fieldsSupported; // LSPField[]
    private UnsynchStack modifiersSupported; // LSPModifier[]
    private UnsynchStack fieldModifierCombinations; //LSPField,LSPModifier
    private String queryPartsSupported;
    private double lowestScore = -1;
    private double highestScore = -1;
    private String rankingAlgorithmId;
    private UnsynchStack tokenizerIdList; // String[]
    private String sampleDatabaseResults;
    private UnsynchStack stopWordList; // String[]
    private boolean turnOffStopWords;
    private LSPMetaAttributeSet metaAttributeSet;
    private String class_t;
    private String linkage;
    private String contentSummaryLinkage;
    private String sourceLanguage;
    private String sourceName;
    private String dateChanged;
    private String dateExpires;
    private String abstrct;
    private String accessConstants;
    private String contact;
	private String classification;



    // ------------ METHODS ------------
    public LMABHandler () throws SAXException {
      super ();

      fieldsSupported = new UnsynchStack();
      modifiersSupported = new UnsynchStack();
      fieldModifierCombinations = new UnsynchStack();
      tokenizerIdList = new UnsynchStack();
      stopWordList = new UnsynchStack();
    }

    public LSPMetaAttributes parse (Reader reader) throws Exception 
    {
		SDARTSConfig config = SDARTSConfigBuilder.fromXML();

		super.EnableSchemaChecking(true);
		super.EnableValidation(true);
		super.setSchemaLocation(STARTS.NAMESPACE_VALUE, config.getStartsDtdURL());
		parser.parse (new InputSource (reader));
		return metaAttributes;
    }

    public void startElement (String name, AttributeList attrs)
	throws SAXException {
        name = super.removeNamespace(name);
        if (name.equals ("smeta-attributes")) {
          state = SMETA_ATTRIBUTES;
        }
        else if (name.equals ("source")) {
          state = SOURCE;
        }
        else if (name.equals ("fields-supported")) {
          state = FIELDS_SUPPORTED;
        }
        else if (name.equals ("field")) {
          state = FIELD;
          String fieldName = attrs.getValue ("name");
          LSPField field = new LSPField (fieldName);
          if (inFMCombo) {
            fieldModifierCombinations.push (field);
          }
          else {
            fieldsSupported.push (field);
          }
        }
        else if (name.equals ("modifiers-supported")) {
          state = MODIFIERS_SUPPORTED;
        }
        else if (name.equals ("modifier")) {
          state = MODIFIER;
          String modifierName = attrs.getValue ("name");
          LSPModifier modifier = new LSPModifier (modifierName);
          if (inFMCombo) {
            fieldModifierCombinations.push (modifier);
          }
          else {
            modifiersSupported.push (modifier);
          }
        }
        else if (name.equals ("field-modifier-combinations")) {
          state = FIELD_MODIFIER_COMBINATIONS;
          inFMCombo = true;
        }
        else if (name.equals ("query-parts-supported")) {
          state = QUERY_PARTS_SUPPORTED;
          queryPartsSupported = attrs.getValue("parts");
          if (queryPartsSupported == null) {
            queryPartsSupported = "RF";
          }
        }
        else if (name.equals ("score-range")) {
          state = SCORE_RANGE;
          lowestScore = new Double (attrs.getValue ("lower")).doubleValue();
          highestScore = new Double (attrs.getValue ("upper")).doubleValue();
        }
        else if (name.equals ("ranking-algorithm-id")) {
          state = RANKING_ALGORITHM_ID;
        }
        else if (name.equals ("tokenizer-id-list")) {
          state = TOKENIZER_ID_LIST;
        }
        else if (name.equals ("tokenizer")) {
          state = TOKENIZER;
        }
        else if (name.equals ("sample-database-results")) {
          state = SAMPLE_DATABASE_RESULTS;
        }
        else if (name.equals ("stop-word-list")) {
          state = STOP_WORD_LIST;
        }
        else if (name.equals ("word")) {
          state = WORD;
        }
        else if (name.equals ("turnoff-stop-words")) {
          state = TURNOFF_STOP_WORDS;
          turnOffStopWords =
            new Boolean (attrs.getValue ("value")).booleanValue();
        }
        else if (name.equals ("meta-attribute-set")) {
          state = META_ATTRIBUTE_SET;
        }
        else if (name.equals ("class")) {
          state = CLASS_T;
        }
        else if (name.equals ("linkage")) {
          state = LINKAGE;
        }
		else if (name.equals ("classification")) {
		  state = CLASSIFICATION;
		}
        else if (name.equals ("content-summary-linkage")) {
          state = CONTENT_SUMMARY_LINKAGE;
        }
        else if (name.equals ("source-language")) {
          state = SOURCE_LANGUAGE;
        }
        else if (name.equals ("source-name")) {
          state = SOURCE_NAME;
        }
        else if (name.equals ("date-changed")) {
          state = DATE_CHANGED;
        }
        else if (name.equals ("date-expires")) {
          state = DATE_EXPIRES;
        }
        else if (name.equals ("abstract")) {
          state = ABSTRACT;
        }
        else if (name.equals ("access-constants")) {
          state = ACCESS_CONSTANTS;
        }
        else if (name.equals ("contact")) {
          state = CONTACT;
        }
        else {
          processException ("unknown element: " + name);
        }
    }

    public void characters (char[] ch, int start, int length)
	throws SAXException {
        String value = new String (ch, start, length);
        switch (state) {
          case SMETA_ATTRIBUTES:
          case FIELDS_SUPPORTED:
          case FIELD:
          case MODIFIERS_SUPPORTED:
          case MODIFIER:
          case FIELD_MODIFIER_COMBINATIONS:
          case SCORE_RANGE:
          case STOP_WORD_LIST:
          case TURNOFF_STOP_WORDS:
          case META_ATTRIBUTE_SET:
          case TOKENIZER_ID_LIST:
            //processException ("illegal value: " + value);
          break;

          case SOURCE:
            source = value;
          break;

          case QUERY_PARTS_SUPPORTED:
            queryPartsSupported = value;
          break;

          case RANKING_ALGORITHM_ID:
            rankingAlgorithmId = value;
          break;

          case TOKENIZER:
            tokenizerIdList.push (value);
          break;

          case SAMPLE_DATABASE_RESULTS:
            sampleDatabaseResults = value;
          break;

          case WORD:
            stopWordList.push (value);
          break;

          case CLASS_T:
            class_t = value;
          break;

          case LINKAGE:
            linkage = value;
          break;
          
          case CLASSIFICATION:
          	classification = value;
          break;

          case CONTENT_SUMMARY_LINKAGE:
            contentSummaryLinkage = value;
          break;

          case SOURCE_LANGUAGE:
            sourceLanguage = value;
          break;

          case SOURCE_NAME:
            sourceName = value;
          break;

          case DATE_CHANGED:
            dateChanged = value;
          break;

          case DATE_EXPIRES:
            dateExpires = value;
          break;

          case ABSTRACT:
            abstrct = value;
          break;

          case ACCESS_CONSTANTS:
            accessConstants = value;
          break;

          case CONTACT:
            contact = value;
          break;
        }
    }

    public void endElement (String name) throws SAXException {
      switch (state) {
        case SMETA_ATTRIBUTES:
          metaAttributes = new LSPMetaAttributes (
            source,
            (LSPField[]) fieldsSupported.toArray (new LSPField [0]),
            (LSPModifier[]) modifiersSupported.toArray (new LSPModifier[0]),
            (LSPObject[]) fieldModifierCombinations.toArray(new LSPObject[0]),
            queryPartsSupported,
            new double[] {lowestScore, highestScore},
            rankingAlgorithmId,
            (String[]) tokenizerIdList.toArray (new String [0]),
            sampleDatabaseResults,
            (String[]) stopWordList.toArray (new String [0]),
            turnOffStopWords,
            metaAttributeSet,
            classification
          );
        break;

        case SOURCE:
        case CLASSIFICATION:
        case FIELDS_SUPPORTED:
        case MODIFIERS_SUPPORTED:
        case QUERY_PARTS_SUPPORTED:
        case SCORE_RANGE:
        case RANKING_ALGORITHM_ID:
        case TOKENIZER_ID_LIST:
        case SAMPLE_DATABASE_RESULTS:
        case STOP_WORD_LIST:
        case TURNOFF_STOP_WORDS:
          state = SMETA_ATTRIBUTES;
        break;

        case WORD:
          state = STOP_WORD_LIST;
        break;

        case FIELD:
          if (inFMCombo) {
            state = FIELD_MODIFIER_COMBINATIONS;
          }
          else {
            state = FIELDS_SUPPORTED;
          }
        break;

        case MODIFIER:
          if (inFMCombo) {
            state = FIELD_MODIFIER_COMBINATIONS;
          }
          else {
            state = MODIFIERS_SUPPORTED;
          }
        break;

        case TOKENIZER:
          state = TOKENIZER_ID_LIST;
        break;

        case FIELD_MODIFIER_COMBINATIONS:
          state = SMETA_ATTRIBUTES;
          inFMCombo = false;
        break;

        case META_ATTRIBUTE_SET:
          state = SMETA_ATTRIBUTES;
          metaAttributeSet = new LSPMetaAttributeSet (
            sourceLanguage, sourceName, class_t, linkage,
            contentSummaryLinkage, dateChanged,
            dateExpires, abstrct, accessConstants,
            contact
          );
        break;

        case CLASS_T:
        case LINKAGE:
        case CONTENT_SUMMARY_LINKAGE:
        case SOURCE_LANGUAGE:
        case SOURCE_NAME:
        case DATE_CHANGED:
        case DATE_EXPIRES:
        case ABSTRACT:
        case ACCESS_CONSTANTS:
        case CONTACT:
          state = META_ATTRIBUTE_SET;
        break;
      }
    }
  }
}
