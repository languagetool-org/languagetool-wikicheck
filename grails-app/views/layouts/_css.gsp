<g:if test="${language && language.getName() == 'Persian'}">
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main-rtl.css')}" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'mobile-rtl.css')}" />
</g:if>
<g:else>
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'mobile.css')}" />
</g:else>
