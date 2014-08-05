#!/bin/sh
# there's a Grails bug that causes Grails to not get new SNAPSHOT
# artifacts, so they need to be deleted manually...

rm -r ~/.grails/ivy-cache/org.languagetool/
#grails clean
