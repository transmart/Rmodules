package jobs

import jobs.steps.helpers.ColumnConfigurator
import jobs.steps.helpers.NumericColumnConfigurator
import jobs.steps.helpers.OptionalBinningColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct

@Component
@Scope('job')
class LogisticRegression extends AbstractLowDimensionalAnalysisJob {
//TODO Test. It was using simple dump instead
    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    NumericColumnConfigurator independentVariableConfigurator

    @Autowired
    @Qualifier('general')
    OptionalBinningColumnConfigurator outcomeVariableConfigurator

    @PostConstruct
    void init() {
        primaryKeyColumnConfigurator.column = new PrimaryKeyColumn(header: 'PATIENT_NUM')

        configureIndependentVariableConfigurator()
        configureOutcomeVariableConfigurator()
    }

    private void configureIndependentVariableConfigurator() {
        independentVariableConfigurator.header = 'Y'
        independentVariableConfigurator.alwaysClinical = true
        independentVariableConfigurator.setKeys('independent')
    }

    private void configureOutcomeVariableConfigurator() {
        outcomeVariableConfigurator.header = 'X'
        outcomeVariableConfigurator.setKeys('groupBy')
        outcomeVariableConfigurator.projection          = Projection.DEFAULT_REAL_PROJECTION
        outcomeVariableConfigurator.multiRow            = true

        def binningConfigurator = outcomeVariableConfigurator.binningConfigurator
        binningConfigurator.keyForDoBinning           = 'binning'
        binningConfigurator.keyForManualBinning       = 'manualBinning'
        binningConfigurator.keyForNumberOfBins        = 'numberOfBins'
        binningConfigurator.keyForBinDistribution     = 'binDistribution'
        binningConfigurator.keyForBinRanges           = 'binRanges'
        binningConfigurator.keyForVariableType        = 'variableType'

    }

    @Override
    protected List<String> getRStatements() {
        [
            '''source('$pluginDirectory/LogisticRegression/LogisticRegressionLoader.R')''',
            '''LogisticRegressionData.loader(input.filename='$inputFileName',
                        concept.dependent='$dependentVariable',
                        concept.independent='$independentVariable',
                        binning.enabled='FALSE',
                        binning.variable='')'''
        ]
    }

    @Override
    protected List<ColumnConfigurator> getColumnConfigurators() {
        [
            primaryKeyColumnConfigurator,
            outcomeVariableConfigurator,
            independentVariableConfigurator,
        ]
    }

    @Override
    protected getForwardPath() {
        return "/logisticRegression/logisticRegressionOutput?jobName=$name"
    }
}
