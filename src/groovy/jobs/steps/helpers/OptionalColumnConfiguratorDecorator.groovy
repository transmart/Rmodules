package jobs.steps.helpers

import jobs.table.Column
import jobs.table.columns.ConstantValueColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope('prototype')
class OptionalColumnConfiguratorDecorator extends ColumnConfigurator {

    @Autowired
    ApplicationContext ctx

    ColumnConfigurator generalCase

    ColumnConfigurator fallback

    /* if this param is non empty, it will be considered provided */
    String keyForEnabled

    @Override
    protected void doAddColumn(Closure<Column> decorateColumn) {
        if (!configuratorToUse.header) {
            configuratorToUse.header = header
        }

        configuratorToUse.doAddColumn decorateColumn
    }

    private ColumnConfigurator getConfiguratorToUse() {
        if (getStringParam(keyForEnabled, false)) {
            generalCase
        } else {
            fallback
        }
    }

    @Override
    List<String> getConceptPaths() {
        configuratorToUse.conceptPaths
    }

    void setConstantColumnFallback(Object columnValue) {
        if (!header) {
            throw new IllegalStateException(
                    'Set this configurator\'s header before calling this method')
        }
        SimpleAddColumnConfigurator c = ctx.getBean(SimpleAddColumnConfigurator)
        c.column = new ConstantValueColumn(header: header,
                                           value:  columnValue)
        fallback = c
    }
}
