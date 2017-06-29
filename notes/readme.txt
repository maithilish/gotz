
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

   Main->Base directory - ${project_loc:npicks}
   Goals - process-classes exec:java -Dexec.mainClass="in.m.picks.Picks" -Dexec.cleanupDaemonThreads=false -Dpicks.mode=dev 
   
test run 
   mvn test  # enhance and test

dev run 
   mvn exec:java -Dexec.mainClass="in.m.picks.Picks" -Dpicks.mode=dev
   
prod run 
   mvn exec:java -Dexec.mainClass="in.m.picks.Picks" 

  
M2E 
---

Link javadoc and sources 
    Project Context Menu->Maven->Download Javadoc
    
    
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

go to preferences - checkstyle and scroll to right end
click new and enter
 - type - External Configuration
 - name - Gotz Checks
 - location - /orange/data/workspace/gotz/src/main/resources/checkstyle/gotz_checks.xml

this will create new config and select it in project checkstyle setup 
 

(earlier method or to modify Gotz Checks)
copy Sun checks to Gotz Checks and configure Module Groups
 - Javadoc comments - disable all modules
 - Class design - disable Design for extension (forces for javadoc for extension) 
 - Class design - disable Final Class 
   (forces class singleton with private constructor to be final 
     and final class can't be mocked)

in project properties select Gotz Checks module and activate cs

for suppressions configure
 - group filters - suppression filter and set file to
   /orange/data/workspace/gotz/src/main/resources/checkstyle/suppressions.xml
 
 !! abs path is required relative path not allowed
 !! enable Purge Checkstyles caches button in toolbar, purge cache after 
    any changes to suppressions.xml

For formatter, select checkstyle from project context menu and select Formatter profile 
which creates Eclipse-cs Gotz formatter profile

To edit it in preferences -> Java -> Code Style -> Formatter
  - Line wrapping - scroll down to Expressions - assignment and set line wrap policy to
    wrap where necessary
    



 
  



    
