<?xml version="1.0" encoding="UTF-8"?>
<!-- Transforms XML-ified HTML that has been returned by a web search -->
<!-- engine into a <starts:intermediate>. This <starts:intermediate> -->
<!-- should contain one or more <starts:sqrdocument>s representing -->
<!-- the results, and, if there is a "more" button at the bottom of -->
<!-- the page, a <starts:script> to perform further invocations. -->

<!-- This version is for the Cardiovascular Institute of the South search 
engine. -->
<!-- There are no "more" buttons on the results returned from the site, -->
<!-- so this stylesheet never creates a <starts:script> -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:starts="http://sdarts.cs.columbia.edu/STARTS/"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:strip-space elements="*"/>

	<xsl:variable name="linkage-prefix" 
select="'http://www.cardio.com'"/>

<!-- All doc_style.xsl files you write ought to have this instruction -->
<!-- here, in order to make sure that the document output is declared -->
<!-- as being of type "starts_intermediate.dtd"                       -->
	<xsl:output method="xml"/> <!--doctype-system="http://sdarts.cs.columbia.edu/dtd/starts_intermediate.dtd"/-->
<!--
<starts:intermediate>
<starts:numavailable>1</starts:numavailable>
<starts:sqrdocument>
<starts:rawscore>0.5</starts:rawscore>
            <starts:doc-term>
                <starts:field name="title"/>
                <starts:value>The title</starts:value>
                </starts:doc-term>
                <starts:doc-term>
                <starts:field name="linkage"/>
                <starts:value>http://here.com</starts:value>
                </starts:doc-term>
</starts:sqrdocument>
</starts:intermediate>
-->

	<xsl:template match="/">
            <xsl:text>&#xa;</xsl:text><xsl:text> </xsl:text>
		<starts:intermediate>
			<xsl:attribute name="xsi:schemaLocation">http://sdarts.cs.columbia.edu/STARTS/ http://sdarts.cs.columbia.edu/xsd/starts_intermediate.xsd</xsl:attribute>
		<xsl:text>&#xa;</xsl:text>
                <xsl:if test="not(//b[text()='Error:'])">
		<xsl:for-each select="html/body/table/tr/td[2]/table/tr[2]/td/p[2]/a"> 
			<xsl:call-template name="makeDoc"/>
		</xsl:for-each>
                </xsl:if>

             <xsl:variable name="numavailable" select="count(html/body/table/tr/td[2]/table/tr[2]/td/p[2]/a)"/>
             <xsl:if test="$numavailable">
              <starts:numavailable><xsl:value-of select="$numavailable"/></starts:numavailable>
              <xsl:text>&#xa;</xsl:text>
              </xsl:if>
		</starts:intermediate>
	</xsl:template>	
	
	<xsl:template name="makeDoc">
		<xsl:text> </xsl:text>
		<starts:sqrdocument>
		<xsl:text>&#xa;</xsl:text><xsl:text>  </xsl:text>
		<starts:rawscore>
            <xsl:variable name="myPos" select="position()"/>
            <xsl:variable name="x" select="normalize-space(../text()[$myPos])"/>
            <xsl:value-of select="number(substring-before($x,'%')) div 100"/>
		</starts:rawscore>
		<xsl:text>&#xa;</xsl:text><xsl:text>  </xsl:text>
		<starts:doc-term>
		<xsl:text>&#xa;</xsl:text><xsl:text>   </xsl:text>
		<starts:field name="title"/>
            <xsl:text>&#xa;</xsl:text><xsl:text>   </xsl:text>
		<starts:value>
		<xsl:value-of select="normalize-space(.)"/>
		</starts:value>
		<xsl:text>&#xa;</xsl:text><xsl:text>  </xsl:text>
		</starts:doc-term>
		<xsl:text>&#xa;</xsl:text><xsl:text>  </xsl:text>
		<starts:doc-term>
		<xsl:text>&#xa;</xsl:text><xsl:text>   </xsl:text>
		<starts:field name="linkage"/>
            <xsl:text>&#xa;</xsl:text><xsl:text>   </xsl:text>
		<starts:value>
		<xsl:value-of select="normalize-space(concat($linkage-prefix,./@href))"/>
		</starts:value>
		<xsl:text>&#xa;</xsl:text><xsl:text>  </xsl:text>
		</starts:doc-term>
	      <xsl:text>&#xa;</xsl:text><xsl:text> </xsl:text>
		</starts:sqrdocument>
		<xsl:text>&#xa;</xsl:text>
      </xsl:template>
</xsl:stylesheet>
