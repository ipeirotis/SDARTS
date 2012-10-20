XML COLLECTION WRAPPING INSTRUCTIONS

Assuming the directory where sdarts server is installed is $SDARTS_INSTALL. For unindexed local xml documents, "doc" wrapper is used, and it is very similar to the text documents wrappers, except for a few differences listed below.

	1. Differences between XML Documents Wrapper and Text Documents Wrapper

		The xml documents wrapper is very similar to the text documents wrappers. The differences are listed here:

		1. The classname for the wrapper's Lucene-based reference implementation is:
				sdarts.backend.impls.xml.XMLBackEndLSP
				whilte for text collection it is
				sdarts.backend.impls.xml.TextBackEndLSP
		2. The doc_config.xml file should not have any <field-descriptor>s. It may not need <date-formats> either.
		3. A second file, doc_style.xsl, is needed.
		4. The offline indexing script is called xmlsetup.sh. It works exactly the same as textsetup.sh
	
	2. XML Stylesheet: doc_style.xsl

		doc_style.xsl is an XSL stylsheet, and to create one you should be familiar with the XSL syntax. SDARTS currently uses the Apache Xalan processor for processing the XSL.

		The basic concept for all doc_style.xsl sheets is to transform each document to be indexed into an intermediate form that can be used by the sdarts.backend.impls.XMLDocumentEnum class to find fields and construct an index. This form is called "starts_intermediate" and is described in the starts_intermediate.dtd file, in the $SDARTS_INSTALL/dtd subdirectory. Basically, it is an augmented subset of STARTS. In the wrapper we are discussing in this section, the output of the transformation should appear like this:

			<!DOCTYPE starts:intermediate SYSTEM 
			  http://www.cs.columbia.edu/~dli2test/dtd/starts_intermediate.dtd>
			<starts:intermediate>
			<starts:sqrdocument>
			  <starts:doc-term>
				<starts:field name="title"/>
				<starts:value>Design Patterns</starts:value>
			  </starts:doc-term>
			  <starts:doc-term>
				<starts:field name="author"/>
				<starts:value>Erich Gamma, et al</starts:value>
			  </starts:doc-term>
					. . . . . . . . . . .
			 </starts:sqrdocument>
			</starts:intermediate>
		Notice how there is only one <starts:sqrdocument> inside the <starts:intermediate> tag. That is because the doc_style.xsl document describes the transformation of one XML document from the collection into one STARTS <starts:sqrdocument>.

		It is never actually output, but rather transformed by the Xalan processor into a series of SAX events, which the sdarts.backend.impls.XMLDocumentEnum then responds to.

		NOTE: If the documents you are indexing have <!DOCTYPE . . . > tags that reference DTDs, you must make sure these DTDs exist and are accessible to the Xalan processor. Currently, there is no way to prevent Xalan from trying to load these DTDs.

	3. Scripts to help setup xml documents collection

		In directory $SDARTS_INSTALL/tools, you will find the following two scripts:

		  xmlsetup.sh -- to build an index, meta-attributes file, and content-summary
						 file for an Lucene-wrapped xml collection.

		  xmltest.sh -- Given an XML document and an XSL stylesheet, xsltest.sh will 
						output the results of the transformation. This is a good tool
						for applying your doc_style.xsl stylesheet to a sample document
						from the collection, and seeing whether the output is a good
						<starts:intermediate>. Here is the usage string from the script.
						Ignore the details about the -tidy parameter; this is only
						important for the "www" wrapper.

		  Usage: xsltest.sh [-tidy] <documentName> <stylesheetName>

		  Where:
		   -tidy -- indicates to preprocess XML input document with HTML Tidy, just as 
					sdarts.backend.www does with incoming HTML results <documentName>
					is the name of the XML document to process <stylesheetName> is the
					name of the XSL stylesheet to use.

		  xmlvalidate.sh -- check any XML document with a <!DOCTYPE . . .> and see 
							whether it is valid or not. You can use this to test the
							output of the xsltest.sh script, to make sure the output
							is a valid <starts:intermediate>. Here is the usage string
							for this script:

		   Usage: xmlvalidate.sh [-v] <documentName>

		   Where:
			-v -- if present, verbose output
				  <documentName> is the name of the XML document to process
	4. Sample Wrappers for xml documents collection

		The "aides" subdirectories of $SDARTS_INSTALL/config are examples of an "xml" wrapper.