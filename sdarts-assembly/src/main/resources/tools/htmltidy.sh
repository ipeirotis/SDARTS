#!/bin/sh

#
# htmltidy.sh
#

#
# Given an HTML document, this script 
# will produce a version that has been turned into XML (not XHTML) by
# HTML Tidy (http://www.w3.org/People/Raggett/tidy/), just
# as the sdarts.backend.www  package does, in order to see how
# a web page read by that framework would look before a stylesheet is applied
# to it. This tool is useful when developing www_results.xsl
# files. The output of this script should  be piped to a file, and then run
# through the xsltest.sh script. This script is built around the 
# sdarts.tools.HTMLTidy class. Here is the usage string:
#
# Usage: htmltidy.sh <documentName>
# Where:
# <documentName> is the name of the HTML document to tidy
#
# The script does not try to validate the HTML document, nor is there
# any way to set it to do this.
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
JAVA_HOME=/usr/local/java/java1.4


java -classpath $SDARTS_HOME/lib/sdarts.jar:$SDARTS_HOME/lib/Tidy.jar sdarts.tools.HTMLTidy $0 $1
