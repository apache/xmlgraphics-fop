<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- $Id$ -->
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:svg="http://www.w3.org/2000/svg">

  <xsl:template name="poster-layout-master-set">
    <fo:layout-master-set>
      <fo:simple-page-master master-name="A1L" page-height="2 * 29.7cm" page-width="4 * 21cm">
        <fo:region-body margin-top="10cm" margin-bottom="8cm" 
            margin-left="1cm" margin-right="1cm" 
            column-count="4" column-gap="1cm"/>
        <fo:region-before extent="0pt"/>
      </fo:simple-page-master>
      <fo:simple-page-master master-name="A2plusL" page-width="700mm" page-height="500mm">
        <fo:region-body margin-top="7.5cm" margin-bottom="7cm" 
            margin-left="1cm" margin-right="1cm" 
            column-count="4" column-gap="1cm"/>
        <fo:region-before extent="0pt"/>
      </fo:simple-page-master>
    </fo:layout-master-set>
 </xsl:template>

</xsl:stylesheet>