SDARTS Server v1.1a README file -- 2002/07/15

by Panos Ipeirotis <pirot@cs.columbia.edu>
(initial version by Noah Green, modified by Sergey Sigelman)
modified by Jiangcheng Bao

1. INSTALLATION
---------------

  To install the sdarts server, copy sdarts-server.tar.gz to the destination
  directory, and then decompress it by running

  gzip -d sdarts-server.tar.gz
  tar -xvf sdarts-server.tar

2. SDARTS SERVER COMPONENTS
---------------------------

  config/ -- a directory containing the sdarts_config.xml file, which is used
             for configuring the server, and the wrapper configuration
             subdirectories.

  config/<backendlspname> -- for each subcollection that you wrap, there must
            be a subdirectory: config/<backendlspname>. It MUST HAVE THE SAME
            NAME as the collection/BackEndLSP is registered under in the 
            sdarts_config.xml file. The directory will contain configuration
            files. Currently, for wrappers built on the sdarts.backend.doc
            package - wrappers for local text and XML unindexed document
            collections -, the following files always present:
              doc_config.xml
            A wrapper for a collection of XML documents will also have:
              doc_style.xsl
            A wrapper for a remote web site search engine must have:
              meta_attributes.xml
              www_query.xsl
              www_results.xsl
            
            See the config directory for sample collections wrapped. Some of
            the files in these samples might require some editing, depending
            on where your local collections are located. All web wrappers
            should work as it it.
                             
  lib/ -- contains all .jar files needed by SDARTS.

  sdarts.sh -- the script for startsing the server

  tools/ -- contains scripts that will help you test XML files and XSL files
            for the wrappers.

  tools/testclient.sh -- the script for testing the server

  tools/textsetup.sh  -- the script for offline indexing and setup of a
                         collection of unindexed text documents using the
                         Lucene search engine. See wrapper_text.txt

  tools/xmlsetup.sh   -- the script for offline indexing and setup of a
                         collection of unindexed xml documents using the
                         Lucene search engine. See wrapper_xml.txt

3. CONFIGURING THE SDARTS SERVER
--------------------------------

  To configure the sdarts server, we must edit config/sdarts_config.xml to
  inform the sdarts server about:

  1) location of DTD for the sdarts_config.xml file

  2) location of DTD for the SDLIP XML

  3) location of DTD for STARTS XML

  4) wrapping each BackEndLSP/subcollections:
    i)   sdarts.backend.BackEndLSP subclass used to wrap the subcollection
    ii)  the name the subcollection will be known as
    iii) the description of the subcollection
    iv)  query language each subcollection supports (usually only starts)

  See config.txt for details for syntax of sdarts_config.xml


4. RUNNING SDARTS SERVER
------------------------

* To start the server, go to the directory where sdarts is installed, and type:

  java -jar lib/sdarts-server-complete.jar

  The actual full commmand executed is:

  java -classpath lib/sdarts-server-complete.jar sdarts.frontend.SDARTS <port>
<lspname> <directory> 

<port>      = the port that you want the SDARTS server to listen on, defaults
              to 8080.

<lspname>   = the name of your SDARTS server will register itslef under, for
              clients to contact it. For example, if you run it with lspname
              "lsp1", and were running on host "www.cs.columbia.edu", then
              a client would contact this SDARTS server at
              "http://www.cs.columbia.edu:8080/lsp1". Actually, this is the
              name that SDLIP's client/server protocol uses for contacting the
              lsp, because SDARTS uses SDLIP's HTTP/DASL protocol. The DASL
              transport module that comes with SDLIP can listen at the port,
              and dispatch to multiple LSPs, each with their own name. SDARTS
              does not support this because it gets too complicated, so
              actually there is only one LSP at the port.
              
<directory> = the directory that you have installed the SDARTS server

e.g.,
java -classpath lib/sdarts-server-complete.jar sdarts.frontend.SDARTS 1453 mysdarts .

PS: If you want to run the server on the background, use "javaw" instead of "java"

* The server at this point has the no subcollections, but with the following
  subcollections included in the configuration file but commentted out.

    aides
    20groups
    pubmed
    cardio
    noah
    harrisons

5. TESTING SDARTS SERVER
------------------------

  To check that your server is running, you can use our web-accessible
  SDARTS client at http://sdarts.cs.columbia.edu:8181/
  and enter the address of your SDARTS server. You should be able
  to connect, but no subcollections will be available at this moment.

  You can also test the sdarts server from command line using the test tool
  testclient.sh which is included in this distribution package. You will need
  to modify the sample query xml file to obtain correct query result.

  The script will
    i)   call the getInterface() method of the LSP, 
    ii)  call the getSubcollectionInfo() method of the LSP
    iii) figure out what the subcollections are by looking at the resutls of
         ii), and then call getPorpertyInfo() for each subcollection.
    iv) if a filename for a sample query is provided, the script will load it
        and run it.

  The script must be run from a machine that the sdarts server is installed,
  because it needs to know where the sdartsbean.jar locates. Alternatively,
  you can modify the testclient.sh script yourself to look somewhere else for
  sdartsbean.jar. It's all in the -classpath attribute of the java command
  inside the script. This is a good sample code for client construction.

  The usage string for the script:

    Usage: testclient.sh <lspname> | <lspurl> [queryscript.xml]

    where <lspname> is the name of the lsp, if you are just
    contacting it locally. Example:
      testclient.sh lsp1

    <lspurl> is the URL of the LSP if you are contacting it
    over a network. Example:
      testclient.sh http://www.elsewhere.com:8080/lsp1

    If you are testing locally, but the LSP is at a port other
    than 8080, use the <lspurl> format:
      testclient.sh http://localhost:otherportnumber/<lspname>

    [queryscript.xml] is an optional parameter that will load a
    file with that name, and use it to query the collection. This
    must be a <starts:squery>.

  Note that for all client/server interactions with SDARTS, you do
  not need a web server running on the same host as the SDARTS server.
  SDARTS has its own built-in http.

  Note also that for the sake of simplicity, this script assumes
  that the STARTS/SDLIP DTDs are both located at:
  http://www.cs.columbia.edu/~dli2test/dtd/sdarts.dtd
  http://www.cs.columbia.edu/~dli2test/dtd/sdlip.dtd


6. ADDING NEW COLLECTIONS
-------------------------

* First of all you have to have to modify the file config/sdarts_config.xml
  You can use as a template the existing entries (initially they are
  commented out), to add new collections.

  Three wrappers are included with the server distribution, with their
  configuration files (20groups, aides, cardio, harrisons, noah and pubmed).
  The wrappers are located in the subdirectories under the config/ directory.
  In each directory
  there is a readme.txt file with the details about the peculiarities
  of each wrapper, and with detailed instructions about the configuration.
  The collections are available from the SDARTS web site, and are not
  included in the server distribution.

  For more details you can download the "SDARTS Wrapper Development Toolkit."
  There you can find detailed documentation on how to create your own wrappers
  for your collections. Refer to wrapper_text.txt, wrapper_xml.txt and 
  wrapper_www.txt.

  NOTE: When you start the server with the "aides" and "20groups" collections
  added, the SDARTS server will start to index all the contents of these
  collections. This happens because, by default, the configuration files
  have this directive. If you want to load the server faster, after creating
  the indices, please change the doc_config.xml files for the "aides" and
  "20groups" collections.

7. DEVELOPING CLIENTS
---------------------

  So you've got a server up and running, and want to build your own clients.
  Maybe something slick like a Servlet/JSP arrangement that uses the 
  SDARTSBean?  Or an application with a complicated Swing interface?  
  (As long as it is not an Applet, since we all know how lame those are.)

  First of all, any existing SDLIP client can access SDARTS, using the
  HTTP/DASL protocol mentioned above. This client must be aware of the
  STARTS XML protocol, and the way in which SDARTS embeds STARTS inside of
  SDLIP. 

  Or, you can develop your own applications. It is best to use the
  sdarts.client.SDARTSBean class to do this, for the following reasons:
    1. SDLIP takes out the <!DOCTYPE> declaration; the
    SDARTSBean puts it back in. This is helpful for validation.

    2. The SDARTSBean has a very simple interface that works almost
    entirely in Strings. 

    3. Since SDARTS does not support SDLIP's asynchronous or
    ResultAccess interfaces, the methods are themselves synchronous,
    with almost no confusing OUT parameters. (Well, only one.)

    4. SDARTSBean comes in a nice sdartsbean.jar file, which includes
    only the SDARTS, SDLIP and XML support classes needed to run as
    client. You do not need a full SDLIP installation to run a
    SDARTSBean-enabled client.

  All you need to do is copy the sdartsbean.jar file from the /lib 
  subdirectory of an installed server (or from a distribution directory
  where a "make client" has been run.) Put it in your classpath, read
  the online javadoc, and start coding!

  ONE FINAL WARNING: Remember that the <starts:squery>'s that you pass
  as parameters must be namespace-qualified. Make sure to always use
  the STARTS namespace. So, for example, your query would begin with:

  <starts:squery xmlns:starts="http://www.cs.columbia.edu/~dli2test/STARTS/">

  NOTE THE '/' AT THE END OF THE STARTS NAME!! There is a bug in SDLIP
  that will make the transport layer fail if you do not include this!!

8. LIBRARIES INCLUDED IN THE DISTRIBUTION
-----------------------------------------

  The sdarts-server.jar file contains the necessary SDARTS classes
  to run the SDARTS server. Additionally it contains:

  * SDLIP 1.2 (with Microstar XML parser)
  * GNU regexp 1.0.8
  * Xalan J 1.2.2
  * Xerces 1.4.1
  * JTidy 04aug2000r6
  * Lucene 1.00


9. TROUBLESHOOTING
------------------

  Please read the documentation available online. If you cannot figure
  out what is the problem, then send an email to <pirot@cs.columbia.edu>
