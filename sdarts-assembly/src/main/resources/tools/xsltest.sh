#!/bin/sh

#
# xsltest.sh
#

#
# Given an XML document and an XSL stylesheet, this script 
# will produce the transformed output. In addition,
# if passed the -tidy parameter, the script can pre-process
# the XML document using
# HTML Tidy (http://www.w3.org/People/Raggett/tidy), just
# as the sdarts.backend.www package does, in order to see how
# a web page read by that framework would look if stylesheet is applied
# to it. This tool is useful when developing www_results.xsl
# and www_query.xsl files. Use the "-tidy" parameter 
# for the former; don't use it for the latter. In fact, it's probably
# best not to use -tidy at all, but rather to get a tidied file using
# the htmltidy.sh script. Then, you can look at that output, and build
# the www_results.xsl stylesheet knowing what it is it will be processing.
#
# This script is built around the sdarts.tools.XSLTest class. 
# Here is the usage string:
#
# Usage: xsltest.sh [-tidy] <documentName> <stylesheetName>
# Where:
# -tidy indicates to preprocess XML input document with HTML Tidy,
# just as sdarts.backend.www does with incoming HTML results
# <documentName> is the name of the XML document to process
# <stylesheetName> is the name of the XSL stylesheet to use
# 
# The script does not try to validate the XML document, nor is there
# any way to set it to do this.
# 
#


#
# SDARTS_HOME variable. This needs to be set so the script can know
# where to look for .jar files. If you are reading these comments
# inside the distribution, it will be set to a placeholder character.
# The install.sh will modify the placeholder when installing. If you
# are reading this script in an installed server directory, the variable
# will be set to this directory name
#
SDARTS_HOME=/home/dli2test/sdarts


java -classpath $SDARTS_HOME/lib/sdarts.jar:$SDARTS_HOME/lib/Tidy.jar:$SDARTS_HOME/lib/xerces.jar:$SDARTS_HOME/lib/xalan.jar sdarts.tools.XSLTest $0 $1 $2 $3 $4



