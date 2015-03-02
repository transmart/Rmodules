package org.transmartproject.utils

import java.util.regex.Pattern

class ConceptUtils {

    static final String PARTS_SEP = '\\'
    static final String CONCEPTS_SEP = '|'

    private static shortenToUniqueTails(List<List<String>> partsList, int step = 0) {
        int revPos = -(step + 1)
        Map<String, List<List<String>>> groups = partsList.groupBy { List<String> parts ->
            if (parts.size() > step) {
                parts[revPos]
            }
        }
        groups.each {
            //Concepts that grouped under it.key == null do not have so many levels.
            if (it.key && it.value.size() > 1) {
                shortenToUniqueTails(it.value, step + 1)
            } else if (it.key) {
                def parts = it.value[0]
                //remove all parts of the concept path from the root down to current element (excluding it).
                //note that we are changing input list here
                (parts.size() + revPos).times { parts.remove(0) }
            }
        }
    }

    /**
     * Shorten the paths to shortest ones, but still unique in current scope
     * (among concepts represented in the input list) by removing higher levels
     * that are not important for uniqueness of this concept in given scope.
     * Although it does not guarantee uniqueness if input list already contains repetitions.
     * e.g. Given input list ['\A\B\C\', '\A\2\C\', '\B\C\', '\B\C\']
     * function returns ['\A\B\C\', '\2\C\', '\B\C\', '\B\C\']
     * @param conceptPathes concepts to shorten
     * @return shortened and normalized concept paths
     */
    static List<String> shortestUniqueTails(List<String> conceptPathes) {
        List<List<String>> conceptsParts = conceptPathes.collect { it.split(Pattern.quote(PARTS_SEP)).findAll() }
        shortenToUniqueTails(conceptsParts)
        conceptsParts.collect { PARTS_SEP + it.join(PARTS_SEP) + PARTS_SEP }
    }

    //TODO This functionality ovrlaps with {@see ConceptKey}
    /**
     * Return part of the concept path
     * @param conceptsString
     * @param deepLevel 0-leaf, 1-parent,...
     * @return
     */
    private static Set<String> getParts(String conceptsString, int deepLevel) {
        def concepts = conceptsString.split(Pattern.quote(CONCEPTS_SEP))
        concepts.collect { String concept ->
            def parts = concept.split(Pattern.quote(PARTS_SEP)).findAll()
            if (parts.size() > deepLevel) {
                PARTS_SEP + parts[-(deepLevel + 1)] + PARTS_SEP
            }
        }.findAll().toSet()
    }

    static Set<String> getParentFolders(String conceptsString) {
        getParts(conceptsString, 1)
    }

    static Set<String> getLeafFolders(String conceptsString) {
        getParts(conceptsString, 0)
    }
}
