<?xml version="1.0" encoding="UTF-8"?>

<!-- Transforms XML-ified HTML that has been returned by a web search -->
<!-- engine into a <starts:intermediate>. This <starts:intermediate> -->
<!-- should contain one or more <starts:sqrdocument>s representing -->
<!-- the results, and, if there is a "more" button at the bottom of -->
<!-- the page, a <starts:script> to perform further invocations. -->

<!-- This version is for the noah search engine. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:starts="http://sdarts.cs.columbia.edu/STARTS/"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:strip-space elements="*"/>

<!-- All doc_style.xsl files you write ought to have this instruction -->
<!-- here, in order to make sure that the document output is declared -->
<!-- as being of type "starts_intermediate.dtd"                       -->
	<xsl:output method="xml"/> <!--doctype-system="http://sdarts.cs.columbia.edu/dtd/starts_intermediate.dtd"/-->

	
	<xsl:template match="/">
            <xsl:text>&#xa;</xsl:text><xsl:text> </xsl:text>
		<starts:intermediate>
			<xsl:attribute name="xsi:schemaLocation">http://sdarts.cs.columbia.edu/STARTS/ http://sdarts.cs.columbia.edu/xsd/starts_intermediate.xsd</xsl:attribute>
		<xsl:text>&#xa;</xsl:text>
		<xsl:for-each select="//strong"> 
			<xsl:call-template name="makeDoc"/>
		</xsl:for-each>
		
		<xsl:variable name="next" select="//a[img/@src='/htdig/buttonr.gif']"/>
		<xsl:variable name="linkage" select="'http://www.noah-health.org'"/>
		<xsl:if test="count($next) &gt; 0">
		 <starts:script>
		  <starts:url method="get">
<!--		   <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text> -->
<!--		   <xsl:value-of select="$linkage"/> -->
		 <!--  <xsl:value-of select="concat($linkage,'&lt;![CDATA[',$next/@href,']]>')"/> -->
		   <xsl:value-of select="normalize-space(concat($linkage,$next/@href))"/>
<!--		   <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text> -->

		  </starts:url>
		 </starts:script>
		</xsl:if>		

	<xsl:variable name="numavailable" select="number(substring-before(substring-after(normalize-space(//b[1]),'of '),' '))"/>
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
<xsl:value-of select="count(preceding-sibling::*[position() &lt; 5][@src = '/htdig/star.gif']) * 0.25"/>
		</starts:rawscore>
        	<xsl:text>&#xa;</xsl:text><xsl:text>  </xsl:text>
		<starts:doc-term>
		<xsl:text>&#xa;</xsl:text><xsl:text>   </xsl:text>
		<starts:field name="title"/>
                <xsl:text>&#xa;</xsl:text><xsl:text>   </xsl:text>
		<starts:value>
		<xsl:value-of select="concat('&lt;![CDATA[',normalize-space(.),']]&gt;')"/>
		</starts:value>
		<xsl:text>&#xa;</xsl:text><xsl:text>  </xsl:text>
		</starts:doc-term>
                <xsl:text>&#xa;</xsl:text><xsl:text> </xsl:text>
		<starts:doc-term>
		<xsl:text>&#xa;</xsl:text><xsl:text>   </xsl:text>
		<starts:field name="linkage"/>
                <xsl:text>&#xa;</xsl:text><xsl:text>   </xsl:text>
		<starts:value>
	<xsl:variable name="x" select="normalize-space(a/@href)"/>
        <xsl:value-of select="concat('&lt;![CDATA[',$x,']]>')"/>
		</starts:value>
		<xsl:text>&#xa;</xsl:text><xsl:text>  </xsl:text>
		</starts:doc-term>
                <xsl:text>&#xa;</xsl:text><xsl:text> </xsl:text>
		</starts:sqrdocument>
		<xsl:text>&#xa;</xsl:text>
      </xsl:template>

</xsl:stylesheet>
