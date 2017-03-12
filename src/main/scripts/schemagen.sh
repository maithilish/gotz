# run this from project base dir
# generates and places schema file in src/main/resources/schema dir
# add all Top Level JAXB annotated java files to schamegen command below

BASE_DIR=.
SRC=$BASE_DIR/src/main/java/org.codetab.gotz/model
RESOURCES=$BASE_DIR/src/main/resources/schema
CLASSES=$BASE_DIR/target/classes
TEMP=$BASE_DIR/temp

# prepare
mkdir -p $TEMP
mkdir -p $RESOURCES

# do fresh compile
mvn clean compile 

# generate
schemagen -d $TEMP -cp $CLASSES $SRC/DataDefs.java $SRC/Locators.java $SRC/Beans.java $SRC/AfieldsList.java

echo -e "\n\n--- Picks SchemaGen ---\n"
rename schema gotz $TEMP/*.xsd
SCHEMAFILES=$(ls $TEMP/*.xsd)
echo "Generated [$SCHEMAFILES]"

mv $TEMP/*.xsd $RESOURCES 
echo "Schema files moved to [$RESOURCES] directory" 

rm -rf $TEMP

echo


