package jobs.steps.helpers

import grails.test.mixin.TestMixin
import jobs.UserParameters
import jobs.table.Table
import jobs.table.columns.PrimaryKeyColumn
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.clinical.ClinicalDataResource
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.PatientRow
import org.transmartproject.core.dataquery.highdim.projections.Projection

import static jobs.steps.helpers.ConfiguratorTestsHelper.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@TestMixin(JobsIntegrationTestMixin)
class BoxPlotVariableColumnConfiguratorTests {

    @Autowired
    Table table

    @Autowired
    UserParameters params

    @Autowired
    BoxPlotVariableColumnConfigurator testee

    @Autowired
    ClinicalDataResource clinicalDataResourceMock

    @Delegate(interfaces = false)
    ConfiguratorTestsHelper configuratorTestsHelper =
            new ConfiguratorTestsHelper()

    @Before
    void setUp() {
        initializeAsBean configuratorTestsHelper

        testee.projection            = Projection.DEFAULT_REAL_PROJECTION
        testee.keyForConceptPaths    = 'variable'
        testee.keyForDataType        = 'divVariableType'
        testee.keyForSearchKeywordId = 'divVariablePathway'

        BinningColumnConfigurator binningColumnConfigurator = testee.binningConfigurator

        binningColumnConfigurator.keyForDoBinning       = 'binning'
        binningColumnConfigurator.keyForManualBinning   = 'manualBinning'
        binningColumnConfigurator.keyForNumberOfBins    = 'numberOfBins'
        binningColumnConfigurator.keyForBinDistribution = 'binDistribution'
        binningColumnConfigurator.keyForBinRanges       = 'binRanges'
        // distinct from testee.keyForDataType!
        binningColumnConfigurator.keyForVariableType    = 'variableType'

        testee.valueForThisColumnBeingBinned = 'IND'
        testee.keyForIsCategorical           = 'variableCategorical'
    }

    @Test
    void testMultipleContinuousVariables() {
        params.@map.putAll([
                variable           : BUNDLE_OF_CLINICAL_CONCEPT_PATH.join('|'),
                divVariableType    : DATA_TYPE_NAME_CLINICAL,

                binning            : 'FALSE',
                binVariable        : 'DEP',

                variableCategorical: 'false',
                result_instance_id1: RESULT_INSTANCE_ID1,
                result_instance_id2: RESULT_INSTANCE_ID2,
        ])

        List<ClinicalVariableColumn> columns =
                createClinicalVariableColumns BUNDLE_OF_CLINICAL_CONCEPT_PATH

        List<BigDecimal> valuesForColumns = [
                11.0, 12.0, 13.0, //1st patient
                21.0, 22.0, 23.0, //2nd patient
        ]

        setupClinicalResult 2, columns, valuesForColumns

        configuratorTestsHelper.play {
            testee.addColumn()

            table.buildTable()

            def res = table.result
            assertThat res, containsInAnyOrder(
                    contains(allOf(
                            dot(['var 1', 'var 2', 'var 3'],
                                    valuesForColumns[0..2], { a, b ->
                                hasEntry(is(a), is(b))
                            }
                            ))),
                    contains(allOf(
                            dot(['var 1', 'var 2', 'var 3'],
                                    valuesForColumns[3..5], { a, b ->
                                        hasEntry(is(a), is(b))
                                    }
                            ))))

            // Y is the header for the numeric col
            assertThat table.headers, contains('Y')
        }
    }

    @Test
    void testSingleContinuousVariable() {
        params.@map.putAll([
                variable           : CONCEPT_PATH_CLINICAL,
                divVariableType    : DATA_TYPE_NAME_CLINICAL,

                binning            : 'FALSE',
                binVariable        : 'DEP',

                variableCategorical: 'false',
                result_instance_id1: RESULT_INSTANCE_ID1,
                result_instance_id2: RESULT_INSTANCE_ID2,
        ])

        ClinicalVariableColumn column =
                createClinicalVariableColumns([CONCEPT_PATH_CLINICAL])[0]

        List<BigDecimal> valuesForColumn = [41.0 /* p1 */, 42.0 /* p2 */]

        setupClinicalResult 2, [column], valuesForColumn

        configuratorTestsHelper.play {
            testee.addColumn()

            table.buildTable()

            def res = table.result
            assertThat res, containsInAnyOrder(
                    contains(valuesForColumn[0]),
                    contains(valuesForColumn[1]))
        }
    }

    @Test
    void testCategoricalVariable() {
        params.@map.putAll([
                variable           : BUNDLE_OF_CLINICAL_CONCEPT_PATH.join('|'),
                divVariableType    : DATA_TYPE_NAME_CLINICAL,

                binning            : 'FALSE',
                binVariable        : 'DEP',

                variableCategorical: 'true',
                result_instance_id1: RESULT_INSTANCE_ID1,
                result_instance_id2: RESULT_INSTANCE_ID2,
        ])

        List<ClinicalVariableColumn> columns =
                createClinicalVariableColumns BUNDLE_OF_CLINICAL_CONCEPT_PATH

        List<BigDecimal> valuesForColumns = [
                'aa', null, null, //1st patient
                null, 'bb', null, //2nd patient
        ]

        setupClinicalResult 2, columns, valuesForColumns

        configuratorTestsHelper.play {
            table.addColumn(new PrimaryKeyColumn(header: 'PK'), [] as Set)
            testee.addColumn()

            table.buildTable()

            def res = table.result
            assertThat res, containsInAnyOrder(
                    contains('subject id #1', 'aa'),
                    contains('subject id #2', 'bb'))

            // X is the header for the categorical col (or binned numeric)
            assertThat table.headers, contains('PK', 'X')
        }
    }

    private void setupClinicalResult(int nPatients,
                                     List<ClinicalVariableColumn> columns,
                                     List<BigDecimal> valuesForColumns) {
        assert nPatients * columns.size() == valuesForColumns.size()

        TabularResult<ClinicalVariableColumn, PatientRow> clinicalResult =
                configuratorTestsHelper.mock(TabularResult)
        clinicalResult.iterator().returns(createPatientRows(nPatients, columns,
                valuesForColumns, true /* relaxed */).iterator())
        clinicalResult.close().stub()

        clinicalDataResourceMock.retrieveData(
                mockQueryResults(),
                containsInAnyOrder(columns.collect { is it })).returns(clinicalResult)
    }

}