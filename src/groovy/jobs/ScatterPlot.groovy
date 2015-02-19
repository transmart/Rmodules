package jobs

import jobs.steps.helpers.ColumnConfigurator
import jobs.steps.helpers.NumericColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct
import java.security.InvalidParameterException

@Component
@Scope('job')
class ScatterPlot extends AbstractLowDimensionalAnalysisJob {

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    NumericColumnConfigurator independentVariableConfigurator

    @Autowired
    NumericColumnConfigurator dependentVariableConfigurator

    @PostConstruct
    void init() {
        primaryKeyColumnConfigurator.column =
                new PrimaryKeyColumn(header: 'PATIENT_NUM')

        configureConfigurator independentVariableConfigurator, 'independent', 'X'
        configureConfigurator dependentVariableConfigurator,   'dependent',   'Y'

        /* we also need these two extra variables (see R statements) */
        extraParamValidation()
    }

    private void extraParamValidation() {
        if (params['divIndependentVariablePathway'] != null) {
            if (params['divIndependentPathwayName'] == null) {
                throw new InvalidParameterException(
                        'Missing user parameter "divIndependentPathwayName"')
            }
        }

        if (params['divDependentVariablePathway'] != null) {
            if (params['divDependentPathwayName'] == null) {
                throw new InvalidParameterException(
                        'Missing user parameter "divDependentPathwayName"')
            }
        }
    }

    private void configureConfigurator(NumericColumnConfigurator configurator,
                                       String key,
                                       String header) {
        configurator.header     = header
        configurator.projection = Projection.LOG_INTENSITY_PROJECTION
        configurator.multiRow   = true
        configurator.setKeys(key)
    }

    @Override
    protected List<String> getRStatements() {
        [ '''source('$pluginDirectory/ScatterPlot/ScatterPlotLoader.R')''',
                '''ScatterPlot.loader(
                    input.filename               = '$inputFileName',
                    concept.dependent            = '$dependentVariable',
                    concept.independent          = '$independentVariable',
                    concept.dependent.type       = '$divDependentVariableType',
                    concept.independent.type     = '$divIndependentVariableType',
                    genes.dependent              = '$divDependentPathwayName',
                    genes.independent            = '$divIndependentPathwayName',
                    aggregate.probes.independent = '$divIndependentVariableprobesAggregation' == 'true',
                    aggregate.probes.dependent   = '$divDependentVariableprobesAggregation'   == 'true',
                    snptype.dependent            = '',
                    snptype.independent          = '',
        )''' ] // last two params should be removed
    }

    @Override
    protected List<ColumnConfigurator> getColumnConfigurators() {
        [
            primaryKeyColumnConfigurator,
            independentVariableConfigurator,
            dependentVariableConfigurator,
        ]
    }

    @Override
    protected getForwardPath() {
        "/scatterPlot/scatterPlotOut?jobName=$name"
    }

}
