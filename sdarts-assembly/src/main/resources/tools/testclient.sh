#!/bin/sh

#
# testclient.sh
#
# Runs a simple test against the server. A server must be installed
# on the same machine the script is run on, regardless of whether
# the script is accessing that server or a remote one. If you
# like, you can modify this script so that the classpath points
# directly at a sdartsbean.jar that is not in the server installation
#
# See the "usage" in the script code to see how this script works.
#


#
# SDARTS_HOME variable. This needs to be set so the script can know
# where to look for .jar files. If you are reading these comments
# inside the distribution, it will be set to a placeholder character.
# The install.sh will modify the placeholder when installing. If you
# are reading this script in an installed server directory, the variable
# will be set to this directory name
#
SDARTS_HOME=.


if [ $# != 1 -a $# != 2 ]
then
    echo "Usage: testclient.sh <lspname> | <lspurl> [samplequery.xml]"
    echo
    echo "where <lspname> is the name of the lsp, if you are just"
    echo "contacting it locally. Example:"
    echo "    testclient.sh lsp1"
    echo
    echo "<lspurl> is the URL of the LSP if you are contacting it"
    echo "over a network. Example:"
    echo "    testclient.sh http://www.elsewhere.com:8080/lsp1"
    echo
    echo "If you are testing locally, but the LSP is at a port other"
    echo "than 8080, use the <lspurl> format:"
    echo "    testclient.sh http://localhost:otherportnumber/<lspname>"
    echo
    echo "[queryscript.xml] is an optional parameter that will load a "
    echo "file with that name, and use it to query the collection. This "
    echo "must be a <starts:squery>."
    exit 1
fi

java -classpath $SDARTS_HOME/lib/sdarts-server.jar sdarts.client.SDARTSBean $1 $2

if [ $? = 0 ]
then
    echo Everything looks good to me!
else
    echo Something is wrong!
fi
