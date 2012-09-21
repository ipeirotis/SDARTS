When migrating this project to maven there was a difficulty in pinpointing the correct version of the 3rd party jars.
To get a workaround for this problem, we will install the 3rd party libraries, in the 3rdParty folder, into our local repository.
Once all this jars are installed through maven they will be used as dependencies for the sdarts-server module

Steps in install the 3rd party jars
1. Open command line
2. Go into 3rdParty folder of this project
3. Run these command one by one in the command line

mvn install:install-file -Dfile=activation-1.0.jar -DartifactId=activation -DgroupId=activation -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=gnu-regexp-1.1.4.jar -DartifactId=gnu-regexp -DgroupId=gnu-regexp -Dversion=1.1.4 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=htmlparser-1.0.jar -DartifactId=htmlparser -DgroupId=htmlparser -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=jdom-1.0 -DartifactId=jdom -DgroupId=jdom -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=lucene-1.0.jar -DartifactId=lucene -DgroupId=lucene -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=mail-1.0.jar -DartifactId=mail -DgroupId=mail -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=mm.mysql-1.0.jar -DartifactId=mm.mysql -DgroupId=mm.mysql -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=sdlip-2_0.jar -DartifactId=sdlip -DgroupId=sdlip -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=servlet-1.0.jar -DartifactId=servlet -DgroupId=servlet -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=soap-1.1.jar -DartifactId=soap -DgroupId=soap -Dversion=1.1 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=tidy-1.0 -DartifactId=tidy -DgroupId=tidy -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=visualnumerics-1.0.jar -DartifactId=visualnumerics -DgroupId=visualnumerics -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=xerces-1.0.jar -DartifactId=xerces -DgroupId=xerces -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=xml-apis-1.0.jar -DartifactId=xml-apis -DgroupId=xml-apis -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=xmlParserAPIs-1.0.jar -DartifactId=xmlParserAPIs -DgroupId=xmlParserAPIs -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true


4. Once done, run mvn clean install at the root directory of the project
