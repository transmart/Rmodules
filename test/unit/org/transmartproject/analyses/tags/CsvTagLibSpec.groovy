package org.transmartproject.analyses.tags

import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.transmartproject.analyses.tags.CsvTagLib
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(CsvTagLib)
class CsvTagLibSpec extends Specification {

    @Shared
    File csvFile

    def setupSpec() {
        csvFile = File.createTempFile("csv_tag_lib_spec.csv", '.tmp')
        def testContent = [
                [' ', 'A',  'B'],
                ['1', 'A1', 'B1'],
                ['2', 'A2', 'B2'],
        ]
        csvFile.text = testContent.collect { row -> row.collect { cell -> "\"${cell}\"" }.join(',') }.join('\n')
    }

    def cleanupSpec() {
        csvFile.delete()
    }

    void "file is not specified"() {
        expect:
        shouldFail(GrailsTagException, {
            tagLib.renderAsTable()
        })
    }

    void "file has wrong type"() {
        expect:
        shouldFail(GrailsTagException, {
            tagLib.renderAsTable(file: 0L)
        })
    }

    void "file does not exist"() {
        expect:
        shouldFail(GrailsTagException, {
            tagLib.renderAsTable(file: new File('NOT-EXIST'))
        })
    }

    void "separator has wrong type"() {
        expect:
        shouldFail(GrailsTagException, {
            tagLib.renderAsTable(file: csvFile, separator: 0L)
        })
    }

    void "separator has wrong length"() {
        expect:
        shouldFail(GrailsTagException, {
            tagLib.renderAsTable(file: csvFile, separator: ',,')
        })
    }

    void "no output for empty file"() {
        when:
        File emptyFile = File.createTempFile("empty", '.tmp')
        emptyFile.text = ''

        then:
        tagLib.renderAsTable(file: emptyFile, separator: ',') == ''
    }

    void "render successfully"() {
        expect:
        tagLib.renderAsTable(file: csvFile, separator: ',') == '<table>' +
                '<tr><td> </td><td>A</td><td>B</td></tr>' +
                '<tr><td>1</td><td>A1</td><td>B1</td></tr>' +
                '<tr><td>2</td><td>A2</td><td>B2</td></tr></table>'
    }

    void "class rendering"() {
        expect:
        tagLib.renderAsTable(file: csvFile, separator: ',', cls: 'a b') == '<table class="a b">' +
                '<tr><td> </td><td>A</td><td>B</td></tr>' +
                '<tr><td>1</td><td>A1</td><td>B1</td></tr>' +
                '<tr><td>2</td><td>A2</td><td>B2</td></tr></table>'
    }

    void "header rendering"() {
        expect:
        tagLib.renderAsTable(file: csvFile, separator: ',', rowIndexesNumber: 1) == '<table>' +
                '<tr><th> </th><th>A</th><th>B</th></tr>' +
                '<tr><td>1</td><td>A1</td><td>B1</td></tr>' +
                '<tr><td>2</td><td>A2</td><td>B2</td></tr></table>'
    }

    void "wrong indexes numbers are ignored"() {
        expect:
        tagLib.renderAsTable(file: csvFile, separator: ',', rowIndexesNumber: -1, columnIndexesNumber: -2) == '<table>' +
                '<tr><td> </td><td>A</td><td>B</td></tr>' +
                '<tr><td>1</td><td>A1</td><td>B1</td></tr>' +
                '<tr><td>2</td><td>A2</td><td>B2</td></tr></table>'
    }

    void "column index rendering"() {
        expect:
        tagLib.renderAsTable(file: csvFile, separator: ',', columnIndexesNumber: 1) == '<table>' +
                '<tr><th> </th><td>A</td><td>B</td></tr>' +
                '<tr><th>1</th><td>A1</td><td>B1</td></tr>' +
                '<tr><th>2</th><td>A2</td><td>B2</td></tr></table>'
    }

    void "column and row indexes as string parameters"() {
        expect:
        tagLib.renderAsTable(file: csvFile, separator: ',', columnIndexesNumber: "1", rowIndexesNumber: "1") == '<table>' +
                '<tr><th> </th><th>A</th><th>B</th></tr>' +
                '<tr><th>1</th><td>A1</td><td>B1</td></tr>' +
                '<tr><th>2</th><td>A2</td><td>B2</td></tr></table>'
    }

}
