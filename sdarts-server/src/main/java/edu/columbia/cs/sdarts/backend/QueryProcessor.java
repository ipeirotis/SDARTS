/**
 * This abstract class should be used by back-end developers for processing
 * queries, because it embodies the logic the STARTS specification follows.
 * <p>
 * A STARTS query consists primarily of a filter and a ranking (defined in SDARTS
 * as {@link edu.columbia.cs.sdarts.common.LSPFilter LSPFilter} and
 * {@link edu.columbia.cs.sdarts.common.LSPRanking LSPRanking}.
 * It is altogether possible that an underlying collection cannot fulfill all
 * the terms of the filter and ranking; in this case, STARTS returns that
 * <i>actual filter and ranking</i> used when the query was processed - some
 * best-effort approximation of the original.
 * <p>
 * What this class does is define a way of handling all of this. The programmer
 * should simply call the {@link edu.columbia.cs.sdarts.common.LSPQuery#query() query()} method
 * - this method automatically calls the abstract
 * {@link edu.columbia.cs.sdarts.common.LSPQuery#setActualFilter setActualFilter()} and
 * {@link edu.columbia.cs.sdarts.common.LSPQuery#setActualRanking setActualRanking()} methods.
 * These modify the
 * <code>LSPQuery</code>'s filter and ranking, accordingly. The method then calls the abstract
 * {@link edu.columbia.cs.sdarts.common.LSPQuery#validateQuery validateQuery()} method, which determines
 * whether, after this modification, the query is valid. This validity should
 * probably be tested against the collection's metaAttributes. If the query is
 * valid, the method then passes the modified <code>LSPQuery</code>
 * on to the abstract {@link edu.columbia.cs.sdarts.common.LSPQuery#queryImpl queryImpl()} method,
 * which would perform the actual querying. If not, <code>validateQuery()</code>
 * should throw an exception describing what was wrong with the query.
 * <p>
 * In this way, the developer can control the rules for what rankings and filters
 * are actually possible, modify the query before it actually gets to the
 * underlying collection, and also have the actual filter and ranking available
 * in the query for result processing.
 * <p>
 * <B>NOTE</B>: The <code>QueryProcessor</code> clones the incoming
 * <code>LSPQuery</code> before passing it on to the other methods, since these
 * methods do modify the query, and the original may be used at more than
 * one <code>BackEndLSP</code>. A clone of an
 * <code>LSPQuery</code> is <b>deep</b> for its <code>LSPFilter</code> and
 * <code>LSPRanking</code> (i.e. the clone makes copies of these), and
 * <b>shallow</b> for all other fields. A backend developer should avoid
 * changing anything in a cloned <code>LSPQuery</code> other than filter
 * and ranking.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */

package edu.columbia.cs.sdarts.backend;

import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPResults;


public abstract class QueryProcessor {
   /**
   * This default implementation clones the <code>LSPQuery</code>,
   * calls <code>setActualFilter()</code>
   * on the clone, then <code>setActualRanking()</code>
   * on the clone, then calls <code>validateQuery()</code, and then
   * finally passes the modified query to the
   * <code>queryImpl()</code> method. In general this method
   * should not be overridden, but it can be if necessary. Make sure
   * that your overridden version <b>clones the query</b>.
   * @param query the query to be processed
   * @return the results of the query
   * @exception <code>BackEndException</code> if something goes wrong,
   * or the query is not valid.
   */
  public LSPResults query (LSPQuery query) throws BackEndException {
    LSPQuery newQuery = (LSPQuery) query.clone();
    setActualFilter (newQuery);
    setActualRanking (newQuery);
    validateQuery (newQuery);
    return queryImpl (newQuery);
  }

  /**
   * An implementation of this method should look at the
   * <code>LSPFilter</code> inside the <code>LSPQuery</code>
   * and, if necessary, change it. It should
   * throw a <code>BackEndException</code> if it cannot
   * find an acceptable substitute for the
   * <code>LSPFilter</code>.
   * <p>
   * The <code>set...()</code> methods on <code>LSPFilter</code>
   * can be used for altering the filter. Alternatively,
   * the filter can simply be replaced using the
   * {@link edu.columbia.cs.sdarts.common.LSPQuery#setFilter(sdarts.common.LSPFilter) setFilter()}
   * method of <code>LSPQuery</code>.
   * <p>
   * @param query the <code>LSPQuery</code> to be checked
   * @exception <code>BackEndException</code> if a substitute
   * cannot be found.
   */
  protected abstract void setActualFilter (LSPQuery query)
    throws BackEndException;

  /**
   * An implementation of this method should look at the
   * <code>LSPFilter</code> inside the <code>LSPQuery</code>
   * and, if necessary, change it. It should
   * throw a <code>BackEndException</code> if it cannot
   * find an acceptable substitute for the
   * <code>LSPFilter</code>.
   * <p>
   * The <code>set...()</code> methods on <code>LSPRanking</code>
   * can be used for altering the ranking. Alternatively,
   * the filter can simply be replaced using the
   * {@link edu.columbia.cs.sdarts.common.LSPQuery#setRanking(sdarts.common.LSPRanking) setRanking()}
   * method of <code>LSPQuery</code>.
   * <p>
   * @param query the <code>LSPQuery</code> to be checked
   * @exception <code>BackEndException</code> if a substitute
   * cannot be found.
   */
  protected abstract void setActualRanking (LSPQuery query)
    throws BackEndException;

  /**
   * The abstract method where the query processing actually takes place.
   * It is this method, and not the <code>query()</code> method, that
   * should be overridden. This is where code talking to the actual
   * underlying collection should go.
   * @param query the query to be processed
   * @return the results of the query
   * @exception <code>BackEndException</code> if something goes wrong
   */
  protected abstract LSPResults queryImpl (LSPQuery query)
    throws BackEndException;

  /**
   * An implementor of this method should throw an exception if the
   * query is not valid, otherwise do nothing.
   * @exception BackEndException if the query is not valid
   */
  protected abstract void validateQuery (LSPQuery query) throws BackEndException;
}
