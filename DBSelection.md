SDARTS Collection Selection Component INSTALLATION INSTRUCTIONS
-- 2002/06/13

by Jiangcheng Bao (jb605@columbia.edu)

1. INSTALLATION

  a. The Server
	i. To run the collection selection server as a standalone server, goto the directory where the dbselection component is installed, and type:
		java -jar lib/dbselection-complete.jar

		[port]  = optional port number the collection selection server listens at
				  defaults to 9090
				  
	To test the standalone collection selection server from commandline

		telnet <server> <port>
		where,
		<server> = the server hostname this collection selection server runs on
		<port>   = the port the collection selection server listens at
	
	then type some query command or collection add/remove command, for example: query cancer

    ii. To run the collection selection server as a soap service on Tomcat

		deploy DBSelectionServer as a service in soap server by copying dbselection.jar to the soap jars directory, editing soap server configuration to include this new module, export method DBSelectionServer.processInput(String type, String input) and then restart the soap server.

  b. The Client
  
    * To test the collection selection server under soap with smartclient via web, replace the dbselection.util.jar in smartclient classpath by the new jar file created above, then start the smart client, and test it with browser
	
    * To test the collection selection server under soap with sample SoapClient included within this package, run

			testclient.sh query <term to query>
			
2. QUERY THE SERVER FOR SCORES FOR GIVEN TERM

Use any of the above method to test the dbselection server, and provide input

	query <term>
	where,
	<term> = the term to query about
	e.g.,

	query cancer
		
Or, if using soap service, call the service with

	type: query
	input: <term>
	
3. ADDING/REMOVING COLLECTIONS

Use any of the above method to test the dbselection server, and provide input

  add <sdarts server url> [collection [collection ...]]
where,
  <sdarts server url> = the sdarts server that we want to add into this
                        collection server
  [collection [collection ...]]
                      = zero or more space-separated collection name on the
                        given sdarts server, where no collection name means
                        process all collections for this server
e.g.,

  add http://sdarts.cs.columbia.edu:8080/sdarts pubmed
  
To remove, you will do the same as above, except that the command will be remove instead of add.

4. LIBRARIES INCLUDED IN THIS DISTRIBUTION

The dbselection-complete.jar file contains all classes necessary to run the collection selection server, it contains:

 * activation.jar
 * crimson.jar
 * dbselection.jar
 * dbselection.util.jar
 * GNU regexp 1.0.8
 * imap.jar
 * Jdom 0.8 Beta
 * mailapi.jar
 * mail.jar
 * MicroStar XML
 * SDLIP 1.2
 * SDARTS Bean v1.1a
 * SAX
 * soap.jar
 * Xerces 1.4.1