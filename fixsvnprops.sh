#!/bin/sh

find . -name \*.java -or -name \*.xml -exec svn ps svn:keywords "Revision Id" '{}' \;
find . -name \*.java -or -name \*.xml -exec svn ps svn:eol-style native '{}' \;
