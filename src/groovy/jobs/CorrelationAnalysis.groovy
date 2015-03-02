package jobs

import jobs.steps.*
import jobs.steps.helpers.ColumnConfigurator
import jobs.steps.helpers.GroupNamesHolder
import jobs.steps.helpers.MultiNumericClinicalVariableColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

import static jobs.steps.AbstractDumpStep.DEFAULT_OUTPUT_FILE_NAME

@Component
@Scope('job')
class CorrelationAnalysis extends AbstractLowDimensionalAnalysisJob {
    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    MultiNumericClinicalVariableColumnConfigurator columnConfigurator

    GroupNamesHolder holder = new GroupNamesHolder()

    @PostConstruct
    void init() {
        primaryKeyColumnConfigurator.column =
                new PrimaryKeyColumn(header: 'PATIENT_NUM')

        columnConfigurator.header = 'VALUE'
        columnConfigurator.keyForConceptPaths = 'variablesConceptPaths'
        columnConfigurator.groupNamesHolder = holder

    }

    @Override
    protected List<Step> prepareDataSteps() {

        List<Step> steps = []

        steps << dumpHeaderConceptsFileStep

        steps << new BuildTableResultStep(
                table: table,
                configurators: columnConfigurators)

        steps << new CorrelationAnalysisDumpDataStep(
                table: table,
                temporaryDirectory: temporaryDirectory,
                groupNamesHolder:   holder,
                outputFileName: DEFAULT_OUTPUT_FILE_NAME)

        steps
    }

    @Override
    protected List<String> getRStatements() {
        [
            '''source('$pluginDirectory/Correlation/CorrelationLoader.r')''',
            '''Correlation.loader(input.filename='$inputFileName',
                    correlation.by='$correlationBy',
                    correlation.method='$correlationType')'''
        ]
    }

    @Override
    protected List<ColumnConfigurator> getColumnConfigurators() {
        [
            primaryKeyColumnConfigurator,
            columnConfigurator,
        ]
    }

    @Override
    protected getForwardPath() {
        "/correlationAnalysis/correlationAnalysisOutput?jobName=$name"
    }

}
