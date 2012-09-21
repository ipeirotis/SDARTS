SDARTS Automatic Content Summary Extraction
-------------------------------------------

Overview

The SDARTS Automatic Content Summary Extraction application permits the creation of estimated content summaries for SDARTS remote web sources (see paper). The application J2EE compliant. It can be installed on any J2EE compliant application server. We provide a web application archive file("war" file) to be installed as described below. R, a language and environment for statistical computing and graphics, is required, but not provided.We use documents sampling approach employing a small number of short topically focused query probes. These probes are contained in the hierarchy directory included in the distribution. This application performs the sampling normalizes the results and places an SDARTS content summary in the appropriate configuration directory of the SDARTS server.

Installation

The distribution

([.tar.gz](http://sdarts.cs.columbia.edu/download/SDARTS_csextraction.tar.gz)) ([.zip](http://sdarts.cs.columbia.edu/download/SDARTS_csextraction.zip))

The distribution includes consists of a directory csextraction containing:

    1. csextraction.war. A web application archive file. The application is J2EE compliant and can be installed on any J2EE compliant application server. In addition to the application code, the war file also includes all java libraries, html and jsp pages required for the application. It also includes a csextraction.properties file which will need to updated as described below for your installation.
    2. src directory. This directory includes all of the application source code.
    3. working directory. This directory hold both batch files for Windows systems and script files for Unix/Linux requires by the application. In addition the working directory has 3 subdirectories. The working/profiles directory will hold profiles resulting from using a single classifier. In addition it will hold a profile which is a combination of those profiles. The working/normalizedWebProfiles will hold a normalized version of the combined profile for a site. The working/summary directory will hold the SDARTS compliant content summary.
    4. hierarchy directory. This directory contains the file hierarchy-svm.txt and a subdirectory named classifiers. The classifier subdirectory contains the .hyp files which consist of a collection of probes and related categories. hierarchy-svm.txt contains a listing of the .hyp files.

Installation Instructions

    1. Install the R software. [See the R project website](http://www.r-project.org) for instructions.
    2. Download and decompress this distribution ( [.tar.gz](http://sdarts.cs.columbia.edu/download/SDARTS_csextraction.tar.gz)) ([.zip](http://sdarts.cs.columbia.edu/download/SDARTS_csextraction.zip))
    3. The csextraction/working and csextraction/hierarchy directories can be placed anywhere accessible to the application server.
    4. Deploy in accordance with the instructions for your application server the csextraction.war file.

        In the root directory for the deployed application you will find the csextraction.properties file. Edit the file to reflect the locations of the various directories

        #This contains the configuration parameters for the Automatic Content Summary Extraction

        All of the paths should be absolute.

        #The path to the SDARTS server configuration directory

        defaultconfigpath=e:/documents/sdarts/config/

        #The path where this application will store intermediate profiles

        #and find the patch file to run R-Project

        workingpath = E:/Documents/working/

        #The url for the query interface of the SDARTS server

        sdartsurl=http://db.cs.columbia.edu:8080/sdarts

        Include not only the path but the hierarchy file name. This permits you to incorporate alternative classifiers

        #The full path to the hierarchy file The individual classifier files should be in

        #a directory named classifiers rooted in the same directory as the hierarchy file

        hierarchyfile =E:/Documents/hierarchy/hierarchy-svm.txt

        The doc sample size indicates the number of articles retrieved. We have achieved good results with 4. Increasing this number increases memory consumption as well as time required to create a summary.

        #The number of article retrieved and fully analyzed for each query in the extraction process

        docsamplesize=4

        If true the application executes the .bat files to run R-project software otherwise it uses the .sh files in the working directory.

        #indicates whether the system is Windows(true). False indicates linux/unix

        iswindows=true
    5. The application has only one application variable. Set the properties variable to the absolute filename of csextraction.properties. This can be done in accordance with your application server's documentation.

    6. Edit the .bat or .sh files depending upon your system to reflect your installation. Set the correct path for the R executable and the absolute path the R-steps.text file. Both the runR file and runR2 file need to be updated. The only difference is that the runR2 refer to R-steps2.txt and runR refers to R-steps.txt

    7. Edit both of the R-step.txt and R-step2.txt file. The reference to the file working/ranked.data.text must have the absolute path to the working directory. Remember if using a windows system and using backslashes as a delimiter you must use 2(\\).

    8. The application can be very memory intensive. The upper limit on the java virtual machine should be set in excess of 256 megabytes. We use 512 megabytes in our demonstration application. Check with your application server documentation as to how to make the change.

After restarting your server the application should be ready to use.
