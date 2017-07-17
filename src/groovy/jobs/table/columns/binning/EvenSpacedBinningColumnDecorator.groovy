package jobs.table.columns.binning

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import jobs.table.Column
import jobs.table.columns.ColumnDecorator
import org.mapdb.Fun

import java.text.DecimalFormat

@CompileStatic
class EvenSpacedBinningColumnDecorator implements ColumnDecorator {

    @Delegate
    Column inner

    int numberOfBins

    private Map<String, Number> min = [:].withDefault { Double.POSITIVE_INFINITY },
                                max = [:].withDefault { Double.NEGATIVE_INFINITY }

    DecimalFormat decimalFormat = new DecimalFormat()

    private Map<String, List<Map>> binsByContext = { ->
        Map<String, List> ret = [:]
        ret.withDefault { String ctx ->
            Number minNumber = min[ctx]
            Number stepRange = ((max[ctx] - minNumber) / numberOfBins)
            ret[ctx] = (1..numberOfBins).collect { Integer it ->
                Number lowerBound = minNumber + stepRange * (it - 1)
                String lowerBoundString = decimalFormat.format(lowerBound)
                Number upperBound = minNumber + stepRange * it
                String upperBoundString = decimalFormat.format(upperBound)
                String op = it == 1 ? '<=' : '<'
                [
                        min: lowerBound,
                        max: upperBound,
                        label: "${lowerBoundString} ${op} ${header} <= ${upperBoundString}" as String
                ]

            }
        }
    }()

    private Map<String, BigDecimal> inverseBinInterval = {
        def ret = [:]
        ret.withDefault { String ctx ->
            ret[ctx] = numberOfBins / (max[ctx] - min[ctx])
        }
    }()

    private void considerValue(String ctx, Number value) {
        if (value < min[ctx]) {
            min[ctx] = value
        }
        if (value > max[ctx]) {
            max[ctx] = value
        }
    }

    private void considerValue(String ctx, Object value) {
        considerValue(ctx, value as BigDecimal)
    }

    @CompileStatic(TypeCheckingMode.SKIP) // multi-dispatch
    private void considerValue(String ctx, Map<String, Object> value) {
        /* otherwise found map inside map inside consumeResultingTableRows()'s map? */
        assert ctx == ''
        for (entry in value) {
            considerValue entry.key, entry.value
        }
    }

    @CompileStatic(TypeCheckingMode.SKIP) // multi-dispatch
    Map<String, Object> consumeResultingTableRows() {
        Map<String, Object> innerResult = inner.consumeResultingTableRows()

        for (entry in innerResult) {
            assert entry.value != null /* otherwise violates contract of consumeRTR() */
            considerValue '', entry.value
        }

        innerResult
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    private String transform(String ctx, Number value) {
        List<Map> bins = binsByContext[ctx]
        bins.find { it.min <= value && it.max >= value }.label
    }

    // NOTE: assumes there's no transformer in inner
    Closure<Object> getValueTransformer() {
        { Fun.Tuple3<String, Integer, String> key, Object value ->
            (Object) transform(key.c,
                    (Number) ((value instanceof Number) ? value : (value as BigDecimal)))
        }
    }

    void beforeDataSourceIteration(String dataSourceName, Iterable dataSource) {
        // just for validation
        if (!header) {
            throw new IllegalStateException('Bug: header not set here')
        }

        inner.beforeDataSourceIteration dataSourceName, dataSource
    }
}
