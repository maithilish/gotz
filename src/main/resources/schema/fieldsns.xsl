<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ns0="http://tempuri.org/">
    <xsl:output omit-xml-declaration="yes" indent="yes" />
    <xsl:strip-space elements="*" />

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="xf:{name()}" namespace="http://codetab.org/xfields">
            <xsl:copy-of select="namespace::*" />
            <xsl:apply-templates select="node()|@*" />
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>