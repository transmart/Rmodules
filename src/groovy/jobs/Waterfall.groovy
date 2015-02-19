package jobs

import jobs.steps.helpers.ColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.steps.helpers.WaterfallColumnConfigurator
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@Scope('job')
class Waterfall extends AbstractLowDimensionalAnalysisJob {

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    WaterfallColumnConfigurator columnConfigurator

    @PostConstruct
    void init() {
        primaryKeyColumnConfigurator.column = new PrimaryKeyColumn(header: 'PATIENT_NUM')
        columnConfigurator.header = 'X'
        columnConfigurator.keyForConceptPath = 'dataNode'
        columnConfigurator.keyForLowValue = 'lowRangeValue'
        columnConfigurator.keyForOperatorForLow = 'lowRangeOperator'
        columnConfigurator.keyForHighValue = 'highRangeValue'
        columnConfigurator.keyForOperatorForHigh = 'highRangeOperator'
    }

    @Override
    protected List<String> getRStatements() {
        [
                '''source('$pluginDirectory/Waterfall/WaterfallPlotLoader.R')''',
                '''WaterfallPlot.loader(input.filename='$inputFileName',
                concept='$variablesConceptPaths')'''
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
        "/waterfall/waterfallOut?jobName=$name"
    }

}
