<?xml version="1.0" encoding="iso-8859-1"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xf="http://codetab.org/xfields"
    exclude-result-prefixes="xf">

    <xsl:output method="xml" indent="yes" />
    <xsl:strip-space elements="*" />

    <!-- Identity transform -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
    </xsl:template>

     <!-- don't output global steps -->
    <xsl:template match="xf:fields/xf:steps" />

    <!-- merge global steps with local steps using stepsRef attribute 
         if local step is defined then matching global step is ignored -->

    <xsl:template match="xf:fields/xf:tasks/xf:task/xf:steps">
        <xsl:variable name="stepsRef" select="@ref" />
        <xsl:variable name="taskName" select="parent::xf:task/@name" />
        <xsl:variable name="tasksGroup" select="ancestor::xf:tasks/@group" />

        <steps name="{$stepsRef}" xmlns="http://codetab.org/xfields">         
            
            <!--  copy all local steps as they have precedence over global steps -->

            <xsl:for-each select="xf:step">

                <xsl:copy-of select="." />

            </xsl:for-each>

            <!-- selectively copy global step if it is not defined at local level  -->

            <xsl:for-each
                select="ancestor::xf:fields/child::xf:steps[@name=$stepsRef]/xf:step">

                <xsl:variable name="globalStepName" select="@name" />

                <xsl:variable name="matchingLocalStepCount"
                    select="count(ancestor::xf:fields/child::xf:tasks[@group=$tasksGroup]/xf:task[@name=$taskName]/xf:steps/xf:step[@name=$globalStepName])" />

                <xsl:if test="$matchingLocalStepCount = 0">
                    <xsl:copy-of select="." />
                </xsl:if>

            </xsl:for-each>

        </steps>

    </xsl:template>

</xsl:stylesheet>
