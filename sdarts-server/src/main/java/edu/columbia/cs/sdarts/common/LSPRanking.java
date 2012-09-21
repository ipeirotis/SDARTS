

package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;



/**
 * Represents a "ranking", as defined in STARTS 1.0: one of possibly
 * several criteria for ranking results. A ranking is used to calculate
 * a score for each document.
 * <p>
 * Sometimes, a back-end developer
 * need never instantiate an <code>LSPRanking</code>, but simply read
 * one that has been passed from the front end inside an
 * {@link edu.columbia.cs.sdarts.common.LSPQuery LSPQuery}. The
 * {@link edu.columbia.cs.sdarts.common.LSPResults LSPResults}
 * object that the back end returns must include the actual ranking used.
 * <p>
 * In the case where the wrapped collection does not fully support the
 * requested ranking, the back end developer must instantiate a new
 * ranking that represents the supported subset of the requested filter,
 * and include it in the <code>LSPResults</code> object. See
 * {@link edu.columbia.cs.sdarts.backend.QueryProcessor} and
 * {@link edu.columbia.cs.sdarts.backend.StandardQueryProcessor} for help in doing this.
 * An <code>LSPRanking</code> can be altered using the various
 * <code>set()</code> methods described below. In addition, an
 * <code>LSPRanking</code> is <code>Cloneable</code>; in this way, a
 * parent <code>LSPQuery</code> can be cloned, and the filter in the clone
 * can be manipulated directly using the <code>set()</code> methods.
 * An <code>LSPRanking</code> makes only a shallow clone.
 * <p>
 * A ranking can be of three types:<br>
 * <ul>
 * <li><code>TERMS</code>: one or more {@link edu.columbia.cs.sdarts.common.LSPTerm LSPTerms}
 * <li><code>TERM_PROXOP_TERM</code>: an <code>LSPTerm</code>, followed by an
 * {@link edu.columbia.cs.sdarts.common.LSPProxOp LSPProxOp}, followed by an
 * <code>LSPTerm</code>.
 * <li><code>RANKING_BOOLEANOP_RANKING</code>: an <code>LSPRanking</code>,
 * followed by an {@link edu.columbia.cs.sdarts.common.LSPBooleanOp LSPBooleanOp},
 * followed by an <code>LSPRanking</code>.
 * </ul>
 * Accessors for the type, and for all possible elements within the
 * ranking, are included in this class.<p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPRanking extends LSPObject {
    // ------------ FIELDS ------------
    public static final int TERMS = 0;
    public static final int TERM_PROXOP_TERM = 1;
    public static final int RANKING_BOOLEANOP_RANKING = 2;
    private int type = TERMS;

    private LSPTerm[] terms;

    private LSPTerm term1;
    private LSPProxOp proxOp;
    private LSPTerm term2;

    private LSPRanking ranking1;
    private LSPBooleanOp booleanOp;
    private LSPRanking ranking2;


    // ------------ METHODS ------------
    // -------- CONSTRUCTORS --------
    /**
     * Creates an instance of type <code>TERMS</code>
     * @param terms the terms in the ranking
     */
    public LSPRanking (LSPTerm[] terms) {
      set (terms);
    }

    /**
     * Creates an instance of type <code>TERM_PROXOP_TERM</code>
     * @param term1 the first term
     * @param proxOp the proximity operator
     * @param term2 the second term
     */
    public LSPRanking (LSPTerm term1, LSPProxOp proxOp, LSPTerm term2) {
      set (term1, proxOp, term2);
    }

    /**
     * Creates an instance of type <code>RANKING_BOOLEANOP_RANKING</code>
     * @param ranking1 the first ranking
     * @param booleanOp the boolean operator
     * @param ranking2 the second ranking
     */
    public LSPRanking (LSPRanking ranking1, LSPBooleanOp booleanOp,
                       LSPRanking ranking2) {
      set (ranking1, booleanOp, ranking2);
    }

    // -------- ACCESSORS --------
    /**
     * Return the type of ranking this is: either <code>TERMS</code>,
     * <code>TERM_PROXOP_TERM</code>, or <code>RANKING_BOOLEANOP_RANKING</code>.
     * @return the type of ranking this is: either <code>TERMS</code>,
     * <code>TERM_PROXOP_TERM</code>, or <code>RANKING_BOOLEANOP_RANKING</code>.
     */
    public int getType () {
      return type;
    }

    /**
     * Return the <code>LSPTerms</code> associated with this ranking,
     * if type is <code>TERMS</code>. Else, returns <code>null</code>.
     * @return the <code>LSPTerms</code> associated with this ranking,
     * if type is <code>TERMS</code>. Else, returns <code>null</code>.
     */
    public LSPTerm[] getTerms () {
      return terms;
    }


    /**
     * If type is <code>TERM_PROXOP_TERM</code>,
     * will return the first LSPTerm. Else, returns <code>null</code>.
     * @return If type is <code>TERM_PROXOP_TERM</code>,
     * will return the first LSPTerm. Else, returns <code>null</code>.
     */
    public LSPTerm getFirstTerm () {
      return term1;
    }

    /**
     * Return the <code>LSPProxOp</code> associated with this ranking,
     * if type is <code>TERM_PROXOP_TERM</code>. Else, returns
     * <code>null</code>.
     * @return the <code>LSPProxOp</code> associated with this ranking,
     * if type is <code>TERM_PROXOP_TERM</code>. Else, returns
     * <code>null</code>.
     */
    public LSPProxOp getProxOp () {
      return proxOp;
    }


    /**
     * Return the second <code>LSPTerm</code> associated with this ranking,
     * if type is <code>TERM_PROXOP_TERM</code>. Else, returns
     * <code>null</code>.
     * @return the second <code>LSPTerm</code> associated with this ranking,
     * if type is <code>TERM_PROXOP_TERM</code>. Else, returns
     * <code>null</code>.
     */
    public LSPTerm getSecondTerm () {
      return term2;
    }

    /**
     * Return the first <code>LSPRanking</code> associated with this ranking,
     * if type is <code>RANKING_BOOLEANOP_RANKING</code>. Else, returns
     * <code>null</code>.
     * @return the first <code>LSPRanking</code> associated with this ranking,
     * if type is <code>RANKING_BOOLEANOP_RANKING</code>. Else, returns
     * <code>null</code>.
     */
    public LSPRanking getFirstRanking () {
      return ranking1;
    }

    /**
     * Return the <code>LSPBooleanOp</code> associated with this ranking,
     * if type is <code>RANKING_BOOLEANOP_RANKING</code>. Else, returns
     * <code>null</code>.
     * @return the <code>LSPBooleanOp</code> associated with this ranking,
     * if type is <code>RANKING_BOOLEANOP_RANKING</code>. Else, returns
     * <code>null</code>.
     */
    public LSPBooleanOp getBooleanOp () {
      return booleanOp;
    }

    /**
     * Return the second <code>LSPRanking</code> associated with this ranking,
     * if type is <code>RANKING_BOOLEANOP_RANKING</code>. Else, returns
     * <code>null</code>.
     * @return the second <code>LSPRanking</code> associated with this ranking,
     * if type is <code>RANKING_BOOLEANOP_RANKING</code>. Else, returns
     * <code>null</code>.
     */
    public LSPRanking getSecondRanking () {
      return ranking2;
    }

    /**
     * Convert the <code>LSPRanking</code> into a ranking of type
     * <code>TERMS</code>
     * @param termd the termd in the ranking
     */
    public void set (LSPTerm[] terms) {
      if (terms == null) {
        throw new IllegalArgumentException ("null parameter");
      }
      this.terms = terms;
      this.type = TERMS;
      term1 = null;
      term2 = null;
      proxOp = null;
      booleanOp = null;
      ranking1 = null;
      ranking2 = null;
    }

    /**
     * Convert the <code>LSPRanking</code> into a ranking of type
     * <code>TERM_PROXOP_TERM</code>
     * @param term1 the first term
     * @param proxOp the proximity operator
     * @param term2 the second term
     */
    public void set (LSPTerm term1, LSPProxOp proxOp, LSPTerm term2) {
      if (term1 == null || proxOp == null || term2 == null) {
        throw new IllegalArgumentException ("null parameter");
      }
      this.term1 = term1;
      this.proxOp = proxOp;
      this.term2 = term2;
      this.type = TERM_PROXOP_TERM;
      terms = null;
      booleanOp = null;
      ranking1 = null;
      ranking2 = null;
    }

    /**
     * Convert the <code>LSPRanking</code> into a ranking of type
     * <code>RANKING_BOOLEANOP_RANKING</code>
     * @param ranking1 the first ranking
     * @param booleanOp the boolean operator
     * @param ranking2 the second ranking
     */
    public void set (LSPRanking ranking1, LSPBooleanOp booleanOp,
                     LSPRanking ranking2 ) {
      if (ranking1 == null || booleanOp == null || ranking2 == null) {
        throw new IllegalArgumentException ("null parameter");
      }
      this.ranking1 = ranking1;
      this.booleanOp = booleanOp;
      this.ranking2 = ranking2;
      this.type = RANKING_BOOLEANOP_RANKING;
      terms = null;
      term1 = null;
      proxOp = null;
      term2 = null;
    }

    public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      writer.printStartElement ("ranking");
      writer.indent();
      switch (type) {
        case TERMS:
          int len = terms.length;
          for (int i = 0 ; i < len ; i++) {
            terms[i].toXML (writer);
          }
        break;

        case TERM_PROXOP_TERM:
          term1.toXML (writer);
          proxOp.toXML (writer);
          term2.toXML (writer);
        break;

        case RANKING_BOOLEANOP_RANKING:
          ranking1.toXML (writer);
          booleanOp.toXML (writer);
          ranking2.toXML (writer);
        break;
      }
      writer.unindent();
      writer.printEndElement ("ranking");
      writer.exitNamespace();
    }

    public Object clone () {
      LSPRanking newRanking = null;
      switch (type) {
        case TERMS:
        newRanking = new LSPRanking (terms);
        break;

        case TERM_PROXOP_TERM:
        newRanking = new LSPRanking (term1, proxOp, term2);
        break;

        case RANKING_BOOLEANOP_RANKING:
        newRanking = new LSPRanking (ranking1, booleanOp, ranking2);
        break;
      }

      return newRanking;
    }
}
