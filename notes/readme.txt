
locator - group, url, symbol, cache, datadef, handler

Config Files
------------

fixed             - bean.xml, package.jdo
user configurable - datadef.xml, locator.xml, afields.xml, picks-default.xml
                    picks.properties, logback.xml, jdoconfig.xml

Three sets of config files at following locations
dev - src/main/resources  and also sub dir like simple, mc etc. set the sub
                          dir in picks.properties and beans.xml
test - src/test/resources
prod - src/prod/resources  

while packaging the app as jar (mvn package), 
  - exclude src/main/resources i.e. dev 
for app distribution, 
  - use maven assemble plugin to copy src/prod/resources to target/config dir 
  - zip it with app jar with dependencies. User can modify config
    files. Config dir and jar are to be added to classpath while running the app    


database setup
--------------

hsqldb is used for test mode. 

In Fedora, install hsqldb with

# dnf install hsqldb

It installs hsqldb at /usr/lib/hsqldb and  /var/lib/hsqldb 

config files are - /var/lib/hsqldb/server.properties and /etc/sysconfig/hsqldb

to create picks db instances, add following lines in /var/lib/hsqldb/server.properties

server.database.1   file:data/db1               
server.dbname.1     picks-dev
server.database.2   file:data/db2               
server.dbname.2     picks-test

start hsqldb with systemctl

# systemctl daemon-reload    ## if new installation
# systemctl start hsqldb.service
# systemctl enable hsqldb.service

data files are created at /var/lib/hsqldb/data. 

For more info, ref HsqlDB doc chapters 
    - HyperSQL Network Listeners (Servers) 
    - HyperSQL on UNIX
  
to enable hsqldb client
    add new external tool configuration 
    Program->New Launch Configration
    Main->Location     /usr/bin/java
    Main->Arguments    -jar /usr/share/java/hsqldb.jar
    disable - Refresh, Build->build before launch, Common->allocate console
    enable - Common->Display in Favorites
  

maven build
-----------

Add new run configuration

   Main->Base directory - ${project_loc:gotz}
   Goals-> process-classes exec:java -Dexec.mainClass="org.codetab.gotz.Gotz" -Dexec.cleanupDaemonThreads=false -Dpicks.mode=dev 
   
test run 
   mvn test  # enhance and test

dev run 
   mvn exec:java -Dexec.mainClass="in.m.picks.Picks" -Dpicks.mode=dev
   
prod run 
   mvn exec:java -Dexec.mainClass="in.m.picks.Picks" 

tests and IT tests
   mvn verify
   
skip tests and run integration tests 
   mvn integration-test -Dtest=zzz.java -DfailIfNoTests=false

javadoc
   mvn javadoc:javadoc
   
jacoco report
   mvn clean test jacoco:report     - coverage excludes IT tests. 
   mvn clean verify                 - coverage excludes IT tests.    
   mvn clean verify jacoco:report   - coverage includes IT tests.

know dependency updates
   mvn versions:display-dependency-updates
   
  
M2E 
---

Link javadoc and sources - Project Context Menu->Maven->Download Javadoc


Eclipse setup 
-------------

attach Java javadoc
install openjdk javadoc rpm, list alternatives and look for javadocdir  
# alternatives --list
javadocdir auto /usr/share/javadoc/java-1.8.0-openjdk-1.8.0.121-8.b14.fc24/api

Preferences -> Installed JRE -> OpenJdk x.x.x -> Edit -> select rt.jar -> 
Javadoc Location - enter Javadoc URL as file:///etc/alternatives/javadocdir

Static import - Preference -> Java -> Editor -> Content Assist -> Favorites
and add New Types
  org.mockito.Mockito
  org.mockito.Matchers
  org.mockito.BDDMockito
  org.assertj.core.api.Assertions

Change author name - edit eclipse.ini and add
-Duser.name=Maithilish

    
Schema Generation
-----------------

run src/main/scripts/schemagen.sh from project base dir and it will generates schema1.xsd
and places it in src/main/resources/schema dir. Beans are validated against the schema.
Before packaging run the script
  
Generate Model classes from schema
----------------------------------

xjc -Xinject-code -extension                \
    -p in.m.picks.model                     \
    -b src/main/resources/schema/picks.xjb  \
    -d src/main/java                        \
    src/main/resources/schema/picks.xsd


Integration Tests
-----------------

mvn clean integration-test -Dtest=zzz.java -DfailIfNoTests=false

this ensures 
 - all tests are compiled
 - test resources are copied
 - unit tests are skipped  (as no such file named zzz.java)
 - build is not failed because of failure of unit tests 
 - *IT.java tests are run 


Eclipse CheckStyle
------------------

go to preferences - checkstyle and slide to right side end, click New and enter
 - type - External Configuration
 - name - Gotz Checks
 - location - /orange/data/workspace/gotz/src/main/resources/checkstyle/gotz_checks.xml

this will create new config and select it in project checkstyle setup. Next, 
edit and modify Gotz Checks

 - Javadoc comments - disable all modules
 - Class design - disable Design for extension (forces for javadoc for extension) 
 - Class design - disable Final Class 
   (forces class singleton with private constructor to be final and final class 
    can't be mocked)
  - Filters - suppression filter and set file to
   /orange/data/workspace/gotz/src/main/resources/checkstyle/suppressions.xml
   
in project properties select Gotz Checks module and activate cs

for suppressions configure
  
 !! abs path is required relative path not allowed
 !! enable Purge Checkstyle caches button in toolbar, purge cache after 
    any changes to suppressions.xml

Code Style - Formatter
----------------------

For Checkstyle compliant formatter, import workspace/eclipse-prefs/formatter.xml 
which creates Eclipse-cs Gotz formatter profile. This is the preferred method.
Import using Preferences -> Java -> Code Style -> Formatter -> Import and then
in project properties -> Java Code Style -> Formatter, set Active Profile 
to Eclipse-cs Gotz 


Alternatively, to create new Eclipse-cs Gotz formatter profile, select checkstyle 
from project context menu and select Create Formatter-profile. Edit it 
with Preferences -> Java -> Code Style -> Formatter
  - Line wrapping - scroll down to Expressions - assignment and set line wrap policy to
    wrap where necessary
  - Whitespace - Arrays - Initilizers - no after opening and before closing brace    
For XML files, change format options in Preferences -> XML -> XML file -> Editor
  - uncheck format comments
  - indent using spaces - Indention Size 4
  

Know selector or xpath in chrome
-------------------------------

open the page in chrome, select any text, select Inspect from context menu
in Inspector pane, right click on element and select Copy where you
can copy selector and xpath and paste it to editor. In selector or xpath use
single quotes instead of double quotes 


javadoc guidelines
------------------

add /** and press enter to generate javadoc comments.
remove any non-javadoc comments generated by eclipse.
use @throws both for checked and unchecked exception.
for methods that doesn't do anything add do nothing
for overridden method, add javadoc comments.
use @see to link any project classes and also java or external classes and methods.
  -- @see in text creates inline link 
  -- if used after tags (param,return) then added in See also section.  
        
      
    



 
  



    
