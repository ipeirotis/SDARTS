<?xml version="1.0" encoding="UTF-8"?>
<!-- Transforms XML-ified HTML that has been returned by a web search -->
<!-- engine into a <starts:intermediate>. This <starts:intermediate> -->
<!-- should contain one or more <starts:sqrdocument>s representing -->
<!-- the results, and, if there is a "more" button at the bottom of -->
<!-- the page, a <starts:script> to perform further invocations. -->
<!-- This version is for the PubMed search engine. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:starts="http://sdarts.cs.columbia.edu/STARTS/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:strip-space elements="*"/>
	<!-- All doc_style.xsl files you write ought to have this instruction -->
	<!-- here, in order to make sure that the document output is declared -->
	<!-- as being of type "starts_intermediate.dtd"                       -->
	<xsl:output method="xml"/>
	<xsl:template match="/">
		<xsl:text>&#xa;</xsl:text>
		<xsl:text/>
		<starts:intermediate>
			<xsl:attribute name="xsi:schemaLocation">http://sdarts.cs.columbia.edu/STARTS/ http://sdarts.cs.columbia.edu/xsd/starts_intermediate.xsd</xsl:attribute>
			<xsl:text>&#xa;</xsl:text>
			<xsl:for-each select="//form/table[2]//td/table[@cellspacing='5']">
				<xsl:call-template name="makeDoc"/>
			</xsl:for-each>
			<xsl:variable name="numavailable" select="number(substring-after(//td[@align='center'][@width='50%']/div[@class='medium2']/p/text(),'of '))"/>
			<starts:numavailable>
				<xsl:choose>
					<xsl:when test="$numavailable">
						<xsl:value-of select="$numavailable"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="count(//dl)"/>
					</xsl:otherwise>
				</xsl:choose>
			</starts:numavailable>
			<xsl:text>&#xa;</xsl:text>
		</starts:intermediate>
	</xsl:template>
	<xsl:template name="makeDoc">
		<xsl:text/>
		<starts:sqrdocument>
			<xsl:text>&#xa;</xsl:text>
			<xsl:text/>
			<starts:doc-term>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
				<starts:field name="title"/>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
				<starts:value>
					<xsl:variable name="x" select="normalize-space(.//td[@colspan='2']/p/text())"/>
					<xsl:value-of select="concat('&lt;![CDATA[',$x,']]>')"/>
				</starts:value>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
			</starts:doc-term>
<!--
			<starts:doc-term>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
				<starts:field name="journal"/>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
				<starts:value>
					<xsl:variable name="x" select="normalize-space(.//td[@colspan='2']/p/text()[2])"/>
					<xsl:value-of select="concat('&lt;![CDATA[',$x,']]>')"/>
				</starts:value>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
			</starts:doc-term>
-->
			<starts:doc-term>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
				<starts:field name="author"/>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
				<starts:value>
					<xsl:value-of select="normalize-space(.//td[@width='100%']/a)"/>
				</starts:value>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
			</starts:doc-term>
			<xsl:text>&#xa;</xsl:text>
			<xsl:text/>
			<starts:doc-term>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
				<starts:field name="linkage"/>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
				<starts:value>
					<xsl:variable name="x" select="normalize-space(.//td[@width='100%']/a/@href)"/>
					<xsl:value-of select="concat('&lt;![CDATA[',$x,']]>')"/>
				</starts:value>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text/>
			</starts:doc-term>
			<xsl:text>&#xa;</xsl:text>
			<xsl:text/>
		</starts:sqrdocument>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>
</xsl:stylesheet>
