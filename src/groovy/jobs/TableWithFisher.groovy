package jobs

import jobs.steps.helpers.OptionalBinningColumnConfigurator
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import static org.transmartproject.utils.ConceptUtils.getLeafFolders
import static org.transmartproject.utils.ConceptUtils.getParentFolders

@Component
@Scope('job')
class TableWithFisher extends CategoricalOrBinnedJob {

    @Autowired
    @Qualifier('general')
    OptionalBinningColumnConfigurator independentVariableConfigurator

    @Autowired
    @Qualifier('general')
    OptionalBinningColumnConfigurator dependentVariableConfigurator

    @Override
    void afterPropertiesSet() throws Exception {
        primaryKeyColumnConfigurator.column =
                new PrimaryKeyColumn(header: 'PATIENT_NUM')

        configureConfigurator independentVariableConfigurator,
                'indep', 'independent', 'X'
        configureConfigurator dependentVariableConfigurator,
                'dep',   'dependent',   'Y'
    }

    @Override
    protected List<String> getRStatements() {
        [ '''source('$pluginDirectory/TableWithFisher/FisherTableLoader.R')''',
                '''
                FisherTable.loader(
                input.filename               = '$inputFileName',
                aggregate.probes.independent = '$divIndependentVariableprobesAggregation' == 'true',
                aggregate.probes.dependent   = '$divDependentVariableprobesAggregation'   == 'true'
                )''' ]
    }

    @Override
    protected getForwardPath() {
        """/tableWithFisher/fisherTableOut?jobName=$name"""
    }

    @Override
    protected String headerMessage(String header) {
        String modifiedHeader = super.headerMessage header
        if (modifiedHeader == independentVariableConfigurator.header) {
            String indVar
            if (independentVariableConfigurator.categorical) {
                indVar = getParentFolders(independentVariableConfigurator.conceptPaths).join(' ')
            } else {
                indVar = getLeafFolders(independentVariableConfigurator.conceptPaths).join(' ')
            }
            "Independent variable - ${indVar} value"
        } else if (modifiedHeader == dependentVariableConfigurator.header) {
            String depVar
            if (dependentVariableConfigurator.categorical) {
                depVar = getParentFolders(dependentVariableConfigurator.conceptPaths).join(' ')
            } else {
                depVar = getLeafFolders(dependentVariableConfigurator.conceptPaths).join(' ')
            }
            "Dependent variable - ${depVar} value"
        } else {
            modifiedHeader
        }
    }
}
