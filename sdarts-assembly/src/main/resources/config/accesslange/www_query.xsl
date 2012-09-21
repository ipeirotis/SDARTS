<?xml version="1.0" encoding="UTF-8"?>
<!-- Transforms a starts:squery into a starts:intermediate, holding a starts:script that can be used -->
<!-- to query the AccessMedicine.com engine -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:starts="http://sdarts.cs.columbia.edu/STARTS/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.w3.org/2001/XMLSchema-instance">

<!-- All doc_style.xsl files you write ought to have this instruction -->
<!-- here, in order to make sure that the document output is declared -->
<!-- as being of type "starts_intermediate.dtd"                       -->
<xsl:output method="xml"/> 
<!--doctype-system="http://sdarts.cs.columbia.edu/dtd/starts_intermediate.dtd"/-->
	
<xsl:template match="/">
	<starts:intermediate>
		<xsl:attribute name="xsi:schemaLocation">http://sdarts.cs.columbia.edu/STARTS/ http://sdarts.cs.columbia.edu/xsd/starts_intermediate.xsd</xsl:attribute>
	<xsl:apply-templates select="starts:squery" />
	</starts:intermediate>
</xsl:template>

<xsl:template match="starts:squery">
	<starts:script>
		<starts:url method="get">
      <xsl:text>http://harrisons.accessmedicine.com/cgi-bin/search_results.cgi</xsl:text>
	</starts:url>


    <starts:variable>
      <starts:name>category</starts:name>
      <starts:value>chapter</starts:value>
    </starts:variable>

    <starts:variable>
      <starts:name>displaycategory</starts:name>
      <starts:value>chapter</starts:value>
    </starts:variable>

    <starts:variable>
      <starts:name>displaypublication</starts:name>
      <starts:value>lange</starts:value>
    </starts:variable>
    
<!-- PANOS, Oct 13, 2003: It is not supported (?)
    <starts:variable>
      <starts:name>dispmax</starts:name>
      <starts:value>
        <xsl:value-of select="/starts:squery/@max-docs"/>
      </starts:value>
    </starts:variable>
-->   

    <starts:variable>
      <starts:name>text1</starts:name>
      <starts:value>
        <xsl:apply-templates select="starts:filter" />
      </starts:value>
    </starts:variable>
  </starts:script>
</xsl:template>


<!-- process starts:filter recursively -->

<!-- 1) filter of type: TERM  -->
<xsl:template match="starts:filter[(count(*) = 1) and ( name(./*[1]) = 'starts:term')]">
  <xsl:apply-templates select="starts:term" />
</xsl:template>

<!-- 2) filter of type: FILTER_BOOLEANOP_FILTER -->
<!-- harrisons online does not support NOT -->
<!-- harrisons online does not support paranthesis (forcing closer relationship -->
<xsl:template match="starts:filter[(count(*) = 3) and (name(./*[1]) = 'starts:filter') and (name(./*[2]) = 'starts:boolean-op') and (name(./*[3]) = 'starts:filter')]">
  <xsl:apply-templates select="./*[1]" />
  <xsl:variable name="opname" select="./*[2]/@name" />
  <xsl:choose>
    <xsl:when test="$opname = 'and'">
      <xsl:text> AND </xsl:text>
    </xsl:when>
    <!-- no NOT support -->
    <xsl:otherwise>
      <xsl:text> OR </xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates select="./*[3]" />
</xsl:template>

<!-- 3) filter of type: TERM_PROXOP_TERM -->
<xsl:template match="starts:filter[(count(*) = 3) and (name(./*[1]) = 'starts:term') and (name(./*[2]) = 'starts:prox-op') and (name(./*[3]) = 'starts:term')]">
<!-- this is not supported , so don't implement -->
</xsl:template>

<xsl:template match="starts:term">
  <xsl:apply-templates select="starts:value"/>
</xsl:template>

<xsl:template match="starts:value">
  <xsl:value-of select="concat(text(), ' ')"/>
</xsl:template>

</xsl:stylesheet>
