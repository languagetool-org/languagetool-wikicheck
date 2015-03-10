#!/bin/bash
# See http://community.languagetool.org/wikiCheck/
# This script will upload the web app to our server. Requires admin access.

pushd ../languagetool
mvn install -DskipTests
pushd

./cleancache.sh
grails --offline war && \
  scp target/languagetool-wikicheck-0.1.war languagetool@community.languagetool.org:/tmp/wikicheck.war && \
  ssh languagetool@community.languagetool.org unzip -d /home/languagetool/tomcat/webapps/wikiCheck /tmp/wikicheck.war && \
  echo "Now call these commands to finish the deployment:" && \
  echo "  ssh languagetool@languagetool.org" && \
  echo "  ./restart-tomcat.sh"
