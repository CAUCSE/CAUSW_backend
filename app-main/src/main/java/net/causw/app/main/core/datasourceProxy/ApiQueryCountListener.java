package net.causw.app.main.core.datasourceProxy;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

@Component
public class ApiQueryCountListener implements QueryExecutionListener {

	@Override
	public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
		if (queryInfoList.isEmpty()) {
			return;
		}

		String query = queryInfoList.stream()
			.map(QueryInfo::getQuery)
			.collect(Collectors.joining(";\n"));

		List<Object> params = queryInfoList.stream()
			.flatMap(info -> extractParams(info).stream())
			.collect(Collectors.toList());

		QueryContext.addQuery(new QueryContext.QueryInfo(
			query,
			execInfo.getElapsedTime(),
			params));
	}

	@Override
	public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {}

	private List<Object> extractParams(QueryInfo queryInfo) {
		return queryInfo.getParametersList().stream()
			.flatMap(params -> params.stream())
			.map(param -> ((ParameterSetOperation)param).getArgs()[1])
			.collect(Collectors.toList());
	}
}
