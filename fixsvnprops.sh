#!/bin/sh

find . -name \*.java -exec svn ps svn:keywords "Id" '{}' \;
find . -name \*.xml -exec svn ps svn:keywords "Revision Id" '{}' \;
find . -name \*.java -exec svn ps svn:eol-style native '{}' \;
find . -name \*.xml -exec svn ps svn:eol-style native '{}' \;

