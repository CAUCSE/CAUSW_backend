package net.causw.app.main.core.datasourceProxy;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatasourceProxyConfig {

    @Bean
    @Primary
    // 기본 HikaridataSource를 Proxy로 감싸서 Datasource 생성
    public DataSource dataSource(DataSourceProperties properties, ApiQueryCountListener listener) {
        HikariDataSource hikari = properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();

        return ProxyDataSourceBuilder.create(hikari)
            .listener(listener)
            .build();
    }
}
