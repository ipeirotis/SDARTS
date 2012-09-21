package edu.columbia.cs.sdarts.dbselection;

import java.util.Vector;

import edu.columbia.cs.sdarts.dbselection.util.Subcol;
import edu.columbia.cs.sdarts.dbselection.util.SubcolInfo;
import gnu.regexp.RE;
import gnu.regexp.REMatch;

/**
 * This class does the actual ranking score calculation.
 * Currently, only algorithm CORI is supported.
 *
 * @author jb605@cs.columbia.edu Jiangcheng Bao
 * @version 1.0
 */
public class ClientHelper
{
  private Vector indices;

  /**
   * constructor. initialize the indices to be used for calculating ranking
   * score
   *
   * @param ind the indices to be used
   */
  public ClientHelper (Vector ind) {
    this.indices = ind;
  }

  /**
   * given an input string, calculate a ranking score for it for each
   * available collection in the indices. The input string might optionally
   * include an algorithm.
   *
   * @param input the terms to calculate ranking score for
   * @return a string representation of an {@link edu.columbia.cs.sdarts.dbselection.util.SubcolInfo SubcolInfo}
   *         object, that contains the ranking score for each subcollection
   */
  public String processInput(String input) {
      // call cori, and calculate their scores
    SubcolInfo subcolInfo = new SubcolInfo();
    // if input starts with algorithm=
    // then all chars from = till first space after = is algorithm name
    if (input.startsWith("algorithm=")) {
      String algorithm = input.substring(10, input.indexOf(" ")-1);
      if ((algorithm == null) || !(algorithm.equalsIgnoreCase("cori"))) {
        return "Invalid algorithm found!";
      }
    }
    for (int i=0; i<indices.size(); i++) {
      Index index = (Index) indices.elementAt(i);
      String serverURL = index.serverURL;
      String collectionName = index.name;
      String collectionDesc = index.desc;
      double score = cori(input, collectionName);
      if (Double.isNaN(score)) {
        score = 0.0;
      }
      subcolInfo.addSubcol(new Subcol(serverURL, collectionName,
          collectionDesc, score));
    }
    return subcolInfo.toXML();
  }

  /**
   * given input terms <code>queryTerms</code>,
   * return the cori score of the terms for collection
   * <code>collectioName</code>. return average if multiple terms given.
   *
   * @param queryTerms one or more terms to be queried.
   * @param collectionName name of the collection the cori ranking score
   *                       will be returned.
   * @return the ranking score for these terms for this collection
   *         <code>collectionName</code> as a double
   */
  private double cori(String queryTerms, String collectionName) {
    String delim = " ";
    double score = 0.0;
    int count = 0;

    try {
      RE re = new RE("(\\w+)", RE.REG_ICASE);
      REMatch[] matches = re.getAllMatches(queryTerms);
      for (int i=0; i<matches.length; i++) {
        String term = matches[i].toString(1);
        // remove all starts keywords
        if (!term.equalsIgnoreCase("and") &&
            !term.equalsIgnoreCase("or") &&
            !term.equalsIgnoreCase("not")) {
          score += coriScore(term, collectionName);
          count++;
        }
      }
    } catch (gnu.regexp.REException e) {
      e.printStackTrace();
    }

    return score/(double) count;
  }

  /**
   * given a single <code>term</code>, calculate it's cori score for 
   * collection <code>collectionName</code>.
   *
   * @param term the term whose cori score is going to be returned
   * @param collectionName the name of the collection against which the
   *                       cori score is going to be calculated.
   * @return the cori score for <code>term</code> for collection
   *         <code>collectionName</code> as a double
   */
  private double coriScore(String term, String collectionName) {
    int df; // number of documents in collection containing term
            // i.e. term's doc-freq in this collection
    int cf = 0; // number of collections containing term
    int cc; // total number of collections
    int cw; // number of terms in this collection
    double acw = 0.0; // average number of cw over all collections

    df = getDocFreq(collectionName, term);
    cw = getDocCount(collectionName);
    cc = indices.size();

    for (int i=0; i<indices.size(); i++) {
      Index index = (Index) indices.elementAt(i);
      int hits = getDocFreq(index.name, term);
      if (hits > 0) {
        cf++;
      }
      int cw_i = getDocCount(index.name);
      acw += (double) cw_i;
    }

    acw /= cc;

    double t = df + 50 + 150 * ((double) cw / acw);
    t = (double) df / t;
    double p = Math.log(((double) cc + 0.5)
        / (double) cf) / Math.log((double) cc + 1.0);
    p = t * p;
    return p;
  }

  /**
   * get the doc-freq of a single <code>term</code> in collection
   * <code>collectionName</code>.
   *
   * @param collectionName the name of the collection in which the doc-freq
   *                       of the term will be returned
   * @param term the term whose doc-freq will be returned.
   * @return the doc-freq of <code>term</code> in collection
   *         <code>collectionName</code> as an integer.
   */
  public int getDocFreq(String collectionName, String term) {

    if ((collectionName == null) || (term == null)) { return 0; }

    Index index = null;

    for (int i=0; i<indices.size(); i++) {
      Index ind = (Index) indices.elementAt(i);
      if (collectionName.equals(ind.name)) {
        index = ind;
      }
    }

    return (index != null) ? index.getDocFreq(term) : 0;

  }

  /**
   * get the total document count for collection <code>collectionName</code>
   * 
   * @param collectionName the name of the collection whose document count
   *                       will be returned
   * @return the total document count of collection <code>collectionName</code>
   */
  public int getDocCount(String collectionName) {

    if (collectionName == null) { return 0; }

    Index index = null;

    for (int i=0; i<indices.size(); i++) {
      Index ind = (Index) indices.elementAt(i);
      if (collectionName.equals(ind.name)) {
        index = ind;
      }
    }

    return (index != null) ? index.getDocCount() : 0;

  }

}
