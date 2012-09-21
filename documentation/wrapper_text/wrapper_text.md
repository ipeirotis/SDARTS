TEXT COLLECTION WRAPPING INSTRUCTIONS
-------------------------------------

For plain text documents, "doc" wrapper is used. Assuming the directory the sdarts server is installed is $SDARTS_INSTALL.

    1. Register the collection with sdarts server by editing $SDARTS_INSTALL/config/sdarts_config.xml to include this collection.

        For example, if we had a collection called "books", then in sdarts_config.xml, we would have:

            <sdarts_config>
                .....
                <back-end-lsp>
                <classname> ........
                <name> books </name>
                ......
            <sdarts_config>

    2. Create the collection config directory

        Create the collection config directory under $SDARTS_INSTALL/config, with THE SAME NAME as the one with which the collection is registered in sdarts_config.xml. For example, if the collection is registered as "books", a subdirectory named books will be created in $SDARTS_INSTALL/config. All configuration files of this collection will be stored in this directory.

        With the same example, we now have collection config directory:

          $SDARTS_INSTALL/config/books

    3. Create the configuration file: doc_config.xml

        Within the doc_config.xml file, we are telling SDARTS:

            a. Where to find the DTD for the doc_config.xml document (rarely changes)
            b. Whether or not to re-index the collection each time the SDARTS server comes up.
            c. Where to look for documents in the collection (only portion from the sample files that requires editing, if you wish to mount the same collection the sample file describes.)
            d. What the "linkage-prefix" is for the collection, which is a way that the STARTS protocol uses to tell users how to access the collection directly to retrieve documents.
            e. What are the stop-words (common words that don't go into the index like "a", "the", etc.)
            f. What fields will be searchable, and where can their contents be found in each document.
            g. What kinds of date formats might be expected in fields that are dates.

        See wrapper_text.details.txt for details.

    4. Other configuration related files/directories:

        index/              -- a directory containing Lucene-specific index files
                               for this collection
        meta-attributes.xml -- a STARTS xml file of type ,
                               containing meta info about this collection. It will 
                               be created upon activation of the sdarts server, if 
                               it does not exist. It can also be mannually editted, 
                               or be overwritten by running a script to index this
                               collection.

        content-summary.xml -- a STARTS xml file of type ,
                               containing summary information about this collection.

    5. Script to perform offline re-indexing of a Lucene-wrapped collection

        In directory $SDARTS_INSTALL/tools, you will find a script textsetup.sh, which you can use to perform offline re-indexing of a Lucene-wrapped text collection. Run this script only from the directory of an installed SDARTS server. 
        Here is the usage string from the script:

            Usage: textsetup.sh -name 
                               [-sdurl, -starts-dtd-url ]
                               [-o, -overwrite-meta-attributes]

            Where  is the name under which the LSP is registered.
            The name must be listed in the sdarts_config.xml file.
            -sdurl or -starts-dtd-url is the optional URL of the STARTS DTD
                (default is http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd
            -o or -overwrite-meta-attributes, if present, will cause the
                script to overwrite the meta-attributes file (default false,
                in case you wish to edit it by hand.)

    6. Sample wrappers for text documents collection

        The "2ogroups" subdirectory of $SDARTS_INSTALL/config is an example of a "text" wrapper.
