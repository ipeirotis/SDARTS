SDARTS Server v1.1a INSTALLATION INSTRUCTIONS
-- 2002/07/15

(initial version by Noah Green, modified by [Panos Ipeirotis](mailto:pirot@cs.columbia.edu), Sergey Sigelman and Jiangcheng Bao)

1. INSTALLATION

To install the sdarts server, copy sdarts-server.tar.gz to the destination directory, and then decompress it by running

  gzip -d sdarts-server.tar.gz
  tar -xvf sdarts-server.tar
2. RUNNING SDARTS SERVER

 * To start the server, go to the directory where sdarts is installed and type:

   java -jar lib/sdarts-server-complete.jar

   PS: If you want to run the server on the background, use "javaw" instead of "java"

 * The server at this point has the no subcollections, but with the following subcollections included in the configuration file but commentted out.
     aides
     20groups
     pubmed
     cardio
     noah
     harrisons
3. TESTING SDARTS SERVER USING TESTCLIENT

To check that your server is running, you can use our web-accessible SDARTS client at

  http://sdarts.cs.columbia.edu:8181/
and enter the address of your SDARTS server. You should be able to connect, but no subcollections will be available at this moment.

There is also a handy commandline tool for testing the sdarts server. See Section 5 -- TESTING SDARTS SERVER of the readme.txt file in the SDARTS server distribution for more details.

4. FOR MORE INFORMATION

Please read the readme.txt in the SDARTS server distribution for more details.