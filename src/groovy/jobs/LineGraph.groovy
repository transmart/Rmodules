package jobs

import com.google.common.base.Function
import com.google.common.collect.Maps
import jobs.steps.BuildConceptTimeValuesStep
import jobs.steps.BuildTableResultStep
import jobs.steps.LineGraphDumpTableResultsStep
import jobs.steps.Step
import jobs.steps.helpers.*
import jobs.table.ConceptTimeValuesTable
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct

import static jobs.steps.AbstractDumpStep.DEFAULT_OUTPUT_FILE_NAME

@Component
@Scope('job')
class LineGraph extends AbstractLowDimensionalAnalysisJob {

    private static final String SCALING_VALUES_FILENAME = 'conceptScaleValues'

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    @Qualifier('general')
    OptionalBinningColumnConfigurator innerGroupByConfigurator

    @Autowired
    OptionalColumnConfiguratorDecorator groupByConfigurator

    @Autowired
    ContextNumericVariableColumnConfigurator measurementConfigurator

    @Autowired
    ConceptTimeValuesTable conceptTimeValues

    @PostConstruct
    void init() {
        primaryKeyColumnConfigurator.column = new PrimaryKeyColumn(header: 'PATIENT_NUM')

        measurementConfigurator.header = 'VALUE'
        measurementConfigurator.projection = Projection.LOG_INTENSITY_PROJECTION
        measurementConfigurator.multiRow = true
        measurementConfigurator.multiConcepts = true
        // we do not want group name pruning for LineGraph

        measurementConfigurator.keyForConceptPath = 'dependentVariable'
        measurementConfigurator.keyForDataType = 'divDependentVariableType'
        measurementConfigurator.keyForSearchKeywordId = 'divDependentVariablePathway'

        innerGroupByConfigurator.projection = Projection.LOG_INTENSITY_PROJECTION
        innerGroupByConfigurator.multiRow = true
        innerGroupByConfigurator.keyForIsCategorical = 'groupByVariableCategorical'
        innerGroupByConfigurator.setKeys 'groupBy'

        def binningConfigurator = innerGroupByConfigurator.binningConfigurator
        binningConfigurator.keyForDoBinning = 'binningGroupBy'
        binningConfigurator.keyForManualBinning = 'manualBinningGroupBy'
        binningConfigurator.keyForNumberOfBins = 'numberOfBinsGroupBy'
        binningConfigurator.keyForBinDistribution = 'binDistributionGroupBy'
        binningConfigurator.keyForBinRanges = 'binRangesGroupBy'
        binningConfigurator.keyForVariableType = 'variableTypeGroupBy'

        groupByConfigurator.header = 'GROUP_VAR'
        groupByConfigurator.generalCase = innerGroupByConfigurator
        groupByConfigurator.keyForEnabled = 'groupByVariable'
        groupByConfigurator.setConstantColumnFallback 'SINGLE_GROUP'

        conceptTimeValues.conceptPaths = measurementConfigurator.getConceptPaths()
    }

    @Override
    protected List<Step> prepareDataSteps() {
        List<Step> steps = []

        steps << dumpHeaderConceptsFileStep

        steps << new BuildTableResultStep(
                table: table,
                configurators: columnConfigurators)

        steps << new LineGraphDumpTableResultsStep(
                table: table,
                temporaryDirectory: temporaryDirectory,
                outputFileName: DEFAULT_OUTPUT_FILE_NAME)

        steps << new BuildConceptTimeValuesStep(
                table: conceptTimeValues,
                outputFile: new File(temporaryDirectory, SCALING_VALUES_FILENAME),
                header: ["GROUP", "VALUE"]
        )

        steps
    }

    private String getScalingFilename() {
        conceptTimeValues.resultMap ? SCALING_VALUES_FILENAME : null
    }

    @Override
    protected Map getRExtraParams() {
        Map<String, Closure<String>> extraParams = [:]
        extraParams['scalingFilename'] = { getScalingFilename() }
        extraParams['inputFileName'] = { DEFAULT_OUTPUT_FILE_NAME }
        Maps.transformValues(extraParams, { it() } as Function)
    }

    @Override
    protected List<String> getRStatements() {
        ['''source('$pluginDirectory/LineGraph/LineGraphLoader.r')''',
         '''LineGraph.loader(
                    input.filename           = '$inputFileName',
                    graphType                = '$graphType',
                    scaling.filename  = ${scalingFilename == 'null' ? 'NULL' : "'$scalingFilename'"},
                    plotEvenlySpaced = '$plotEvenlySpaced'
        )''']
    }

    @Override
    protected List<ColumnConfigurator> getColumnConfigurators() {
        [
                primaryKeyColumnConfigurator,
                measurementConfigurator,
                groupByConfigurator,
        ]
    }

    @Override
    protected getForwardPath() {
        "/lineGraph/lineGraphOutput?jobName=$name"
    }

}
