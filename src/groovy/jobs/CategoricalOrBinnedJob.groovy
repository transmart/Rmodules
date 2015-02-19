package jobs

import jobs.steps.helpers.BinningColumnConfigurator
import jobs.steps.helpers.ColumnConfigurator
import jobs.steps.helpers.OptionalBinningColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.projections.Projection

abstract class CategoricalOrBinnedJob extends AbstractLowDimensionalAnalysisJob implements InitializingBean {

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    abstract ColumnConfigurator getIndependentVariableConfigurator()

    abstract ColumnConfigurator getDependentVariableConfigurator()

    protected void configureConfigurator(OptionalBinningColumnConfigurator configurator,
                                         String keyBinPart,
                                         String keyVariablePart,
                                         String header = null) {
        if (header != null) {
            configurator.header = header
        }
        configurator.projection            = Projection.LOG_INTENSITY_PROJECTION

        configurator.multiRow              = true

        configurator.keyForConceptPaths    = "${keyVariablePart}Variable"
        configurator.keyForDataType        = "div${keyVariablePart.capitalize()}VariableType"
        configurator.keyForSearchKeywordId = "div${keyVariablePart.capitalize()}VariablePathway"

        BinningColumnConfigurator binningColumnConfigurator =
                configurator.binningConfigurator
        binningColumnConfigurator.keyForDoBinning       = "binning${keyBinPart.capitalize()}"
        binningColumnConfigurator.keyForManualBinning   = "manualBinning${keyBinPart.capitalize()}"
        binningColumnConfigurator.keyForNumberOfBins    = "numberOfBins${keyBinPart.capitalize()}"
        binningColumnConfigurator.keyForBinDistribution = "binDistribution${keyBinPart.capitalize()}"
        binningColumnConfigurator.keyForBinRanges       = "binRanges${keyBinPart.capitalize()}"
        binningColumnConfigurator.keyForVariableType    = "variableType${keyBinPart.capitalize()}"
    }


    @Override
    protected List<ColumnConfigurator> getColumnConfigurators() {
        return [
            independentVariableConfigurator,
            dependentVariableConfigurator,
        ]
    }

}
