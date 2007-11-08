<xsl:stylesheet
  xmlns:wsdd="http://xml.apache.org/axis/wsdd/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version='1.0'>

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:template match="wsdd:service">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>
