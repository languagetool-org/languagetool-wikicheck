/* LanguageTool Community 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.languagetool

class FeedMatchesController {

    private final static int MAXIMUM_CHECK_AGE_IN_MINUTES = 3*60
    private final static int MAXIMUM_PING_AGE_IN_MINUTES = 10

    def index = {
        redirect(action:list,params:params)
    }

    def list = {
        if(!params.max) params.max = 10
        if(!params.offset) params.offset = 0
        if(!params.notFixedFilter) params.notFixedFilter = "1440"  // default to 24 hours
        String langCode = getLanguageCode()
        Calendar calendar = getCalender()
        int languageMatchCount = FeedMatches.countByLanguageCode(langCode)
        // Grouped Overview of Rule Matches:
        List matchesByRule = getMatchesByRule(calendar, langCode)
        def matchByCategoryCriteria = FeedMatches.createCriteria()
        def matchesByCategory = matchByCategoryCriteria {
            isNull('fixDate')
            eq('languageCode', langCode)
            projections {
                groupProperty("ruleCategory")
                count "ruleCategory", 'mycount'
                property("ruleCategory")
            }
            order 'mycount', 'desc'
        }
        // Rule Matches for this language:
        List hiddenRuleIds = getHiddenRuleIds(langCode)
        List<FeedMatches> matches = getFeedMatches(calendar, langCode, hiddenRuleIds)
        def allMatchesCriteria = FeedMatches.createCriteria()
        def allMatchesCount = allMatchesCriteria.count {
            if (params.filter) {
                eq('ruleId', params.filter)
            } else {
                not {
                    inList('ruleId', hiddenRuleIds)
                }
            }
            if (params.notFixedFilter && params.notFixedFilter != "0") {
                le('editDate', calendar.getTime())
            }
            if (params.categoryFilter) {
                eq('ruleCategory', params.categoryFilter)
            }
            isNull('fixDate')
            eq('languageCode', langCode)
        }
        boolean latestCheckDateWarning = false
        Date latestCheckDate = Pings.findByLanguageCode(langCode)?.checkDate
        if (latestCheckDate) {
            Calendar earliestDateStillOkay = new Date().toCalendar()
            earliestDateStillOkay.add(Calendar.MINUTE, - MAXIMUM_CHECK_AGE_IN_MINUTES)
            latestCheckDateWarning = latestCheckDate.before(earliestDateStillOkay.time)
        }
        Language langObj = Languages.getLanguageForShortName(langCode)
        [ languageMatchCount: languageMatchCount, corpusMatchList: matches,
                languages: SortedLanguages.get(), lang: langCode, totalMatches: allMatchesCount,
                matchesByRule: matchesByRule, matchesByCategory: matchesByCategory, hiddenRuleIds: hiddenRuleIds, language: langObj,
                latestCheckDateWarning: latestCheckDateWarning, latestCheckDate: latestCheckDate]
    }

    // http://statuscake.com cannot check for text with the free account, so we introduce
    // our own check that returns status 503 if there's a problem:
    def status() {
        Set okay = []
        Set failures = []
        Set noDate = []
        Calendar earliestDateStillOkay = new Date().toCalendar()
        for (Language lang  : Languages.get()) {
            Date latestCheckDate = Pings.findByLanguageCode(lang.getShortName())?.checkDate
            if (latestCheckDate) {
                earliestDateStillOkay.add(Calendar.MINUTE, - MAXIMUM_PING_AGE_IN_MINUTES)
                boolean latestCheckDateWarning = latestCheckDate.before(earliestDateStillOkay.time)
                if (latestCheckDateWarning) {
                    failures.add(lang.getShortName())
                } else {
                    okay.add(lang.getShortName())
                }
            } else {
                noDate.add(lang.getShortName())
            }
        }
        if (failures.size() == 0) {
            log.info("Status check fail: none, okay: ${okay}, no date: ${noDate}, threshold date: ${earliestDateStillOkay.getTime()}")
            render "OK"
        } else {
            log.warn("Status check fail: ${failures} (okay: ${okay}, no date: ${noDate}, threshold date: ${earliestDateStillOkay.getTime()})")
            if (params.nofail) {
                // useful to see the error message as WMF labs show its own error page when we return code 503:
                render(text: "FAIL: ${failures}")
            } else {
                render(text: "FAIL: ${failures}", status: 503)
            }
        }
    }

    private String getLanguageCode() {
        String langCode = "en"
        if (params.lang) {
            langCode = params.lang
        }
        return langCode
    }

    private Calendar getCalender() {
        Calendar calendar = Calendar.getInstance()
        if (params.notFixedFilter && params.notFixedFilter != "0") {
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - Integer.parseInt(params.notFixedFilter))
            calendar.set(Calendar.SECOND, 0)  // minute granularity is enough and enables use of MySQL cache (for a minute at least)
        }
        return calendar
    }

    private List<FeedMatches> getFeedMatches(cal, String langCode, List hiddenRuleIds) {
        def matchCriteria = FeedMatches.createCriteria()
        def matches = matchCriteria {
            if (params.filter) {
                eq('ruleId', params.filter)
            } else {
                not {
                    inList('ruleId', hiddenRuleIds)
                }
            }
            if (params.notFixedFilter && params.notFixedFilter != "0") {
                le('editDate', cal.getTime())
            }
            if (params.categoryFilter) {
                eq('ruleCategory', params.categoryFilter)
            }
            isNull('fixDate')
            eq('languageCode', langCode)
            firstResult(params.int('offset', 0))
            maxResults(params.int('max', 10))
            order('editDate', 'desc')
        }
        return matches
    }

    private List getHiddenRuleIds(String langCode) {
        List hiddenRuleIds = getHiddenRuleIds(langCode, grailsApplication.config.disabledRulesPropFile)
        hiddenRuleIds.addAll(getHiddenRuleIds(langCode, grailsApplication.config.disabledRulesForFeedPropFile))
        hiddenRuleIds
    }

    private List getHiddenRuleIds(String langCode, String propFileName) {
        List hiddenRuleIds = []
        Properties langToDisabledRules = new Properties()
        def fis = new FileInputStream(propFileName)
        try {
            langToDisabledRules.load(fis)
            hiddenRuleIds.addAll(langToDisabledRules.getProperty("all").split(",\\s*"))
            String langSpecificDisabledRulesStr = langToDisabledRules.get(langCode)
            if (langSpecificDisabledRulesStr) {
                List<String> langSpecificDisabledRules = langSpecificDisabledRulesStr.split(",")
                if (langSpecificDisabledRules) {
                    hiddenRuleIds.addAll(langSpecificDisabledRules)
                }
            }
        } finally {
            fis.close()
        }
        return hiddenRuleIds
    }

    private List getMatchesByRule(cal, String langCode) {
        def matchByRuleCriteria = FeedMatches.createCriteria()
        def matchesByRule = matchByRuleCriteria {
            // fixDate = null: neither fixed in Wikipedia (then it would also have the fixDiffId set) 
            // nor marked as 'fixed or false alarm' by a user (then it wouldn't have fixDiffId set either):
            isNull('fixDate')
            eq('languageCode', langCode)
            if (params.notFixedFilter && params.notFixedFilter != "0") {
                le('editDate', cal.getTime())
            }
            if (params.categoryFilter) {
                eq('ruleCategory', params.categoryFilter)
            }
            projections {
                groupProperty("ruleId")
                count "ruleId", 'mycount'
                property("ruleDescription")
            }
            order 'mycount', 'desc'
        }
        return matchesByRule
    }

    def feed = {
        Calendar calendar = getCalender()
        String langCode = getLanguageCode()
        Language lang = Languages.getLanguageForShortName(langCode)
        List hiddenRuleIds = getHiddenRuleIds(langCode)
        if (params.int('max', 10) > 250) {
            params.max = 250
        }
        List<FeedMatches> matches = getFeedMatches(calendar, langCode, hiddenRuleIds)
        render(feedType:"rss", feedVersion:"2.0") {
            title = "${lang.getName()} Wikipedia Recent Changes Check"
            description = "LanguageTool applied to Wikipedia's recent changed feed"
            link = createLink(controller: 'feedMatches', action: 'list', 
                    params: [lang: lang.getShortName(), notFixedFilter:params.notFixedFilter,
                             categoryFilter:params.categoryFilter, filter:params.filter], absolute: true)
            matches.each { match ->
                def content = match.title.encodeAsHTML() +  "<br/><br/>: " +
                        StringTools.formatError(match.errorContext.encodeAsHTML())
                            .replace(' class="error">', ' class="error"><b>')
                            .replace('</span>', '</b></span>') +
                        " <a href='http://${match.languageCode.encodeAsURL()}.wikipedia.org/w/index.php?title=${match.title.replace(' ', '_').encodeAsURL()}&diff=${match.diffId}'>(diff)</a>" +
                        "<br/><br/>" +
                        match.ruleMessage.replace('<suggestion>', '"').replace('</suggestion>', '"') + "<br/>"
                def url = "http://${lang.getShortName()}.wikipedia.org/wiki/${match.title.replace(' ', '_').replace('&', '%26')}"
                entry(match.ruleDescription) {
                    publishedDate = match.editDate
                    link = createLink(controller: 'pageCheck', action: 'index', params: [url: url, enabled: match.ruleId], absolute: true)
                    content
                }
            }
        }
    }

}