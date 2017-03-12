
PROJ_DIR=/orange/data/workspace/npicks
GENERATED_DIR=target/generated-sources/xjc
SCHEMA_DIR=src/main/resources/schema
PACKAGE=org.codetab.gotz.model
MODEL_DIR=org/codetab/gotz/model

cd $PROJ_DIR

rm -rf $GENERATED_DIR
mvn jaxb2:generate

#mkdir $GENERATED_DIR
#xjc -Xinject-code -extension -p $PACKAGE -b $SCHEMA_DIR/gotz.xjb -d $GENERATED_DIR $SCHEMA_DIR/gotz.xsd

replace() {
   sed -i "$1" "$GENERATED_DIR/$MODEL_DIR/$2"
   return
}


# insert imports

code_point="import java.io.Serializable;"
code="import java.util.Iterator;"

java_file=Field.java
replace "s/$code_point/$code $code_point/g" $java_file 

java_file=Fields.java
replace "s/$code_point/$code $code_point/g" $java_file 

java_file=FieldsBase.java
replace "s/$code_point/$code $code_point/g" $java_file 


code_point="import java.io.Serializable;"
code="import java.util.Set;"

java_file=DAxis.java
replace "s/$code_point/$code $code_point/g" $java_file

java_file=DataDef.java
replace "s/$code_point/$code $code_point/g" $java_file


# replaces

# add excludes to hash and equals methods
code_point='{ "id",'
code='{ "id", "fromDate", "toDate",'

java_file=DataDef.java
replace "s/$code_point/$code/g" $java_file 

# change list to set
java_file=DAxis.java
replace "s/List<DMember>/Set<DMember>/g" $java_file

# make members private and remove trailing space
cd $GENERATED_DIR/$MODEL_DIR
java_files=$(ls *.java)
cd $PROJ_DIR

for java_file in $java_files 
do
    replace "s/protected/private/g" $java_file
    # remove trailing space
    replace "s/ *$//g" $java_file
done


echo -e "\n\n"
echo " Model Files Generated !!!"
echo " generated java files are in : $GENERATED_DIR. Copy them to src directory" 
echo " Copy them to src directory with : "
echo "   cp -f $GENERATED_DIR/$MODEL_DIR/* src/main/java/$MODEL_DIR"
echo "   run organize imports and format"
echo "   use regular exp /\*\*(?s:(?!\*/).)*\*/ in find and replace to remove comments"
echo -e "\n\n"

