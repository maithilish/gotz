
Eclipse setup 
-------------

install eclipse-cs and ecl-emma
import preferences  !!! only after cs and emma are installed

attach Java javadoc and source

# dnf install java-1.8.0-openjdk-javadoc
# dnf install java-1.8.0-openjdk-src

use alternatives to find location of javadoc  
# alternatives --list
javadocdir auto /usr/share/javadoc/java-1.8.0-openjdk-1.8.0.xxx/api

source src.zip is installed under /usr/lib/jvm/jdk<xxx>/  

Preferences -> Installed JRE -> OpenJdk x.x.x -> Edit -> select rt.jar ->
 
Javadoc Location - enter Javadoc URL as file:///etc/alternatives/javadocdir

Source Attachment - external location -> path as /usr/lib/jvm/jdk1.8.0_xxx/src.zip

add imports

Static import - Preference -> Java -> Editor -> Content Assist -> Favorites
and add New Types
  org.mockito.Mockito
  org.mockito.Matchers
  org.mockito.BDDMockito
  org.assertj.core.api.Assertions

Change author name - edit eclipse.ini and add
-Duser.name=Maithilish

M2E 
---

Download javadoc and sources - Project Context Menu-> Maven->Download Javadoc
or
mvn dependency:sources dependency:resolve -Dclassifier=javadoc

if doc location not set error is thrown then
Build Path -> Configure Build Path -> Libraries remove extra entries of M2_REPO 
only JRE and Maven Dependencies is enough. Update Maven.

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

For Checkstyle compliant formatter, import src/mainresources/checkstyle/formatter.xml 
which creates Eclipse-cs Gotz formatter profile. This is the preferred method and 
to do that : Import using Preferences -> Java -> Code Style -> Formatter -> Import 
and then in project properties -> Java Code Style -> Formatter, set Active Profile 
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

Debug 
-----
 
To  allot maximum space to Variables and editors reorganize the Debug perspective.
 
 - if a view is dropped to top bar of area, view will be added to that area
 - if dropped on area then area is resized to accommodate new area.   

drag n drop Debug view to Console view area, to split bottom row into two. 
close Outline view to remove that area 
Finally, drag n drop Variables and Breakpoints view to editor area to split
editor area into two.

customize the perspective to remove toolbar items.

In debugger, you will get line number error, even when line number generation is
enabled in Preferences -> Java -> Compiler option. 
Disable this error in Java -> Debug option.

Debug XML objects - search debug or xml in pocket

DB setup
--------------

hsqldb is used for dev and tests. 

In Fedora, install hsqldb with

# dnf install hsqldb
# dnf install java-1.8.0-openjdk     // for java headless error

installation locations /usr/lib/hsqldb and /var/lib/hsqldb
data files : /var/lib/hsqldb/data
config files : /var/lib/hsqldb/server.properties and /etc/sysconfig/hsqldb

to create gotz DB instances, edit /var/lib/hsqldb/server.properties 
add following lines, leave db0 config as it is. 

server.database.1   file:data/db1
server.dbname.1     gotz-dev
server.database.2   file:data/db2
server.dbname.2     gotz-test

start hsqldb

# systemctl daemon-reload    ## if new installation
# systemctl start hsqldb.service
# systemctl enable hsqldb.service
  
for client, copy hsqldb.desktop to ~/.local/share/applications

!!! if jdbc error in gotz, then compare hsqldb dependency and server version  

maven build
-----------

Add new run configuration

   Main->Base directory - ${project_loc:gotz}
   Goals-> process-classes exec:java -Dexec.mainClass="org.codetab.gotz.Gotz" -Dexec.cleanupDaemonThreads=false -Dpicks.mode=dev 
   
test run 
   mvn test  # enhance and test

dev run 
   mvn exec:java -Dexec.mainClass="org.codetab.gotz.Gotz" -Dpicks.mode=dev
   
prod run 
   mvn exec:java -Dexec.mainClass="org.codetab.gotz.Gotz"

tests and IT tests
   mvn verify
   
skip tests and run integration tests 
   mvn integration-test -Dtest=zzz.java -DfailIfNoTests=false

find selector
   mvn exec:java -Dexec.mainClass="org.codetab.gotz.util.FindSelector" 
       -Dexec.args="fileName 'selector' "
 example
   mvn exec:java -Dexec.mainClass="org.codetab.gotz.util.FindSelector" 
   -Dexec.args="src/main/resources/devdefs/mc/parse-locator-file/itc-quote.html 'div#mktdet_2 div:matchesOwn(^P/E$)' "        
        
javadoc
   mvn javadoc:javadoc
   
jacoco report
   mvn clean test jacoco:report     - coverage excludes IT tests. 
   mvn clean verify                 - coverage excludes IT tests.    
   mvn clean verify jacoco:report   - coverage includes IT tests.

know dependency updates
   mvn versions:display-dependency-updates
   
download javadoc and source

mvn dependency:resolve -Dclassifier=javadoc
mvn dependency:sources   
    
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


Know selector or xpath in chrome
-------------------------------

open the page in chrome, select any text, select Inspect from context menu
in Inspector pane, right click on element and select Copy where you
can copy selector and xpath and paste it to editor. In selector or xpath use
single quotes instead of double quotes 

    
Design notes
------------

As XML schema allows optional elements, JAXB uses wrappers for primitives since
primitives can't be null. XJC generated models will have Long, Integer etc.
Apart from model classes, try to use primitives as far as possible as it we
need not validate method param for primitives.  

validate param for null or illegal argument (not required for private methods).
validate IllegalState for state vars, injected state vars or init (not required
    for private methods).
    
In method signature add only the checked exceptions. Mention unchecked exceptions in 
javadoc.     

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
    
XML namespace
-------------

getNamespaceURI() on document returns null but for jaxb elementNSImpl it returns uri.
better option is to use lookupNamespaceURI() with null for default ns. 

JDO
---

JDODataStoreException - Exception thrown flushing changes to datastore

In case detached object is persisted again and detachable is not true 
for the class  then instead of update jdo inserts new object. 
If this results in constraint violation then jdo throws NullPointerExcetpion.
Set detachable as true in package.jdo for the class. 

Coding Guidelines
-----------------

for string join use Util.join(), for delimited join use String.join()
in steps, for StepRunException don't use getLabeled() as task will handle label,
   for others use getLabeled() to create message which prefixes label
   if message is used only with logger, then use {} and getLabel() 

in steps, if exception is recoverable then catch and handle it otherwise 
   if unrecoverable throw StepRunException (unchecked exception)
in steps, while throwing StepRunException don't log or add activity as 
   tasks' exception handling takes care of it. 
   

Versions
--------

Given a version number MAJOR.MINOR.PATCH, increment the:

    MAJOR version when you make incompatible API changes,
    MINOR version when you add functionality in a backwards-compatible manner, and
    PATCH version when you make backwards-compatible bug fixes.

And yes, 1.0 should be stable. All releases should be stable, unless they're
marked alpha, beta, or RC. 

Use Alphas for known-broken-and-incomplete. 
Betas for known-broken. 
RCs for "try it; you'll probably spot things we missed". 
Anything without one of these should (ideally, of course) be tested, 
  known good, have an up to date manual, etc.
  
Precedence Example: 
1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta 
  < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0.
        
0.9.0-beta
0.9.0-rc.1
0.9.0-rc.2
1.0.0



    