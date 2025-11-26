package net.causw.app.main.core.datasourceProxy;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryContext {
    private static final ThreadLocal<List<QueryInfo>> queries =
        ThreadLocal.withInitial(ArrayList::new);

    public static void addQuery(QueryInfo queryInfo) {
        queries.get().add(queryInfo);
    }

    public static List<QueryInfo> getQueries() {
        return new ArrayList<>(queries.get());
    }

    public static void clear() {
        queries.remove();
    }

    @Data
    @AllArgsConstructor
    public static class QueryInfo {
        private String query;
        private long executionTime;
        private List<Object> params;
    }
}
