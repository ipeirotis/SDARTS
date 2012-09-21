/**
 * A convenient utility class for sorting {@link edu.columbia.cs.sdarts.common.LSPDoc LSPDocs}
 * using an array of {@link edu.columbia.cs.sdarts.common.LSPSortByField LSPSortByFields}.
 * This should be used inside a
 * {@link edu.columbia.cs.sdarts.backend.QueryProcessor QueryProcessor} implementation, for
 * sorting results before returning them.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */

package edu.columbia.cs.sdarts.backend;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.columbia.cs.sdarts.common.LSPDoc;
import edu.columbia.cs.sdarts.common.LSPSortByField;

public class LSPDocSorter {

	/**
	 * Sort a collection of <code>LSPDocs</code>. Note that this changes
	 * the ordering of the original collection; it does not copy and
	 * return a sorted collection.
	 * @param docs a <code>List</code> containing <code>LSPDocs</code> to
	 * be sorted
	 * @param sortByFields the sorting fields to use, in order of precedence
	 */
	public static void sortLSPDocs(List docs, LSPSortByField[] sortByFields) {
		if (sortByFields == null || sortByFields.length == 0) {
			return;
		}

		Comparator c = buildComparator(sortByFields);
		Collections.sort(docs, c);
	}

	/**
	 * Sort a collection of <code>LSPDocs</code>. Note that this changes
	 * the ordering of the original collection; it does not copy and
	 * return a sorted collection.
	 * @param docs an array containing <code>LSPDocs</code> to
	 * be sorted
	 * @param sortByFields the sorting fields to use, in order of precedence
	 */
	public static void sortLSPDocs(LSPDoc[] docs, LSPSortByField[] sortByFields) {
		if (sortByFields == null || sortByFields.length == 0) {
			return;
		}

		Comparator c = buildComparator(sortByFields);
		Arrays.sort(docs, c);
	}

	/**
	 * Builds a comparator for <code>LSPDocs</code>. 
	 * @param sortByFields the sorting fields to use, in order of precedence
	 */
	private static Comparator buildComparator(final LSPSortByField[] sortByFields) {
		// Build the comparator
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
							if (num < 0) {
								return 1;
							}
							if (num > 0) {
								return -1;
							}
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
					if (comp != 0) {
						if (ascendingDescending == LSPSortByField.DESCENDING) {
							return (comp * -1);
						} else {
							return comp;
						}
					}
				}
				return 0;
			}
		};
		return c;
	}
}
