languagetool-wikicheck
======================

## This project is not maintained anymore


Wikipedia style and grammar checker, powered by LanguageTool. ~~This is the software running at
http://community.languagetool.org/wikiCheck/.~~

It will check pages on requests. For the continuous check of the 'recent changes' feed, it will
only display the results it finds in the database. To fill this database, download
LanguageTool-wikipedia from https://languagetool.org/download/snapshots/ and run this command:

    java -cp mysql-connector.jar:languagetool-wikipedia.jar org.languagetool.dev.wikipedia.atom.AtomFeedCheckerCmd

It will display a more detailed usage message. To write the results to MySQL you will also need to download the JDBC driver from 
http://dev.mysql.com/downloads/connector/j/ (called mysql-connector.jar in the command line example above).
