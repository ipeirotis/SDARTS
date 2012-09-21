/**
 * This <code>QueryProcessor</code> implementation, while still abstract,
 * is still a bit more concrete and is a good one to subclass. The idea is
 * that since an {@link edu.columbia.cs.sdarts.common.LSPMetaAttributes LSPMetaAttributes}
 * object should contain all the information about what is legal and possible
 * in a query, it ought to be used as the basis for the
 * {@link edu.columbia.cs.sdarts.common.LSPQuery#setActualFilter() setActualFilter()},
 * {@link edu.columbia.cs.sdarts.common.LSPQuery#setActualRanking() setActualRanking()}, and
 * {@link edu.columbia.cs.sdarts.common.LSPQuery#validateQuery() validateQuery()} methods. The
 * class receives the <code>LSPMetaAttributes</code> object through its
 * constructor.
 * <p>
 * Once it is initialized, the class's <code>setActualFilter()</code> and
 * <code>setActualRanking()</code> methods will eliminate all illegal
 * field/modifier combinations from the incoming query's filter and ranking,
 * and will make a best-effort attempt at providing as much of the filter
 * and query as possible. The <code>validateQuery()</code> method will throw
 * an exception if one of the following conditions are true:
 * <ul>
 * <li>The <code>queryPartsSupported</code> property of
 * <code>LSPMetaAttributes</code> does not contain an "F", but the query
 * contains a filter
 * <li>The <code>queryPartsSupported</code> property of
 * <code>LSPMetaAttributes</code> does not contain an "R", but the query
 * contains a ranking
 * <li>The query has neither a filter nor a ranking
 * </ul>
 * <p>
 * The class also expands the <code>any</code> field to an <i>"OR"</i> of all
 * legal fields.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */

package edu.columbia.cs.sdarts.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.columbia.cs.sdarts.common.FieldNames;
import edu.columbia.cs.sdarts.common.LSPBooleanOp;
import edu.columbia.cs.sdarts.common.LSPField;
import edu.columbia.cs.sdarts.common.LSPFilter;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPModifier;
import edu.columbia.cs.sdarts.common.LSPObject;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPRanking;
import edu.columbia.cs.sdarts.common.LSPTerm;

public abstract class StandardQueryProcessor extends QueryProcessor {
	private String[] fieldNames;
	private Map fieldModifierCombos;
	private LSPMetaAttributes metaAttributes;

	/**
	 * Construct a <code>StandardQueryProcessor</code> that uses an
	 * <code>LSPMetaAttributes</code> object to tell it what fields,
	 * modifiers, and field/modifier combinations are legal.
	 * @param metaAttributes the <code>LSPMetaAttributes</code> object
	 * used to tell the class what fields, modifiers, and field/modifier
	 * combinations are legal
	 */
	public StandardQueryProcessor(LSPMetaAttributes metaAttributes) {
		this.metaAttributes = metaAttributes;
		LSPField[] fieldsSupported = metaAttributes.getFieldsSupported();
		int len = fieldsSupported.length;
		fieldNames = new String[len];
		for (int i = 0; i < len; i++) {
			fieldNames[i] = fieldsSupported[i].getName();
		}
		Arrays.sort(fieldNames);
		fieldModifierCombos = new HashMap(len);
		LSPObject[] combos = metaAttributes.getFieldModifierCombinations();
		len = combos.length;
		for (int i = 0; i < len; i += 2) {
			LSPField field = (LSPField) combos[i];
			LSPModifier mod = (LSPModifier) combos[i + 1];
			addFieldModifierCombo(field.getName(), mod.getName());
		}
	}

	/**
	 * Change the query's filter, if necessary, to eliminate all illegal
	 * field/modifier combinations.
	 * @param query the incoming query
	 */
	protected void setActualFilter(LSPQuery query) throws BackEndException {
		LSPFilter filter = query.getFilter();
		if (filter != null) {
			LSPFilter newFilter = checkFilter(filter);
			query.setFilter(newFilter);
		}
	}

	/**
	 * Change the query's ranking, if necessary, to eliminate all illegal
	 * field/modifier combinations.
	 * @param query the incoming query
	 */
	protected void setActualRanking(LSPQuery query) throws BackEndException {
		LSPRanking ranking = query.getRanking();
		if (ranking != null) {
			LSPRanking newRanking = checkRanking(ranking);
			query.setRanking(newRanking);
		}
	}

	/**
	 * Will throw an exception if any of the following is true:
	 * <ul>
	 * <li>The <code>queryPartsSupported</code> property of
	 * <code>LSPMetaAttributes</code> does not contain an "F", but the query
	 * contains a filter
	 * <li>The <code>queryPartsSupported</code> property of
	 * <code>LSPMetaAttributes</code> does not contain an "R", but the query
	 * contains a ranking
	 * <li>The query has neither a filter nor a ranking
	 * </ul>
	 * @exception BackEndException if the query is invalid
	 */
	protected void validateQuery(LSPQuery query) throws BackEndException {
		boolean canHaveFilter = (metaAttributes.getQueryPartsSupported().indexOf('F') >= 0);
		boolean canHaveRanking = (metaAttributes.getQueryPartsSupported().indexOf('R') >= 0);

		if (query.getRanking() == null && query.getFilter() == null) {
			throw new BackEndException("Cannot have null filter and null ranking");
		}

		if (!canHaveFilter && query.getFilter() != null) {
			throw new BackEndException("Filters are not supported");
		}

		if (!canHaveRanking && query.getRanking() != null) {
			throw new BackEndException("Rankings are not supported");
		}
	}

	/**
	 * Return the <code>LSPMetaAttributers</code> object used to initialize
	 * this class.
	 * @return the <code>LSPMetaAttributers</code> object used to initialize
	 * this class.
	 */
	public LSPMetaAttributes getMetaAttributes() {
		return metaAttributes;
	}

	// -------- HELPER METHODS ---------
	private void addFieldModifierCombo(String fieldName, String modifierName) {
		if (Arrays.binarySearch(fieldNames, fieldName) < 0) {
			return;
		}
		List l = null;
		if (!fieldModifierCombos.containsKey(fieldName)) {
			l = new LinkedList();
			fieldModifierCombos.put(fieldName, l);
		} else {
			l = (List) fieldModifierCombos.get(fieldName);
		}

		if (!l.contains(modifierName)) {
			l.add(modifierName);
		}
	}

	private LSPFilter checkFilter(LSPFilter filter) {
		int type = filter.getType();
		switch (type) {
			case LSPFilter.TERM :
				LSPTerm newTerm = checkTerm(filter.getTerm());
				if (newTerm == null) {
					filter = null;
				} else {
					filter.set(newTerm);
				}
				filter = expandANY(filter);
				break;

			case LSPFilter.TERM_PROXOP_TERM :
				LSPTerm firstNewTerm = checkTerm(filter.getFirstTerm());
				LSPTerm secondNewTerm = checkTerm(filter.getSecondTerm());
				if (firstNewTerm == null) {
					if (secondNewTerm == null) {
						filter = null;
					} else {
						filter.set(secondNewTerm);
					}
				} else if (secondNewTerm == null) {
					filter.set(firstNewTerm);
				} else {
					filter.set(firstNewTerm, filter.getProxOp(), secondNewTerm);
				}
				break;

			case LSPFilter.FILTER_BOOLEANOP_FILTER :
				LSPFilter firstNewFilter = checkFilter(filter.getFirstFilter());
				LSPFilter secondNewFilter = checkFilter(filter.getSecondFilter());
				if (firstNewFilter == null) {
					if (secondNewFilter == null) {
						filter = null;
					} else {
						filter = secondNewFilter;
					}
				} else if (secondNewFilter == null) {
					filter = firstNewFilter;
				} else {
					filter.set(firstNewFilter, filter.getBooleanOp(), secondNewFilter);
				}
				break;
		}

		return filter;
	}

	private LSPRanking checkRanking(LSPRanking ranking) {
		int type = ranking.getType();
		switch (type) {
			case LSPRanking.TERMS :
				LSPTerm[] newTerms = checkTerms(ranking.getTerms());
				if (newTerms == null) {
					ranking = null;
				} else {
					ranking.set(newTerms);
				}
				break;

			case LSPRanking.TERM_PROXOP_TERM :
				LSPTerm firstNewTerm = checkTerm(ranking.getFirstTerm());
				LSPTerm secondNewTerm = checkTerm(ranking.getSecondTerm());
				if (firstNewTerm == null) {
					if (secondNewTerm == null) {
						ranking = null;
					} else {
						ranking.set(new LSPTerm[] { secondNewTerm });
					}
				} else if (secondNewTerm == null) {
					ranking.set(new LSPTerm[] { firstNewTerm });
				} else {
					ranking.set(firstNewTerm, ranking.getProxOp(), secondNewTerm);
				}
				break;

			case LSPRanking.RANKING_BOOLEANOP_RANKING :
				LSPRanking firstNewRanking = checkRanking(ranking.getFirstRanking());
				LSPRanking secondNewRanking = checkRanking(ranking.getSecondRanking());

				if (firstNewRanking == null) {
					if (secondNewRanking == null) {
						ranking = null;
					} else {
						ranking = secondNewRanking;
					}
				} else if (secondNewRanking == null) {
					ranking = firstNewRanking;
				} else {
					ranking.set(firstNewRanking, ranking.getBooleanOp(), secondNewRanking);
				}
				break;
		}

		return ranking;
	}

	private LSPTerm[] checkTerms(LSPTerm[] terms) {
		int len = terms.length;
		List l = new ArrayList(len);
		for (int i = 0; i < len; i++) {
			LSPTerm t = checkTerm(terms[i]);
			if (t != null) {
				l.add(t);
			}
		}

		if (l.size() == 0) {
			return null;
		} else {
			return (LSPTerm[]) l.toArray(new LSPTerm[0]);
		}
	}

	private LSPTerm checkTerm(LSPTerm term) {
		String fieldName = term.getField().getName();
		LSPModifier[] modifiers = term.getModifiers();

		if (Arrays.binarySearch(fieldNames, fieldName) < 0) {
			return null;
		} else if (modifiers == null) {
			return term;
		} else {
			List l = (List) fieldModifierCombos.get(fieldName);
			if (l == null) {
				term.setModifiers(null);
				return term;
			}

			List mods = Arrays.asList(modifiers);
			int len = modifiers.length;
			for (int i = 0; i < len; i++) {
				String name = ((LSPModifier) mods.get(i)).getName();
				if (!l.contains(name)) {
					mods.remove(name);
				}
			}
			modifiers = (LSPModifier[]) mods.toArray(new LSPModifier[0]);
			term.setModifiers(modifiers);
			return term;
		}
	}

	/**
	 * expand terms in ANY field into all other supported fields,
	 * with a OR relationship between all expansions of a given term
	 * the boolean relation ship between terms in ANY fields will be kept
	 * eg: any: enterococcus AND faecalis AND endocarditis
	 *     with a support field body-of-text and author 
	 * will be expanded to:
	 *     ( body-of-text: enterococcus OR author: enterococcus )
	 * AND ( body-of-text: faecalis OR author: faecalis )
	 * AND ( body-of-text: endocarditis OR author: endocarditis )
	 */
	private LSPFilter expandANY(LSPFilter filter) {

		if (filter == null) {
			return null;
		}

		LSPTerm term = filter.getTerm();
		if (!term.getField().getName().equalsIgnoreCase(FieldNames.ANY)) {
			return filter;
		}

		LSPFilter newFilter = null;
		for (int i = 0; i < fieldNames.length; i++) {
			if (!(fieldNames[i].equals(FieldNames.ANY) || fieldNames[i].equals(FieldNames.LINKAGE))) {
				if (newFilter == null) {
					newFilter = makeFilterWithDifferentTerm(term, fieldNames[i]);
				} else {
					newFilter =
						new LSPFilter(
							(LSPFilter) newFilter.clone(),
							new LSPBooleanOp(LSPBooleanOp.OR),
							makeFilterWithDifferentTerm(term, fieldNames[i]));
				}
			}
		}

		return newFilter;
	}

	private LSPFilter makeFilterWithDifferentTerm(LSPTerm originalTerm, String newFieldName) {
		return new LSPFilter(
			new LSPTerm(
				new LSPField(newFieldName),
				originalTerm.getModifiers(),
				originalTerm.getValue().getValue(),
				originalTerm.getWeight()));
	}
}
