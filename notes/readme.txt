
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
   Goals - compile datanucleus:enhance exec:java -Dexec.mainClass="in.m.picks.Picks" -Dpicks.mode=dev
   
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



 
  



    
