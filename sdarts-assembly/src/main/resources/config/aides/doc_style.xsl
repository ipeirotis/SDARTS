<?xml version="1.0" encoding="UTF-8"?>

<!-- doc_style.xsl -->
<!-- This kind of stylesheet is used by the sdarts.backend.impls.XMLBackEndLSP to help index a local -->
<!-- XML document collection.  This particular version is designed for the "aides" and "testing" sample -->
<!-- collections. The basic concept for all doc_style.xsl sheets is to transform each document to be indexed -->
<!-- into an intermediate form that can be used by the sdarts.backend.impls.XMLDocumentEnum class -->
<!-- to find fields and construct an index.  This intermediate form looks like the following: -->
<!-- <starts:intermediate> -->
<!-- <starts:sqrdocument> -->
<!--		<starts:doc-term> -->
<!--			<starts:field name="title"/> -->
<!--			<starts:value>Design Patterns</starts:value> -->
<!--		</starts:doc-term> -->
<!--		<starts:doc-term> -->
<!--			<starts:field name="author"/> -->
<!--			<starts:value>Erich Gamma, et al</starts:value> -->
<!--		</starts:doc-term> -->
<!--      . . . . . . . . . . .   -->
<!-- </starts:sqrdocument> -->
<!-- </starts:intermediate> -->
<!-- This form follows the "starts_intermediate" format, which is described -->
<!-- in the starts_intermediate.dtd. Note that while the DTD allows a -->
<!--  <starts:intermediate> to hold more than one <starts:sqrdocument>, as -->
<!-- well as a <starts:script>, this stylesheet here must only produce the -->
<!-- the one <starts:sqrdocument> inside the <starts:intermediate>. -->

<!-- The XML is never actually output, but rather transformed by the Xalan processor into a series of SAX events, -->
<!-- which the sdarts.backend.impls.XMLDocumentEnum then responds to -->
<!-- So as you can see by this form, and the stylsheet below, what the stylesheet does is transform an XML -->
<!-- document into this form. -->


<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:starts="http://sdarts.cs.columbia.edu/STARTS/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

<!-- All doc_style.xsl files you write ought to have this instruction -->
<!-- here, in order to make sure that the document output is declared -->
<!-- as being of type "starts_intermediate.dtd"                       -->
<xsl:output method="xml"/><!--doctype-system="http://127.0.0.1/xsd/starts_intermediate.xsd"/-->

	<xsl:template match="/PAPER">
		<starts:intermediate>
			<xsl:attribute name="xsi:schemaLocation">http://sdarts.cs.columbia.edu/STARTS/ http://sdarts.cs.columbia.edu/xsd/starts_intermediate.xsd</xsl:attribute>
		<starts:sqrdocument>
		<xsl:apply-templates/>
		</starts:sqrdocument>
		</starts:intermediate>
	</xsl:template>

	<xsl:template match="TITLE">
		<starts:doc-term>
		  <starts:field name="title"/>
		  <starts:value>
		    <xsl:choose>
			<xsl:when test="./W">
			  <xsl:for-each select="W">
			   <xsl:value-of select="."/>
			   <xsl:text> </xsl:text>
			  </xsl:for-each>
			</xsl:when>
			<xsl:when test="./text()">
		          <xsl:value-of select="."/>
		        </xsl:when>
		    </xsl:choose>
		  </starts:value>
		</starts:doc-term>
	</xsl:template>

	<xsl:template match="AUTHORS">
		<starts:doc-term>
		  <starts:field name="author"/>
		  <starts:value>
		    <xsl:choose>
			<xsl:when test="./AUTHOR">
			  <xsl:for-each select="AUTHOR">
			   <xsl:value-of select="."/>
			   <xsl:text> </xsl:text>
			  </xsl:for-each>
			</xsl:when>
			<xsl:when test="./text()">
		          <xsl:value-of select="."/>
		        </xsl:when>
		    </xsl:choose>
		  </starts:value>
		</starts:doc-term>
	</xsl:template>

	<xsl:template match="BODY">
		<starts:doc-term>
		 <starts:field name="body-of-text"/>
		 <starts:value>
		   <xsl:apply-templates/>
	         </starts:value>
		</starts:doc-term>
	</xsl:template>

	<xsl:template match="DIV">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="HEADER">
		<xsl:choose>
		  <xsl:when test="./W">
		    <xsl:for-each select="W">
		      <xsl:value-of select="."/>
		      <xsl:text> </xsl:text>
		    </xsl:for-each>
		  </xsl:when>
		  <xsl:when test="./text()">
		    <xsl:value-of select="."/>
		  </xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="P">
		<xsl:choose>
		  <xsl:when test="./S">
		    <xsl:for-each select="S">
		     <xsl:apply-templates select="."/>
		    </xsl:for-each>
		  </xsl:when>
		  <xsl:when test="./text()">
		    <xsl:value-of select="."/>
		  </xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="S">
		<xsl:choose>
		  <xsl:when test="./W">
		    <xsl:for-each select="W">
		     <xsl:value-of select="."/>
		     <xsl:text> </xsl:text>
		    </xsl:for-each>
		  </xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="FILENO">
<!-- let the linkage default to filename
		<starts:doc-term>
		 <starts:field name="linkage"/>
		 <starts:value><xsl:text>http://localhost:8180/sdarts/collections/aides/</xsl:text><xsl:value-of select="."/></starts:value>
		</starts:doc-term>
-->
	</xsl:template>

	<xsl:template match="APPEARED">
	<!-- haven't got date working in Lucene yet, so nothing here for now -->
	</xsl:template>

	<xsl:template match="text()">
		<xsl:value-of select="normalize-space()"/>
	</xsl:template>
	<xsl:template match="REFLABEL | CLASSIFICATION | ABSTRACT | REFERENCES"/>
</xsl:stylesheet>




