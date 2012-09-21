#!/bin/sh

#
# sdarts.sh
#
# Starts the SDARTS server
# 
# See the usage message below in the script code to see how this
# works.
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

if [ $# -eq 0 -o $# -gt 2 -o "$1" = "-?" ]
then
	echo
	echo "Usage: "
	echo "sdarts.sh -?"
	echo "sdarts.sh [port] lsp_name"
	echo
	echo "  -? - Prints this help message"
   	echo "  [port] - The port where SDARTS shoud listen for requests"
	echo "		 Default is 8080. (optional)"
	echo "  lsp_name - The name that SDARTS should register itself"
	echo "             under, for clients to contact it. For example"
	echo "             if you entered 'lsp1', and were running on"
	echo "             host 'www.cs.columbia.edu', a client would"
	echo "             contact SDARTS with the URL: "
	echo "             'http://www.cs.columbia.edu:8080/lsp1'"
	echo
	echo "The script will configure the server using "
	echo $SDARTS_HOME"/config/sdarts_config.xml"
	exit 1
fi

$JAVA_HOME/bin/java -mx2000m -classpath $SDARTS_HOME/lib/sdarts-server-complete.jar sdarts.frontend.SDARTS $1 $2 $SDARTS_HOME 1>$SDARTS_HOME/sdarts_out.log 2>$SDARTS_HOME/sdarts_err.log

# /usr/local/java/java1.4/bin/java -mx2000m -classpath $SDARTS_HOME/lib/sdarts-server.jar sdarts.frontend.SDARTS $1 $2 $SDARTS_HOME



