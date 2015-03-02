package jobs

import jobs.steps.BuildTableResultStep
import jobs.steps.MultiRowAsGroupDumpTableResultsStep
import jobs.steps.Step
import jobs.steps.WriteFileStep
import jobs.steps.helpers.ColumnConfigurator
import jobs.table.Table
import org.springframework.beans.factory.annotation.Autowired

import static jobs.steps.AbstractDumpStep.getDEFAULT_OUTPUT_FILE_NAME

abstract class AbstractLowDimensionalAnalysisJob extends AbstractAnalysisJob {

    @Autowired
    Table table

    protected Step getDumpHeaderConceptsFileStep() {
        new WriteFileStep(
                temporaryDirectory: temporaryDirectory,
                fileName: 'header_concepts.txt',
                fileContent: columnConfigurators.collect { ColumnConfigurator cc ->
                    if (cc.header && cc.conceptPaths) {
                        [] + cc.header + cc.conceptPaths
                    }
                }.findAll().collect { it.join('\t') }.join('\n')
        )
    }

    @Override
    protected List<Step> prepareDataSteps() {

        List<Step> steps = []

        steps << dumpHeaderConceptsFileStep

        steps << new BuildTableResultStep(
                table: table,
                configurators: columnConfigurators)

        steps << new MultiRowAsGroupDumpTableResultsStep(
                table: table,
                temporaryDirectory: temporaryDirectory,
                outputFileName: DEFAULT_OUTPUT_FILE_NAME)

        steps
    }

    abstract protected List<ColumnConfigurator> getColumnConfigurators()

}
