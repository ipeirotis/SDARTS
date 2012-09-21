#!/bin/sh

#
# xmlvalidate.sh
#

# 
# This tool is used for validating an XML document. The document must
# have a <!DOCTYPE . . . > header pointing to a valid DTD. The script
# is built around the sdarts.tools.XMLValidate Java class. Here is the
# usage string produced by the script:
#
# Usage: xmlvalidate.sh [-v] <documentName>
# Where:
# -v if present, verbose output
# <documentName> is the name of the XML document to process
#

# SDARTS_HOME variable. This needs to be set so the script can know
# where to look for .jar files. If you are reading these comments
# inside the distribution, it will be set to a placeholder character.
# The install.sh will modify the placeholder when installing. If you
# are reading this script in an installed server directory, the variable
# will be set to this directory name
#
SDARTS_HOME=/home/dli2test/sdarts


java -classpath $SDARTS_HOME/lib/sdarts.jar:$SDARTS_HOME/lib/xerces.jar:$SDARTS_HOME/lib/xalan.jar sdarts.tools.XMLValidate $0 $1 $2
