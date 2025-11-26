package net.causw.app.main.core.datasourceProxy;

import java.util.List;
import java.util.stream.Collectors;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.springframework.stereotype.Component;

@Component
public class ApiQueryCountListener implements QueryExecutionListener {

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        for (QueryInfo queryInfo : queryInfoList) {
            QueryContext.addQuery(new QueryContext.QueryInfo(
                queryInfo.getQuery(),
                execInfo.getElapsedTime(),
                extractParams(queryInfo)
            ));
        }
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {}

    private List<Object> extractParams(QueryInfo queryInfo) {
        return queryInfo.getParametersList().stream()
            .flatMap(params -> params.stream())
            .map(param -> ((ParameterSetOperation) param).getArgs()[1])
            .collect(Collectors.toList());
    }
}
