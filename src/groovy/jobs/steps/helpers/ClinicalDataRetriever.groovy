package jobs.steps.helpers

import jobs.table.Table
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.clinical.ClinicalDataResource
import org.transmartproject.core.dataquery.clinical.ClinicalVariable
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.PatientRow

@Component
@Scope('job')
class ClinicalDataRetriever {

    // work with only this case now
    //boolean joinSubsets = true

    public static final String DATA_SOURCE_NAME = 'Clinical Data'

    private List<ClinicalVariable> variables = []

    private boolean resultsGiven,
                    attachedToTable

    @Autowired
    private ClinicalDataResource clinicalDataResource

    @Autowired
    private ResultInstanceIdsHolder resultInstanceIdsHolder


    void leftShift(ClinicalVariable var) {
        if (resultsGiven) {
            throw new IllegalStateException(
                    'Cannot add column, results already opened')
        }
        variables << var
    }

    void leftShift(String conceptPath) {
        this << createVariableFromConceptPath(conceptPath)
    }

    ClinicalVariable createVariableFromConceptPath(String conceptPath) {
        clinicalDataResource.createClinicalVariable(
                ClinicalVariable.TERMINAL_CONCEPT_VARIABLE,
                concept_path: conceptPath)
    }

    List<ClinicalVariable> getVariables() {
        Collections.unmodifiableList(variables)
    }

    TabularResult<ClinicalVariableColumn, PatientRow> getResults() {
        if (!variables) {
            throw new IllegalStateException('No variables provided')
        }
        if (resultsGiven) {
            throw new IllegalStateException('Results already provided')
        }

        resultsGiven = true

//        if (joinSubsets) {
            clinicalDataResource.retrieveData(
                    resultInstanceIdsHolder.queryResults, variables)
//        } else {
//            List<TabularResult> res = []
//            try {
//                resultInstanceIdsHolder.queryResults.each {
//                    res << clinicalDataResource.retrieveData(it, variables)
//                }
//            } catch (Exception e) {
//                res.each { it.close() }
//                throw e
//            }
//            res
//        }
    }

    void attachToTable(Table table) {
        if (attachedToTable) {
            return
        }

        table.addDataSource(DATA_SOURCE_NAME,
                new LazyDataSource(makeDataSource: this.&getResults))

        attachedToTable = true
    }

    static class LazyDataSource implements Iterable, AutoCloseable {

        Closure<Iterable> makeDataSource

        private Iterable storedDataSource

        @Override
        void close() throws Exception {
            if (storedDataSource == null) {
                return
            }

            if (dataSource instanceof Closeable) {
                ((Closeable) dataSource).close()
            } else if (dataSource instanceof AutoCloseable) {
                ((AutoCloseable) dataSource).close()
            }
        }

        Iterable getDataSource() {
            if (storedDataSource == null) {
                storedDataSource = makeDataSource()
                assert storedDataSource != null
            }
            storedDataSource
        }

        @Override
        Iterator iterator() {
            dataSource.iterator()
        }

        @Override
        def invokeMethod(String name, args) {
            dataSource.invokeMethod(name, args)
        }
    }
}
