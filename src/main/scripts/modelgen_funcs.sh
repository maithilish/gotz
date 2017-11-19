
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

# remove extra XmlElement annotation from XField

rectify_xfield(){

    match=$(cat <<-END
    \@XmlElement\(namespace = "http:\/\/codetab.org\/xfield"\)
    \@XmlElement
    private XField xfield;
END
)

    replace=$(cat <<-END
    \@XmlElement\(namespace = \"http:\/\/codetab.org\/xfield\"\)
    private XField xfield;
END
)

  multiline_replace "$match" "$replace" $1

}


