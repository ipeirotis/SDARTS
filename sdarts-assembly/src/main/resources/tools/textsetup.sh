#!/bin/sh

#
# textsetup.sh
#
# This script is used for building an index, meta-attributes file,
# and content-summary file for a subcollection wrapped by the
# sdarts.backend.impls.text.TextBackEndLSP framework. This is a wrapper for
# unindexed, text document collections.
#
# You can either run this script,
# or, in the doc_config.xml configuration file associated with the 
# subcollection, have the "re-index" attribute of the "doc-config"
# tag be set to true. If you do the latter, then the SDARTS server
# will re-index the collection every time it starts up. Using this
# script will save that time if you'd rather index only intermittently.
#
# To run this script, you need the following:
# 1. A doc_config.xml file whose collection you are indexing. This
# file must reside in the SDARTS_HOME/config/<name_of_LSP> directory.
# SDARTS_HOME is defined further down. <name_of_BackEndLSP> is a script
# parameter
#
# Usage: textsetup.sh -name <name_of_BackEndLSP>
#                    [-sdurl, -starts-dtd-url <url>]
#                    [-o, -overwrite-meta-attributes]
#
# Where <name_of_BackEndLSP> is the name under which the BackEndLSP is 
# registered.
#  	The name must be listed in the sdarts_config.xml file.
# -sdurl or -starts-dtd-url is the optional URL of the STARTS DTD
#     (default is http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd
# -o or -overwrite-meta-attributes, if present, will cause the
#     script to overwrite the meta-attributes file (default false,
#     in case you wish to edit it by hand.)


#
# SDARTS_HOME variable. This needs to be set so the script can know
# where to look for .jar files. If you are reading these comments
# inside the distribution, it will be set to a placeholder character.
# The install.sh will modify the placeholder when installing. If you
# are reading this script in an installed server directory, the variable
# will be set to this directory name
#
SDARTS_HOME=/home/dli2test/sdarts


#
# Actual command for running the server
#
java -mx2000m -classpath $SDARTS_HOME/lib/sdarts.jar:$SDARTS_HOME/lib/sdlip-1_1A.jar:$SDARTS_HOME/lib/gnu-regexp-1.0.8.jar:$SDARTS_HOME/lib/lucene.jar:$SDARTS_HOME/lib/xalan.jar:$SDARTS_HOME/lib/xerces.jar sdarts.backend.doc.lucene.LuceneSetup $0 $1 $2 $3 $4 $5 -hidden sdarts.backend.impls.text.TextDocumentEnum $SDARTS_HOME


