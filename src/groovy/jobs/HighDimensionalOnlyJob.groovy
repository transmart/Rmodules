package jobs

import jobs.misc.AnalysisConstraints
import jobs.steps.OpenHighDimensionalDataStep
import jobs.steps.Step
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.transmartproject.core.dataquery.highdim.HighDimensionResource

abstract class HighDimensionalOnlyJob extends AbstractAnalysisJob {

    @Autowired
    AnalysisConstraints analysisConstraints

    @Autowired
    HighDimensionResource highDimensionResource

    @Autowired
    ApplicationContext appCtx

    protected List<Step> prepareDataSteps() {
        List<Step> steps = []

        def openResultSetStep = new OpenHighDimensionalDataStep(
                params: params,
                dataTypeResource: highDimensionResource.getSubResourceForType(analysisConstraints['data_type']),
                analysisConstraints: analysisConstraints)

        steps << openResultSetStep

        steps << createDumpHighDimensionDataStep { -> openResultSetStep.results }

        steps
    }

    abstract protected Step createDumpHighDimensionDataStep(Closure resultsHolder)

}
