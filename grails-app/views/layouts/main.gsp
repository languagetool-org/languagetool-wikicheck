<!DOCTYPE html>
<html>
<head>
    <title><g:layoutTitle default="LanguageTool WikiCheck" /></title>
    <g:render template="/layouts/css"/>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <g:layoutHead />
    <r:layoutResources/>
</head>
<body>

<g:render template="/layouts/header"/>

<div id="spinner" class="spinner" style="display:none;">
    <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>

<noscript class="warn">Please turn on JavaScript for full use of this site.</noscript>

<div id="mainContent">
    <g:layoutBody />
</div>

<!-- Piwik -->
<script type="text/javascript">
    var _paq = _paq || [];
    _paq.push(['trackPageView']);
    _paq.push(['enableLinkTracking']);
    (function() {
        var u="//openthesaurus.stats.mysnip-hosting.de/";
        _paq.push(['setTrackerUrl', u+'piwik.php']);
        _paq.push(['setSiteId', 9]);
        var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
        g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);
    })();
</script>
<noscript><p><img src="//openthesaurus.stats.mysnip-hosting.de/piwik.php?idsite=9" style="border:0;" alt="" /></p></noscript>
<!-- End Piwik Code -->

</body>
</html>
