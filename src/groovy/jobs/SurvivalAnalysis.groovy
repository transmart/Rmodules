package jobs

import jobs.steps.helpers.CensorColumnConfigurator
import jobs.steps.helpers.ColumnConfigurator
import jobs.steps.helpers.NumericColumnConfigurator
import jobs.steps.helpers.OptionalBinningColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.MissingValueAction
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Created by carlos on 1/20/14.
 */
@Component
@Scope('job')
class SurvivalAnalysis extends AbstractLowDimensionalAnalysisJob implements InitializingBean {

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    NumericColumnConfigurator timeVariableConfigurator

    @Autowired
    @Qualifier('general')
    OptionalBinningColumnConfigurator categoryVariableConfigurator

    @Autowired
    CensorColumnConfigurator censoringVariableConfigurator

    @Override
    void afterPropertiesSet() throws Exception {
        primaryKeyColumnConfigurator.column = new PrimaryKeyColumn(header: 'PATIENT_NUM')

        configureTimeVariableConfigurator()
        configureCategoryVariableConfigurator()
        configureCensoringVariableConfigurator()
    }

    void configureTimeVariableConfigurator() {
        timeVariableConfigurator.header = 'TIME'
        timeVariableConfigurator.setKeys('time')
        timeVariableConfigurator.alwaysClinical = true
    }

    void configureCategoryVariableConfigurator() {
        categoryVariableConfigurator.required = false
        categoryVariableConfigurator.header             = 'CATEGORY'
        categoryVariableConfigurator.projection         = Projection.LOG_INTENSITY_PROJECTION
        categoryVariableConfigurator.multiRow           = true

        categoryVariableConfigurator.setKeys('dependent')
        categoryVariableConfigurator.binningConfigurator.setKeys('')
        categoryVariableConfigurator.keyForConceptPaths = 'categoryVariable'

        def missingValueAction = categoryVariableConfigurator.getConceptPaths() ?
                new MissingValueAction.DropRowMissingValueAction() :
                new MissingValueAction.ConstantReplacementMissingValueAction(replacement: 'STUDY')

        categoryVariableConfigurator.missingValueAction = missingValueAction
        categoryVariableConfigurator.binningConfigurator.missingValueAction = missingValueAction
    }

    void configureCensoringVariableConfigurator() {
        censoringVariableConfigurator.header             = 'CENSOR'
        censoringVariableConfigurator.keyForConceptPaths = 'censoringVariable'
    }

    @Override
    protected List<String> getRStatements() {
        [
            '''source('$pluginDirectory/Survival/CoxRegressionLoader.r')''',
            '''CoxRegression.loader(
                input.filename      = '$inputFileName')''',
            '''source('$pluginDirectory/Survival/SurvivalCurveLoader.r')''',
            '''SurvivalCurve.loader(
                input.filename      = '$inputFileName',
                concept.time        = '$timeVariable')''',
        ]
    }

    @Override
    protected List<ColumnConfigurator> getColumnConfigurators() {
        [
            primaryKeyColumnConfigurator,
            timeVariableConfigurator,
            censoringVariableConfigurator,
            categoryVariableConfigurator,
        ]
    }

    @Override
    protected getForwardPath() {
        "/survivalAnalysis/survivalAnalysisOutput?jobName=$name"
    }

}
