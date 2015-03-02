package jobs.steps.helpers

import jobs.table.Column
import jobs.table.columns.CategoricalVariableColumn
import jobs.table.columns.ConstantValueColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.clinical.ClinicalVariable

@Component
@Scope('prototype')
class CategoricalColumnConfigurator extends ColumnConfigurator {

    String keyForConceptPaths

    @Autowired
    private ClinicalDataRetriever clinicalDataRetriever

    @Override
    protected void doAddColumn(Closure<Column> decorateColumn) {

        List<String> conceptPaths = getConceptPaths()

        if (conceptPaths) {
            Set<ClinicalVariable> variables =
                    conceptPaths.collect {
                        clinicalDataRetriever.createVariableFromConceptPath it.trim()
                    }

            variables = variables.collect {
                clinicalDataRetriever << it
            }

            clinicalDataRetriever.attachToTable table

            table.addColumn(
                    decorateColumn.call(
                            new CategoricalVariableColumn(
                                    header:    header,
                                    leafNodes: variables)),
                    [ClinicalDataRetriever.DATA_SOURCE_NAME] as Set)
        } else {
            //optional, empty value column
            table.addColumn(new ConstantValueColumn(
                            header: header,
                            missingValueAction: missingValueAction),
                    Collections.emptySet())
        }
    }

    List<String> getConceptPaths() {
        //if required this will fail on empty conceptPaths
        getStringsListParam(keyForConceptPaths, required)
    }
}
