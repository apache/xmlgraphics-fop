This set of files is meant to illustrate the interpretation
of the spec as it relates to the following FO's: fo:region-*,
fo:static-content, fo:page-sequence, and fo:flow, as well as
all other pagination FO's in general.

These files should _not_ process properly.

Descriptions:

1. unique_region_names_in_pm.fo

According to Section 7.33.15, identifiers used as the value of
'region-name' must be unique within a simple page master. This file
assigns the same name to 2 regions in the same SPM.

2. missing_sc_flowname.fo, missing_flow_flowname.fo

According to Section 7.33.5, the 'flow-name' must be provided for
each fo:flow and each fo:static-content. A processor _may_
continue processng after reporting this error; FOP currently
does not.

These files illustrate the consequences of omitting the 'flow-name'
property.

3. bad_region_name.fo

According to Section 7.33.15, there are reserved words (that are also
defaults) for region-names. If you use one, assign it to the correct
region. FOP stops after reporting this error.

4. unmapped_flow_name.fo, duplicate_flow_name.fo

According to Section 7.33.5, flow names must be assigned, and must be
unique. A duplicate flow name is an error; a processor may continue,
but FOP doesn't.

An unmapped flow-name is not an error, but a warning is generated. You
likely didn't want this. In this example FOP will run to completion,
but you'll be missing the page number on the first page.

5. regnames_not_same_class.fo

According to Section 7.33.15, re-use of region-names, including the
defaults, demands that duplicate region-names be assigned to the same
class of region. When you use the defaults and have multiple page
masters, this happens implicitly.

FOP throws an exception if the same region-name is used for one class
of region in one page-master, and another class in another page-master.


