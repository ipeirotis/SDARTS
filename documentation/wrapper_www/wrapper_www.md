WWW COLLECTIONS WRAPPING INSTRUCTIONS
-------------------------------------

Assuming the directory the sdarts server is installed is $SDARTS_INSTALL. For web-based collection, "www" wrapper is used, it also has some similarities to the text collection wrapper.

It does not index, and content-sumamry must be created mannually using the Content Summary Extraction module. However, it is a very powerful wrapper that can convert STARTS XML queries into CGI-BIN invocations onto a web search engine, and translate returned HTML results in STARTS documents.

    1. Register the collection with sdarts server by editing $SDARTS_INSTALL/config/sdarts_config.xml to include this collection.

        Then create the collection config directory.

        See text collection wrapping instructions 1) and 2).

    2. Configuration related files:

        meta_attributes.xml -- Since web search engines generally do not provide 
                               this information, you must write your own.  This is
                               standard STARTS XML - see the SDARTS Design Doc, and
                               the starts.dtd, for more information.

        www_query.xsl       -- An XSL stylesheet for converting a STARTS XML query
                               into the "starts_intermediate" format

        www_results.xsl     -- An XSL stylesheet for converting XML-ified HTML into
                               the "starts_intermediate" format

       Note: No index files are created, and content_summary.xml files must be created mannually using the Content Summary Extraction module.

    3. Query translation xsl file: www_query.xsl

       This file takes as its input a STARTS XML query; that is, a <starts:squery>. It produces a <starts:intermediate> of the following form:

            <!DOCTYPE starts:intermediate SYSTEM 
              http://www.cs.columbia.edu/~dli2test/dtd/starts_intermediate.dtd>
            <starts:intermediate>
                <starts:script>
                    <starts:url method='GET'>
                        http://www.google.com/search.cgi
                    </starts:url>
                    <starts:variable>
                       <starts:name>search<starts:name>
                        <starts:value>cardiovascular<starts:value>
                    </starts:variable>
                    <starts:variable> . . . . .
                </starts:script>
            </starts:intermediate>

What this is is an encoding for a CGI-BIN script. The best way to develop your www_query.xsl file is to analyze the <form> tag of the HTML front-end of the web search engine you are wrapping. You can then see what the URL and method for the script are (e.g. <form action="/search.cgi" method="GET">), and what the variables are (e.g. <INPUT type="text" name="search">). Make sure to look for hidden fields, etc. Then, take a few sample <starts:squery>'s and see how they would map to this format.

NOTE: the wrapper automatically takes care of the <starts:sort-by-fields>, <max-docs>, <min-doc-score>, and <starts:answer-fields> tags in an <starts:squery>. Ignore these, and focus on translating the <starts:filter> and <starts:ranking>, which are the heart of the query.

To test your www_query.xsl file, use the xsltest.sh and xmlvalidate.sh scripts mentioned in the wrapping instructions for xml collection

    4. Query result translation xsl file: www_results.xsl

       This file takes as its input an XML-ified HTML page returned from the web search engine, and converts it into a <starts:intermediate>. The output is very similar to the <starts:intermediate> you saw in the "xml" wrapper, except that it can and usually does contain more than one <starts:sqrdocument>, and can also contain one or more <starts:script>.

        Here is a what the output should look like:

            <!DOCTYPE starts:intermediate SYSTEM 
              http://www.cs.columbia.edu/~dli2test/dtd/starts_intermediate.dtd>
            <starts:intermediate>
            <starts:sqrdocument>
                <starts:rawscore>0.9</starts:rawscoregt;
                <starts:doc-term>
                    <starts:field name="title">
                    <starts:value>cardiovascular<starts:valuegt;
                </starts:doc-term>
                <starts:doc-term> . . .
            </starts:sqrdocument>
            <starts:sqrdocument> . . .

            <starts:script>
            <starts:url method='GET | POST'>
                http://www.google.com
            </starts:url>
            <starts:variable>
                <starts:name>search<starts:namegt;
                <starts:value>cardiovascular<starts:valuegt;
            </starts:variable>
            <starts:variable> . . . . .
                </starts:script>
            </starts:intermediate>

Each <starts:sqrdocumentgt; represents one result extracted from the page. The <starts:script> represents a translation of a "more" button at the bottom of the HTML results page, and is present only if there was such a button there. The wrapper uses this <starts:script> to automatically "press" the more button; it will continue doing so until it reaches the "max-docs" specified in the <starts:squery>, or until the search engine runs out of results.

To build your www_results.xsl file, you should first copy a sample HTML output page from the search engine. Copy several if their format varies. Next, you must realize that it is not this raw HTML that the wrapper looks at; HTML is not well-formed, and cannot be processed by XSL. The HTML first goes through a free tool from the W3C called "HTML Tidy", where it is turned into an XML form (it's almost XHTML, but not quite.) It is this "tidied" version that is presented to the www_results.xsl stylesheet for transformation. Therefore, you should test your stylesheet on tidied versions of this HTML pages. There are two ways to do this. One is to use the -tidy option in the xsltest.sh script (see "xml" wrapper). If you want to make copies of the tidied pages without them going through the transformation - this is usually the best route, since you need to look at what it is you are really going to transform before you even start writing the stylesheet - you can use the htmltidy.sh script to tidy any HTML page you have saved, as described in section 5).

    5. Tidy up your html results using htmltidy.sh

       Here is the usage of htmltidy.sh:

            Usage: htmltidy.sh <documentName>

            Where:
              <documentName> is the name of the HTML document to tidy
        All output is to stdout. There is also some noise to stderr from HTML Tidy; you can ignore this unless it is a real error.

        Do not get frustrated with the stylesheets! This wrapper is a powerful tool that can open up the whole World Wide Web to searchability by SDARTS.

    6. Sample wrappers for web-based collection

       There are four sample www wrappers under directory $SDARTS_INSTALL/config:

            cardio
            harrisons
            noah
            pubmed
