package org.transmartproject.analyses.tags

import au.com.bytecode.opencsv.CSVReader
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

class CsvTagLib {
    static encodeAsForTags = [renderAsTable: 'raw']

    /** Renders csv file as html table
     *@attr file (required) - csv file to render
     *@attr separator - separator to use as delimiter between columns. Usually comma (',') or tab ('\t')
     *@attr rowIndexesNumber - how many rows are in header. Default 0
     *@attr columnIndexesNumber - how many columns are in header. Default 0
     *@attr cls - applied to result html table
     */
    def renderAsTable = { attrs, body ->
        if (attrs.file == null) {
            throw new GrailsTagException("Missing required attribute [file].")
        }
        File file
        if (attrs.file instanceof String) {
            file = new File(attrs.file as String)
        } else if (attrs.file instanceof File) {
            file = attrs.file
        } else {
            throw new GrailsTagException("Expects [file] attribute has unexpected type: ${attrs.file.class}.")
        }
        if (!file.exists()) {
            throw new GrailsTagException("${file} does not exit.")
        }

        char separator = '\t'
        if (attrs.separator != null) {
            if (![String, Character].any { it.isAssignableFrom(attrs.separator.class) }) {
                throw new GrailsTagException("[separator] attribute has unexpected type: ${attrs.separator.class}")
            }
            if (attrs.separator instanceof String && attrs.separator.length() > 1) {
                throw new GrailsTagException("[separator] is longer than 1 character: ${attrs.separator.length()}")
            }

            separator = attrs.separator
        }

        int rowIndexesNumber = 0
        if (attrs.rowIndexesNumber && attrs.rowIndexesNumber > 0) {
            rowIndexesNumber = attrs.rowIndexesNumber as int
        }

        int columnIndexesNumber = 0
        if (attrs.columnIndexesNumber && attrs.columnIndexesNumber > 0) {
            columnIndexesNumber = attrs.columnIndexesNumber as int
        }

        CSVReader reader = new CSVReader(new FileReader(file), separator)
        try {
            String[] row = reader.readNext()
            if (row) {
                int rowNumber = 0
                out << "<table${attrs.cls ? ' class=\"' + attrs.cls.encodeAsHTML() + '\"' : ''}>"
                out << composeHtmlTableRowString(row.toList(), rowIndexesNumber > rowNumber ? Integer.MAX_VALUE : columnIndexesNumber)
                while (row = reader.readNext()) {
                    rowNumber += 1
                    out << composeHtmlTableRowString(row.toList(), rowIndexesNumber > rowNumber ? Integer.MAX_VALUE : columnIndexesNumber)
                }
                out << '</table>'
            }
        } finally {
            reader.close()
        }
    }

    private String composeHtmlTableRowString(List<String> row, int columnIndexesNumber = 0) {
        List<String> tds = []
        row.eachWithIndex { cellValue, indx ->
            def cellTag = indx < columnIndexesNumber ? 'th' : 'td'
            tds << "<${cellTag}>${cellValue.encodeAsHTML()}</${cellTag}>"
        }
        "<tr>${tds.join('')}</tr>"
    }
}
