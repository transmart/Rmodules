package jobs

import jobs.misc.AnalysisConstraints
import jobs.steps.*
import jobs.steps.helpers.CategoricalColumnConfigurator
import jobs.steps.helpers.ColumnConfigurator
import jobs.steps.helpers.HighDimensionColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.HighDimensionResource

import javax.annotation.PostConstruct

abstract class AcghAnalysisJob extends AbstractLowDimensionalAnalysisJob {

    @Autowired
    AnalysisConstraints analysisConstraints

    @Autowired
    HighDimensionResource highDimensionResource

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    CategoricalColumnConfigurator groupByConfigurator

    @Autowired
    HighDimensionColumnConfigurator highDimensionColumnConfigurator

    @PostConstruct
    void init() {
        primaryKeyColumnConfigurator.column = new PrimaryKeyColumn(header: 'PATIENT_NUM')

        groupByConfigurator.header = 'group'
        groupByConfigurator.keyForConceptPaths = 'groupVariable'
    }

    @Override
    protected List<Step> prepareDataSteps() {
        List<Step> steps = []

        steps << dumpHeaderConceptsFileStep

        steps << new BuildTableResultStep(
                table: table,
                configurators: columnConfigurators)

        steps << new SimpleDumpTableResultStep(table: table,
                temporaryDirectory: temporaryDirectory,
                outputFileName: 'phenodata.tsv'
        )

        def openResultSetStep = new OpenHighDimensionalDataStep(
                params: params,
                dataTypeResource: highDimensionResource.getSubResourceForType(analysisConstraints['data_type']),
                analysisConstraints: analysisConstraints)

        steps << openResultSetStep

        steps << createDumpHighDimensionDataStep { -> openResultSetStep.results }

        steps
    }

    @Override
    protected List<ColumnConfigurator> getColumnConfigurators() {
        [
                primaryKeyColumnConfigurator,
                groupByConfigurator,
        ]
    }

    @Override
    protected Step createDumpHighDimensionDataStep(Closure resultsHolder) {
        new AcghRegionDumpDataStep(
                temporaryDirectory: temporaryDirectory,
                resultsHolder: resultsHolder,
                params: params)
    }

}
