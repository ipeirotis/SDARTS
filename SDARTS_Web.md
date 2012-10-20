SDARTS Client v2.1 Installation Instructions
-- 2002/6/22

by Jiangcheng Bao (jb605@columbia.edu)

1. INSTALLATION

The SDARTS Client is packaged as a WAR (Web Application Archive) file, which can be deployed as an application under any servlet engine. We will take Tomcat and JRun as example.

To install the sdarts client under tomcat, copy the war file into the $TOMCAT_HOME/webapps directory. When tomcat is started, it automatically unpacks the WAR and creates the application, with the application's name (and context path) being the name of the WAR. And then you modify $TOMCAT_HOME/conf/server.xml to inlcude this newly deployed client.

To install the sdarts client under jrun (assuming JRun is installed with $JRUN_HOME as the root directory for jrun)

  1. create a directory named sdartsclient under <$JRUN_HOME/servers>, for example, sdartsclient, and then create a directory default-app.
  2. copy the war file into default-app directory, uncompressed the war file.
  3. copy the sdartsclient.properties file to $JRUN_HOME/servers/sdartsclient/local.properties, modify local.properties to reflect your actual path on your server.
  4. modify <$JRUN_HOME>/lib/jvms.properties to include this new server
  
Another way to install an new Jrun web application server is to use the JRun web administration interface.

2. TESTING

start a jrun server (eg, name: sdartsclient)

  <$JRUN_HOME>/bin/jrun -start sdartsclient
  
and view the application via a browser.

3. LIBRARIES INCLUDED

 * dbselection.util.jar
 * gnu-regexp-1.0.8.jar
 * jdom-b8.jar
 * sdartsbean.jar
 * sdlip-1_1A.jar
 * servlet.jar
 * soap.jar
 * xalan.jar
 * xerces.jar