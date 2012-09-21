package edu.columbia.cs.sdarts.backend.doc.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.IndexReader;
import com.lucene.index.Term;
import com.lucene.index.TermDocs;
import com.lucene.search.BooleanQuery;
import com.lucene.search.Hits;
import com.lucene.search.IndexSearcher;
import com.lucene.search.PhraseQuery;
import com.lucene.search.Query;
import com.lucene.search.TermQuery;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.StandardQueryProcessor;
import edu.columbia.cs.sdarts.backend.doc.DocConstants;
import edu.columbia.cs.sdarts.common.FieldNames;
import edu.columbia.cs.sdarts.common.LSPBooleanOp;
import edu.columbia.cs.sdarts.common.LSPDoc;
import edu.columbia.cs.sdarts.common.LSPField;
import edu.columbia.cs.sdarts.common.LSPFilter;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPObject;
import edu.columbia.cs.sdarts.common.LSPProxOp;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPRanking;
import edu.columbia.cs.sdarts.common.LSPResults;
import edu.columbia.cs.sdarts.common.LSPSortByField;
import edu.columbia.cs.sdarts.common.LSPSource;
import edu.columbia.cs.sdarts.common.LSPTerm;
import edu.columbia.cs.sdarts.frontend.SDARTS;
//import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Processes queries, modifying the filter and ranking to support
 * only the operations Lucene can handle. For now, Lucene's limitations
 * in terms of filters and rankings are:
 * <ul>
 * <li>The supported field/modifier combinations in
 * {@link edu.columbia.cs.sdarts.backend.doc.lucene.LuceneConstants LuceneConstants}
 * <li>The {@link edu.columbia.cs.sdarts.common.LSPRanking LSPRanking} can only be of
 * type <code>LSPRanking.TERMS</code> and can only contain terms that
 * are also in the {@link edu.columbia.cs.sdarts.common.LSPFilter LSPFilter}
 * <li>Date queries are not working yet
 * <li>If the filter is <code>null</code>, it is replaced by the ranking
 * <li>The "termfreq" portion of the term-stats-list (see the STARTS spec)
 * is not working yet, and is always 0.
 * <li>No stemming (coming soon)
 * <li>The terms in "term-stats-list" are from the filter only
 * </ul>
 * In addition, there are the following limits and defaults on the "answer-fields"
 * and "sort-by-fields" portions of the query:
 * <ul>
 * <li>If "answer-fields" is unspecified, all "fields-supported" from the
 * meta-attributes will be in the answer
 * <li>Lucene can search on "body-of-text", but cannot return it as
 * an "answer-field", due to space limitations in the index
 * <li>All "sort-by-fields" must also be in the "answer-fields" list
 * </ul>
 * See the SDARTS Design Document for more information.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public class LuceneQueryProcessor extends StandardQueryProcessor {
	// ------------ FIELDS ------------
	private LSPSource source;
	private String indexFilename;
	private LSPMetaAttributes metaAttributes;
	private Map termWeights;

	// ------------ METHODS ------------
	// -------- CONSTRUCTOR --------
	/**
	 * Create query processor
	 * @param sourceName just the name of the <code>BackEndLSP</code>
	 * @param metaAttributes the collection's meta-attributes, which help
	 * with determining supported fields.
	 */
	public LuceneQueryProcessor(String sourceName, LSPMetaAttributes metaAttributes) {
		super(metaAttributes);
		this.indexFilename = SDARTS.CONFIG_DIRECTORY + File.separator + sourceName + File.separator + DocConstants.INDEX_FILENAME;
		source = new LSPSource(sourceName);
		this.metaAttributes = metaAttributes;
		termWeights = new HashMap();
	}

	// -------- QUERY METHODS --------
	/**
	 * Query the underlying collection, using the Lucene search engine.
	 * Do not call this method directly! Use the regular <code>query()</code>
	 * method of the {@link edu.columbia.cs.sdarts.backend.QueryProcessor QueryProcessor}
	 * suprtclass.
	 * @param query the query
	 * @return the results
	 * @exception BackEndException if something goes wrong
	 */
	public LSPResults queryImpl(LSPQuery query) throws BackEndException {
		LSPResults results = null;
		//int maxDocs                    = query.getMaxDocs();
		//double minScore                = query.getMinDocScore();
		//LSPField[] answerFields        = query.getAnswerFields();
		//LSPSortByField[] sortByFields  = query.getSortByFields();

		try {
			Query q = makeQuery(query);
			IndexReader reader = IndexReader.open(indexFilename);
			IndexSearcher searcher = new IndexSearcher(reader);
			Hits hits = searcher.search(q);
			//printHits (hits);

			LSPDoc[] docs = postProcess(hits, reader, query);
			//printHits (docs);

			results = new LSPResults(query.getFilter(), query.getRanking(), source, docs, hits.length());
			searcher.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BackEndException(e.getMessage());
		}

		return results;
	}

	private LSPDoc[] postProcess(Hits hits, IndexReader reader, LSPQuery query) throws IOException {
		// Get all needed fields
		int maxDocs = query.getMaxDocs();
		double minScore = query.getMinDocScore();
		LSPField[] answerFields = query.getAnswerFields();
		final LSPSortByField[] sortByFields = query.getSortByFields();

		// Count number of hits
		int numHits = hits.length();

		// Prepare for termstats building
		Set allTerms = new HashSet();
		LSPFilter filter = query.getFilter();
		if (filter != null) {
			allTerms.addAll(extractTerms(filter));
		}
		LSPRanking ranking = query.getRanking();
		if (ranking != null) {
			allTerms.addAll(extractTerms(ranking));
		}
		int numTerms = allTerms.size();

		TermDocs[] termDocsArray = new TermDocs[numTerms];
		TermWeight[] termWeightArray = new TermWeight[numTerms];
		int[] docFreqArray = new int[numTerms];
		int index = 0;
		for (Iterator it = allTerms.iterator(); it.hasNext();) {
			LSPTerm lspTerm = (LSPTerm) it.next();
			TermWeight termWeight = makeTerm(lspTerm, null);

			termWeightArray[index] = termWeight;
			termDocsArray[index] = reader.termDocs(termWeight.term);
			docFreqArray[index] = reader.docFreq(termWeight.term);
			index++;
		}

		// Build lspdocs using answerfields
		List docList = new ArrayList(numHits);
		if (answerFields == null) {
			answerFields = metaAttributes.getFieldsSupported();
		}
		int numAnswerFields = answerFields.length;

		for (int i = 0; i < numHits; i++) {
			LSPDoc doc = new LSPDoc();
			Document d = hits.doc(i);
			doc.addSource(source);
			doc.setRawScore(hits.score(i));

			for (int j = 0; j < numAnswerFields; j++) {
				LSPField field = answerFields[j];
				String fieldName = field.getName();

				// CANNOT RETURN BODY OF TEXT
				if (fieldName.equals(FieldNames.BODY_OF_TEXT)) {
					continue;
				}

				Field f = d.getField(fieldName);
				if (f != null) {
					doc.addFieldValue(field, f.stringValue());
				}
			}
			doc.setDocSize(Integer.parseInt(d.getField(LuceneConstants.LUCENE_DOC_SIZE).stringValue()));
			doc.setDocCount(Integer.parseInt(d.getField(LuceneConstants.LUCENE_DOC_COUNT).stringValue()));

			int k = 0;
			for (Iterator it = allTerms.iterator(); it.hasNext();) {
				LSPTerm lspTerm = (LSPTerm) it.next();
				int termFreq = 0;
				double termWeight = 0;
				int docFreq = 0;

				// term freq - number of times in doc
				TermDocs td = termDocsArray[k];
				if (td != null) {
					boolean foundDoc = false;
					while (td.next()) {
						int docId = td.doc();
						if (docId == i) {
							foundDoc = true;
							break;
						}
					}
					if (foundDoc) {
						termFreq = td.freq();
					} else {
						termFreq = 0;
					}
				} else {
					termFreq = 0;
				}

				// term weight - normatlized tf . idf weight for term ? ?
				TermWeight tw = termWeightArray[k];
				termWeight = tw.weight;

				// doc-freq - num docs in source that have term
				docFreq = docFreqArray[k];

				doc.addTermStat(lspTerm, termFreq, termWeight, docFreq);
				k++;
			}

			docList.add(doc);
		}

		// Copy hits into List, and elim by minscore
		for (int i = 0; i < numHits; i++) {
			LSPDoc doc = (LSPDoc) docList.get(i);
			if (doc.getRawScore() < minScore) {
				docList.remove(doc);
			}
		}

		// Eval sortbyfields and make comparator & sort
		if (sortByFields != null) {
			Comparator c = new Comparator() {
				public int compare(Object obj1, Object obj2) {
					LSPDoc first = (LSPDoc) obj1;
					LSPDoc second = (LSPDoc) obj2;

					int numSortByFields = sortByFields.length;
					for (int i = 0; i < numSortByFields; i++) {
						LSPSortByField sbf = sortByFields[i];
						if (sbf.isScore()) {
							double num = first.getRawScore() - second.getRawScore();
							if (num != 0) {
								if (num < 0)
									return 1;
								if (num > 0)
									return -1;
							} else {
								continue;
							}
						}
						int ascendingDescending = sbf.getAscendingDescending();
						String fieldName = sbf.getField().getName();
						String firstVal = first.getValue(fieldName);
						String secondVal = second.getValue(fieldName);
						int comp = 0;
						if (firstVal == null && secondVal == null) {
							comp = 0;
						} else if (firstVal != null && secondVal == null) {
							comp = 1;
						} else if (firstVal == null && secondVal != null) {
							comp = -1;
						} else {
							comp = firstVal.compareTo(secondVal);
						}
						if (ascendingDescending == LSPSortByField.DESCENDING) {
							return (comp * -1);
						} else {
							return comp;
						}
					}
					return 0;
				}
			};

			Collections.sort(docList, c);
		}

		// do maxdoc cut off
		if (docList.size() > maxDocs) {
			List subList = new ArrayList(docList.subList(0, maxDocs));
			docList = subList;
		}

		// All done
		return (LSPDoc[]) docList.toArray(new LSPDoc[0]);
	}

	// -------- OVERRIDDEN METHODS --------
	public void setActualFilter(LSPQuery query) throws BackEndException {
		LSPFilter filter = query.getFilter();
		if (filter == null) {
			query.setFilter(rankingToFilter(query.getRanking()));
			query.setRanking(null);
		}
		super.setActualFilter(query);
	}

	public void setActualRanking(LSPQuery query) throws BackEndException {
		super.setActualRanking(query);
		LSPRanking ranking = query.getRanking();
		if (ranking != null && ranking.getType() != LSPRanking.TERMS) {
			Set termSet = extractTerms(ranking);
			LSPTerm[] terms = (LSPTerm[]) termSet.toArray(new LSPTerm[0]);
			ranking.set(terms);
		}
		query.setRanking(ranking);
	}

	// -------- HELPER METHODS --------
	private Query makeQuery(LSPQuery lspQuery) {
		LSPRanking ranking = lspQuery.getRanking();
		LSPTerm[] rankingTerms = null;
		if (ranking != null && ranking.getType() == LSPRanking.TERMS) {
			rankingTerms = ranking.getTerms();
		}

		LSPFilter filter = lspQuery.getFilter();
		Query q = makeFromFilters(filter, rankingTerms);
		return q;
	}

	private Set extractTerms(LSPObject obj) {
		Set s = null;
		int type = -1;
		if (obj instanceof LSPRanking) {
			LSPRanking ranking = (LSPRanking) obj;
			type = ranking.getType();
			switch (type) {
				case LSPRanking.TERMS :
					s = new HashSet(Arrays.asList(ranking.getTerms()));
					break;

				case LSPRanking.TERM_PROXOP_TERM :
					s = new HashSet(2);
					s.add(ranking.getFirstTerm());
					s.add(ranking.getSecondTerm());
					break;

				case LSPRanking.RANKING_BOOLEANOP_RANKING :
					Set sr1 = extractTerms(ranking.getFirstRanking());
					Set sr2 = extractTerms(ranking.getSecondRanking());
					sr1.addAll(sr2);
					s = sr1;
					break;
			}
		} else {
			LSPFilter filter = (LSPFilter) obj;
			type = filter.getType();
			switch (type) {
				case LSPFilter.TERM :
					s = new HashSet(1);
					s.add(filter.getTerm());
					break;

				case LSPFilter.TERM_PROXOP_TERM :
					s = new HashSet(1);
					s.add(filter.getFirstTerm());
					s.add(filter.getSecondTerm());
					break;

				case LSPFilter.FILTER_BOOLEANOP_FILTER :
					Set sr1 = extractTerms(filter.getFirstFilter());
					Set sr2 = extractTerms(filter.getSecondFilter());
					sr1.addAll(sr2);
					s = sr1;
					break;
			}
		}

		return s;
	}

	private LSPFilter rankingToFilter(LSPRanking ranking) {
		LSPFilter filter = null;
		switch (ranking.getType()) {
			case LSPRanking.TERMS :
				filter = new LSPFilter(ranking.getTerms()[0]);
				break;

			case LSPRanking.TERM_PROXOP_TERM :
				filter = new LSPFilter(ranking.getFirstTerm(), ranking.getProxOp(), ranking.getSecondTerm());
				break;

			case LSPRanking.RANKING_BOOLEANOP_RANKING :
				filter =
					new LSPFilter(
						rankingToFilter(ranking.getFirstRanking()),
						ranking.getBooleanOp(),
						rankingToFilter(ranking.getSecondRanking()));
				break;
		}
		return filter;
	}

	private Query makeFromFilters(LSPFilter filter, LSPTerm[] rankingTerms) {
		Query query = null;
		int filterType = filter.getType();

		switch (filterType) {
			case LSPFilter.TERM :
				LSPTerm lspTerm = filter.getTerm();
				TermWeight tw = makeTerm(lspTerm, rankingTerms);

				// make a TermQuery or a PhraseQuery based on number of tokens in the Term
				StringTokenizer tok = new StringTokenizer(tw.term.text());
				if (tok.countTokens() > 1) { // is a phrase
					PhraseQuery pq = new PhraseQuery();
					Term t;
					while (tok.hasMoreTokens()) {
						t = new Term(tw.term.field(), tok.nextToken());
						pq.add(t);
					}

					if (tw.weight != 0) {
						pq.setBoost((float) tw.weight);
					}

					query = pq;
				} else { // is a term
					TermQuery tq = new TermQuery(tw.term);
					if (tw.weight != 0) {
						tq.setBoost((float) tw.weight);
					}

					query = tq;
				}

				break;

			case LSPFilter.TERM_PROXOP_TERM :
				TermWeight tw1 = makeTerm(filter.getFirstTerm(), rankingTerms);
				TermWeight tw2 = makeTerm(filter.getSecondTerm(), rankingTerms);
				float weight1 = (float) (tw1.weight);
				float weight2 = (float) (tw2.weight);
				float weight = 0;
				if (weight1 == LuceneConstants.DEFAULT_WEIGHT && weight2 == LuceneConstants.DEFAULT_WEIGHT) {
					weight = (float) LuceneConstants.DEFAULT_WEIGHT;
				} else if (weight1 == 1.0 && weight2 == 1.0) {
					weight = 1.0f;
				} else {
					weight = weight1 + weight2;
				}

				LSPProxOp proxOp = filter.getProxOp();
				int proximity = proxOp.getProximity();
				PhraseQuery pq1 = new PhraseQuery();
				pq1.add(tw1.term);
				pq1.add(tw2.term);
				pq1.setSlop(proximity);
				if (weight != 0) {
					pq1.setBoost(weight);
				}
				if (!proxOp.wordOrderMatters()) {
					PhraseQuery pq2 = new PhraseQuery();
					pq2.add(tw2.term);
					pq2.add(tw1.term);
					pq2.setSlop(proximity);
					if (weight != 0) {
						pq2.setBoost(weight);
					}
					BooleanQuery bq = new BooleanQuery();
					bq.add(pq1, false, false);
					bq.add(pq2, false, false);
					query = bq;
				} else {
					query = pq1;
				}
				break;

			case LSPFilter.FILTER_BOOLEANOP_FILTER :
				LSPFilter firstFilter = filter.getFirstFilter();
				LSPFilter secondFilter = filter.getSecondFilter();

				Query q1 = makeFromFilters(firstFilter, rankingTerms);
				Query q2 = makeFromFilters(secondFilter, rankingTerms);

				LSPBooleanOp booleanOp = filter.getBooleanOp();
				BooleanQuery bq = new BooleanQuery();
				switch (booleanOp.getType()) {
					case LSPBooleanOp.AND :
						bq.add(q1, true, false);
						bq.add(q2, true, false);
						break;

					case LSPBooleanOp.OR :
						bq.add(q1, false, false);
						bq.add(q2, false, false);
						break;

					case LSPBooleanOp.AND_NOT :
						bq.add(q1, true, false);
						bq.add(q2, false, true);
						break;
				}
				query = bq;
				break;
		}

		return query;
	}

	private TermWeight makeTerm(LSPTerm lspTerm, LSPTerm[] rankingTerms) {
		if (termWeights.containsKey(lspTerm)) {
			TermWeight tw = (TermWeight) termWeights.get(lspTerm);
			return tw;
		}

		Term term = new Term(lspTerm.getField().getName(), lspTerm.getValue().getValue().toLowerCase()); // case-insensitive
		double weight = 1;
		if (rankingTerms != null) {
			int len = rankingTerms.length;
			for (int i = 0; i < len; i++) {
				if (rankingTerms[i].equals(lspTerm)) {
					weight = rankingTerms[i].getWeight();
					if (weight == 0) {
						weight = LuceneConstants.DEFAULT_WEIGHT;
					} else {
						weight += 1.0;
					}
					break;
				}
			}
		}

		TermWeight tw = new TermWeight(term, weight);
		termWeights.put(lspTerm, tw);
		return tw;
	}

	// ------------ INNER CLASSES ------------
	private class TermWeight {
		Term term;
		double weight;

		TermWeight(Term term, double weight) {
			this.term = term;
			this.weight = weight;
		}

		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			TermWeight other = (TermWeight) obj;
			return (other.term.equals(term) && other.weight == weight);
		}

		public int hashCode() {
			return (term.hashCode() + (new Double(weight)).hashCode());
		}
	}

	// -------- DEBUG METHODS --------
//	private void printHits(LSPDoc[] docs) throws IOException {
//		int len = docs.length;
//		XMLWriter writer = new XMLWriter(System.out, new String[] { "sqrdocument" });
//		for (int i = 0; i < len; i++) {
//			docs[i].toXML(writer);
//		}
//		writer.flush();
//	}

//	private void printHits(Hits h) throws IOException {
//		if (h != null) {
//			int len = h.length();
//			System.out.println("Found " + len + " documents");
//			for (int i = 0; i < len; i++) {
//				Document d = h.doc(i);
//				System.out.println("Title: " + d.getField("title").stringValue());
//				System.out.println("Author: " + d.getField("author").stringValue());
//				System.out.println("Date: " + DateField.stringToDate(d.getField("date-last-modified").stringValue()));
//				System.out.println("Score: " + h.score(i));
//				System.out.println("Size in KB: " + d.getField(LuceneConstants.LUCENE_DOC_SIZE).stringValue());
//				System.out.println("Numtokens: " + d.getField(LuceneConstants.LUCENE_DOC_COUNT).stringValue());
//				System.out.println("------------------------------------------");
//			}
//		}
//	}
}
