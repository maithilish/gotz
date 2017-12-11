    
PROJ_DIR=/orange/data/eclipse-workspace/gotz
GENERATED_DIR=target/generated-sources/xjc
SCHEMA_DIR=src/main/resources/schema
PACKAGE=org.codetab.gotz.model
MODEL_DIR=org/codetab/gotz/model
    
main(){
    
    cd $PROJ_DIR
    
    rm -rf $GENERATED_DIR
    mvn jaxb2:generate
    
    # remove unwanted classes. we need these types in xsd for schema validation, but for no other purpose
    rm $GENERATED_DIR/$MODEL_DIR/Beans.java
    rm $GENERATED_DIR/$MODEL_DIR/DataDefs.java
    rm $GENERATED_DIR/$MODEL_DIR/LocatorGroups.java
    rm $GENERATED_DIR/$MODEL_DIR/XFields.java
    
    file=Fields.java
    remove_equals $file
    remove_hashCode $file
    remove_toString $file
    
    file=DAxis.java
    import_set $file
    
    file=DataDef.java
    import_set $file
    add_dates_to_excludes $file 
    remove_toString $file
    
    file=Document.java
    remove_toString $file
    
    file=Locator.java
    remove_toString $file
    
    file=DAxis.java
    replace_list_with_set $file
    
    
    # apply functions to all files
    
    cd $GENERATED_DIR/$MODEL_DIR
    files=$(ls *.java)
    cd $PROJ_DIR
    
    for file in $files 
    do
        replace_protected_with_private $file
        remove_trailing_spaces $file
        rectify_xfield $file ## ref creates extra XmlElement (type creates one)
    done
    
}

success(){    
    echo -e "\n\n"
    echo " model Files generated in : $GENERATED_DIR"
    echo
    echo " to copy use : "
    echo
    echo "      cp -f $GENERATED_DIR/$MODEL_DIR/* src/main/java/$MODEL_DIR"
    echo
    echo " run organize imports and format, to strip comments use regex /\*\*(?s:(?!\*/).)*\*/ in find and replace"
    echo -e "\n\n"
}

replace() {
   sed -i "$1" "$GENERATED_DIR/$MODEL_DIR/$2"
   return
}

multiline_replace(){
   expr="s/$1/$2/"
   perl -0777 -i -pe "$expr" "$GENERATED_DIR/$MODEL_DIR/$3"
   return
}


import_iterator(){
   code_point="import java.io.Serializable;"
   code="import java.util.Iterator;"
   replace "s/$code_point/$code $code_point/g" $1
}

import_set(){
   code_point="import java.io.Serializable;"
   code="import java.util.Set;"
   replace "s/$code_point/$code $code_point/g" $1
}

add_dates_to_excludes(){
   code_point='{ "id",'
   code='{ "id", "fromDate", "toDate",'
   replace "s/$code_point/$code/g" $1
}

replace_list_with_set(){
   replace "s/List<DMember>/Set<DMember>/g" $1
}

replace_protected_with_private(){
   replace "s/protected/private/g" $1
}

remove_trailing_spaces(){
   replace "s/ *$//g" $1
}


# !!!  spaces in match and replace variables are indent spaces so don't edit them !!!

# remove generated toString method

remove_toString() {

match=$(cat <<-END
    \@Override
    public String toString\(\) \{
        return ToStringBuilder.reflectionToString\(this, ToStringStyle.MULTI_LINE_STYLE\);
    \}
END
)

  replace=''

  multiline_replace "$match" "$replace" $1

}

# remove generated equals method

remove_equals() {

    match=$(cat <<-END
    \@Override
    public boolean equals\(final Object obj\) \{
        String\[\] excludes = \{ "id", "dnDetachedState", "dnFlags", "dnStateManager" \};
        return EqualsBuilder.reflectionEquals\(this, obj, excludes\);
    \}
END
)

  replace=''

  multiline_replace "$match" "$replace" $1

}


# remove generated hashCode method

remove_hashCode() {

    match=$(cat <<-END
    \@Override
    public int hashCode\(\) \{
        String\[\] excludes = \{ "id", "dnDetachedState", "dnFlags", "dnStateManager" \};
        return HashCodeBuilder.reflectionHashCode\(this, excludes\);
    \}
END
)

  replace=''

  multiline_replace "$match" "$replace" $1

}

# remove extra XmlElement annotation from Fields

rectify_xfield(){

    match=$(cat <<-END
    \@XmlElement\(namespace = "http:\/\/codetab.org\/xfields"\)
    \@XmlElement
    private Fields fields;
END
)

    replace=$(cat <<-END
    \@XmlElement\(namespace = "http:\/\/codetab.org\/xfields"\)
    private Fields fields;
END
)

  multiline_replace "$match" "$replace" $1

}


## finally call main 

main
success
