SDARTS 1.1
----------

This sample wrapping contains the files necessary for wrapping
 a sample collection of remote web documents.

For more details look at the comments in the meta_attributes.xml,
www_query.xsl and www_results.xsl.


Files:

meta_attributes.xml - describes meta information about this collection,
                      including collection name, fields supported,
                      modifiers supported, query parts supported, ranking
                      algorithm, stop words, linkage-prefix and content
                      summary linkage.

www_query.xsl       - the XML style sheet to convert a starts:squery into a 
                      starts:intermediate, holding one or more starts:script
                      that can be used.

www_results.xsl     - the XML style sheet to transform XML-ified HTML that has
                      been returned by a web search engine into a
                      starts:intermediate. This starts:intermediate should
                      contain one or more starts:sqrdocument representing the
                      results, and, if one or more starts:script to perform
                      further invocations if one or more pages with results
                      are to be retrieved.
