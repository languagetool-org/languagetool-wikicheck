#!/bin/bash
# See http://tools.wmflabs.org/languagetool/
# This script will upload the web app to Tool Labs. Requires admin access to
# the 'languagetool' project at Tool Labs.

pushd ../languagetool
mvn install -DskipTests
pushd

grails war && \
  scp -i ~/.ssh/wikipedia/toollabs target/languagetool-wikicheck-0.1.war tools-login.wmflabs.org:/data/project/languagetool/ && \
  echo "Now call these commands to finish the deployment:" && \
  echo "  ssh your_username@tools-login.wmflabs.org" && \
  echo "  become languagetool" && \
  echo "  ./deploy-wikicheck.sh"
