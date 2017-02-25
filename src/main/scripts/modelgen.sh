PROJ_DIR=/orange/data/workspace/npicks
GENERATED_DIR=target/generated-sources
SCHEMA_DIR=src/main/resources/schema
PACKAGE=in.m.picks.model
MODEL_DIR=in/m/picks/model

cd $PROJ_DIR

rm -rf $GENERATED_DIR
mkdir $GENERATED_DIR

xjc -Xinject-code -extension -p $PACKAGE -b $SCHEMA_DIR/picks.xjb -d $GENERATED_DIR $SCHEMA_DIR/picks.xsd

replace() {
   sed -i "$1" "$GENERATED_DIR/$MODEL_DIR/$2"
   return
}
##### insert iterators

code_point="@Override public boolean equals"
java_file=FieldsBase.java
code="public abstract Iterator<FieldsBase> iterator(); public abstract String getName(); public abstract String getValue();"
replace "s/$code_point/$code $code_point/g" $java_file

java_file=Fields.java
code="@Override public Iterator<FieldsBase> iterator() { return new FieldsIterator(fields); }"
replace "s/$code_point/$code $code_point/g" $java_file 

java_file=Field.java
code="@Override public Iterator<FieldsBase> iterator() { return new NullIterator(); }"
replace "s/$code_point/$code $code_point/g" $java_file 

java_file=DAxis.java
replace "s/List<DMember>/Set<DMember>/g" $java_file

##### insert some imports

code_point="import java.io.Serializable;"
code="import java.util.Iterator;"

java_file=Field.java
sed -i "s/$code_point/$code $code_point/g" $GENERATED_DIR/$MODEL_DIR/$java_file 

java_file=Fields.java
sed -i "s/$code_point/$code $code_point/g" $GENERATED_DIR/$MODEL_DIR/$java_file 

java_file=FieldsBase.java
sed -i "s/$code_point/$code $code_point/g" $GENERATED_DIR/$MODEL_DIR/$java_file 

code="import java.util.Set;"
java_file=DAxis.java
sed -i "s/$code_point/$code $code_point/g" $GENERATED_DIR/$MODEL_DIR/$java_file

code="import java.util.Set;"
java_file=DataDef.java
sed -i "s/$code_point/$code $code_point/g" $GENERATED_DIR/$MODEL_DIR/$java_file


## add excludes to hash and equals methods

code_point='{"id",'
code='{"id", "fromDate", "toDate", "memberSets",'
java_file=DataDef.java
replace "s/$code_point/$code/g" $java_file 


## other replacements

#code_point="protected Date toDate;"
#code="private transient Set<Set<DMember>> memberSets;"
#java_file=DataDef.java
#sed -i "s/$code_point/$code_point $code/g" $GENERATED_DIR/$MODEL_DIR/$java_file

match="@XmlAccessorType(XmlAccessType.FIELD)"
replace="@XmlAccessorType(XmlAccessType.NONE)"
sed -i "s/$match/$replace/g" $GENERATED_DIR/$MODEL_DIR/*.java

## add xmlelement annotation
code="@XmlElement"

code_point="protected Long id;"
replace "s/$code_point/$code $code_point/g" Base.java

code_point="protected List<Bean> bean;"
replace "s/$code_point/$code $code_point/g" Beans.java

code_point="protected List<DAxis> axis;"
replace "s/$code_point/$code $code_point/g" DataDef.java

code_point="protected List<DataDef> datadef;"
replace "s/$code_point/$code $code_point/g" DataDefs.java

code_point="protected Set<DMember> member = new HashSet<DMember>();"
replace "s/$code_point/$code $code_point/g" DAxis.java

code_point="protected DFilter filter;"
replace "s/$code_point/$code $code_point/g" DAxis.java

code_point="protected List<Locators> locators;"
replace "s/$code_point/$code $code_point/g" Locators.java

code_point="protected List<Locator> locator;"
replace "s/$code_point/$code $code_point/g" Locators.java

code_point="protected List<Document> documents;"
replace "s/$code_point/$code $code_point/g" Locator.java

code_point="protected Object documentObject;"
replace "s/$code_point/$code $code_point/g" Document.java
    

echo -e "\n\n"
echo " Model Files Generated !!!"
echo " generated java files are in : $GENERATED_DIR. Copy them to src directory" 
echo " Copy them to src directory with : "
echo "   cp -f $GENERATED_DIR/$MODEL_DIR/* src/main/java/$MODEL_DIR"
echo "   run organize imports and format"
echo "   use regular exp /\*\*(?s:(?!\*/).)*\*/ in find and replace to remove comments"
echo -e "\n\n"

