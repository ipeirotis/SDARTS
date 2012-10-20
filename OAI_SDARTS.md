OAI SDARTS Cooperative Suite

Overview

The suite consists of three web applications designed to work in cooperation with the SDARTS Server. The first application, OAISTART, harvests metadata records from OAI compliant sources and makes them available through the SDARTS search interface. The OAISERVER and OAISDARTS work in combination to provide access using the OAI protocol to the to the local text and xml collections available through the SDARTS server. The OAISERVER application populates a relational database of metadata records for the collections supported by the SDARTS. The OAISDARTS application is an implementation of the ALCME OAICat project http://alcme.oclc.org/oaicat/index.html which is used make those metadata records available through an OAI compliant interface. What we have achieved in this exercise is to provide the simplicity of OAI harvesting with the sophistication of SDARTS searching.

Installation: All of the applications are J2EE compliant. They can be installed on any J2EE compliant application server. We provide a war file to be installed and describe below what application variables or property files need to set.

OAISTART application

This application harvests metadata from selected OAI compliant sources and stores the records to be accessed by SDARTS as local XML collections. The collection can restricted by date or set membership. For instance Source 1 set 1 could be created as a separate collection from Source 1 set 2 or the entirety of Source 1 could be a collection.

Once added and the SDARTS Server restarted the collection is searchable by the SDARTS server.

Installation. Only two application context variables need to set. The first "defaultconfigpath" should be the absolute path to the configuration subdirectory used by your SDARTS server. The second "defaultcollectionpath" is the absolute path to the subdirectory storing your SDARTS collections Also copy the file oaidcstarts.xsl from the installtion directory to the SDARTS configuration directory.

OAISDARTS application.

This is an implementation of the ALCME:OAICat OAI Compliant Server. See the ALCME:OAICat documentation for a description of their work. In this section I will highlight the changes we made. We used ALCME:OAICat to publish all of the local records behind the SDARTS Server in an OAI compliant format. The required database is populated and maintained by the OAIServer application.

In addition to the required Dublin Core metadata each record also contains basic SDARTS metadata. Each of the SDARTS collections is mapped to an OAI set. The truly unique offering is that a SDARTS content summary is published for each set. The URL for the published content summaries is available through the use of the OAI Identify verb. This is done without adversely impacting our OAI compliance.

Installation. The application requires one application variable "properties". This should include the path and filename for an oaicat.properties file. This file includes the information below. The information in bold is unique to this implementation. See the ALCME OAICat documentation for a discussion of all of the items.

AbstractCatalog.oaiCatalogClassName=ORG.oclc.oai.catalog.SDARTSOAICatalog

AbstractCatalog.recordFactoryClassName=ORG.oclc.oai.catalog.XMLRecordFactory

AbstractCatalog.harvestable=true

#DummyOAICatalog.maxListSize=100

SDARTSOAICatalog.maxListSize=100

# tokenTimeToLive should be in minutes

SDARTSOAICatalog.tokenTimeToLive=60

Identify.repositoryName=SDARTS OAI Demonstration 

Identify.protocolVersion=1.1

Identify.adminEmail=mailto:ss1792@cs.columbia.edu

Identify.repositoryIdentifier=cusdarts

Identify.sampleIdentifier=oai:cusdarts:100

# List the supported schemaLocations

OAIFormats.oai_dc=ORG.oclc.oai.oaiFormat.XML3oai_dc

OAIFormats.oai_st=ORG.oclc.oai.oaiFormat.XML2oai_st

#SDARTSOAI Unique

#The drive required by JDBC to access the oairecords database

#in this case the mysql driver

SDARTSOAICatalog.dbDriver=org.gjt.mm.mysql.Driver

#The URL required by JDBC to access the oairecords database

SDARTSOAICatalog.dbURL=
    jdbc:mysql://localhost:3306/oairecords?user=harvester&password=some_pass

#Configuration path used by the SDARTS server

SDARTSOAICatalog.configPath=e:/documents/sdarts/config/
Only two changes were made from the expected implemention of the ALCME OAICat. The frist was to modify the Identify verb object to permit the publishing of the SDARTS Content Summaries. The second is that records are always published with both forms of metadata. The basic implementation permits the return of Dublin Core data exclusively. OAIServer provides the management functions for the underlying database.

OAIServer application

This application is an administrative module. It permits the user to select from the SDARTS local collections should be made available through the OAISDARTS application, which is an OAI compliant document server. OAIServer scans the selected collection and updates the oairecords database with the information required. Actions performed by OAIServer include:

 * Creating a unique OAI compliant identifier for each document
 * Creating an OAI Set entry for each collection
 * Copying and renaming the SDARTS Content Summary to a subdirectory to be available for download

Installation. The application requires one application variable "properties". This should include the path and filename for a .properties file including the following information:

#Absolute path to the SDARTS collection subdirectory

defaultcollectionpath=e:/documents/sdarts/collections/

#Absolute path to the SDARTS configuration subdirectory

defaultconfigpath=e:/documents/sdarts/config/

#Absolute Path to where the Content summaries are published

defaultcontentsummarypath=e:/documents/summary/

#URL to access those published content summary

contentsummaryURL=http://www.cs.columbia.edu/~dli2test/content_summary/

#The URL required by JDBC to access the oairecords database

databaseurl=jdbc:mysql://localhost:3306/oairecords?user=oaiadmin&password=some_password

#The drive required by JDBC to access the oairecords database

#in this case the mysql driver

dbdriver=org.gjt.mm.mysql.Driver

#The repository id used to create the OAI unique identifiers

repositoryId=cusdarts
In addition a relational database will need to created See [The oairecords database](http://sdarts.cs.columbia.edu/documentation/oai-databasecreation.html).