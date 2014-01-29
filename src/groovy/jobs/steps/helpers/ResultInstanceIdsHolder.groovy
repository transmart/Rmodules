package jobs.steps.helpers

import jobs.UserParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryResult

@Component
@Scope('job')
class ResultInstanceIdsHolder {

    @Autowired
    UserParameters params

    @Autowired
    QueriesResource queriesResource

    def keysForResultInstanceIds = ['result_instance_id1', 'result_instance_id2']

    @Lazy List<Long> resultInstanceIds = {
        def r = keysForResultInstanceIds.collect { key ->
            def v = params[key]
            if (v && !v.isLong()) {
                throw new InvalidArgumentsException("Invalid value for $key: '$v'")
            }

            v ? (v as Long) : null
        }.findAll()

        if (!r) {
            throw new InvalidArgumentsException("No result instance ids provided. " +
                    "Check the parameters $keysForResultInstanceIds")
        }

        r
    }()

    @Lazy List<QueryResult> queryResults = {
        resultInstanceIds.collect { id ->
            queriesResource.getQueryResultFromId id
        }
    }()
}
