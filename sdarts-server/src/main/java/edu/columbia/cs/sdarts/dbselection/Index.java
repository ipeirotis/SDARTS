package edu.columbia.cs.sdarts.dbselection;

import java.util.HashMap;

/**
 * a wrapper class to hold a collection's server url,
 * collection's name, description and it's index
 * the index is in the form of a {@link java.util.HashMap HashMap},
 *   with the term as key, and doc_freq as value
 * 
 * @author <a href="mailto:jb605@cs.columbia.eud">Jiangcheng Bao</a>
 * @version 1.0
 */

public class Index {

  /**
   * default constructor, initialize the index HashMap
   */
  public Index () {
    this.index = new HashMap();
  }

  /**
   * constructor, initialize this index with some parameters
   *
   * @param url the sdarts server url for this collection
   * @param n   the name of this collection
   * @param d   the description of this collection
   * @param ind the index of this collection
   */
  public Index (String url, String n, String d, HashMap ind) {
    this.serverURL = url;
    this.name = n;
    this.desc = d;
    this.index = ind;
  }

  /**
   * check whether two indecies are the same. They are considered the same
   * only is they have the same server url and same name. (case-insenstive)
   *
   * @param ind  the index to be compared with this index
   * @return true on same collection
   *         false otherwise.
   */
  public boolean isSameCollection (Index ind) {
    if (ind == null) { return false; }
    return (serverURL.equalsIgnoreCase(ind.serverURL) &&
        name.equalsIgnoreCase(ind.name));
  }

  /**
   * get the document frequency of this <code>term</code> in this collection
   *
   * @param term the term whose document frequency is to be retrieved
   * @return an integer, which is the document frequency for this term in this collection
   */
  public int getDocFreq(String term) {
    int r = 0;
    //System.out.println("Index::getDocFreq(): term = " + term);
    Integer integer = (Integer) index.get(term);
    if (integer != null) {
      r = integer.intValue();
    }
    return r;
  }

  /**
   * get the total number of documents in this collection
   *
   * @return the documents count for this collection
   */
  public int getDocCount() {
    return index.size();
  }

  public String serverURL;
  public String name;
  public String desc;
  public HashMap index;
}
