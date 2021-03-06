<%@page import="org.languagetool.*" %>
<%@page import="org.languagetool.tools.StringTools" %>
<%@page import="org.hibernate.*" %>

<html>
<head>
    <title><g:message code="ltc.wikicheck.title"/></title>
    <meta name="layout" content="main" />
</head>
<body>

<div class="body">

    <g:render template="/languageSelection"/>

    <p style="margin-bottom: 10px">
        <a href="http://community.languagetool.org/">&lt;&lt;</a>
        <a href="http://community.languagetool.org/"><g:message code="ltc.wiki.backlink"/></a>
    </p>

    <p><g:message code="ltc.wiki.intro"/></p>

    <div class="dialog">

        <h2 class="firstHeadline"><g:link controller="feedMatches" action="list" params="[lang: lang.getShortName()]"><g:message code="ltc.feed.matches.title"/></g:link></h2>

        <div class="mainPart">
            <p>
                <g:message code="ltc.feed.matches.explain.short"/>
                <g:message code="ltc.feed.matches.not.available"/>
            </p>
        </div>

        <h2><g:link controller="pageCheck" params="[lang: lang.getShortName()]"><g:message code="ltc.wiki.check"/></g:link></h2>

        <div class="mainPart">
            <p><g:message code="ltc.wiki.check.explain"/></p>
        </div>

    </div>

    <div style="margin-top: 30px">
        <g:render template="/languageToolVersion"/>
    </div>

</div>

</body>
</html>
