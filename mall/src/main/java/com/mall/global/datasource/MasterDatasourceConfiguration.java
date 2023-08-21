package com.mall.global.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;


@Configuration
public class MasterDatasourceConfiguration {

    @Value("${mybatis.type-aliases-package}")
    private String typeAliasesPackage;

    @Value("${mybatis.mapper-locations}")
    private String mapperLocations;

    @Value("${mybatis.configuration.log-impl}")
    private String logImpl;

    @Value("${mybatis.configuration.use-generated-keys}")
    private boolean useGeneratedKeys;

    @Value("${mybatis.configuration.map-underscore-to-camel-case}")
    private boolean mapUnderscoreToCamelCase;

    @Value("${spring.datasource.druid.master.url}")
    private String url;

    @Value("${spring.datasource.druid.master.username}")
    private String username;

    @Value("${spring.datasource.druid.master.password}")
    private String password;

    @Value("${spring.datasource.druid.master.driver-class-name}")
    private String driverClassName;

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.druid.master")//我们配置文件中的前缀
    public DruidDataSource getMasterSource() {
        DruidDataSource druidDataSource= DruidDataSourceBuilder.create().build();
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        druidDataSource.setDriverClassName(driverClassName);
        return druidDataSource;
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory masterSqlSessionFactory()  {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(getMasterSource());
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            factoryBean.setMapperLocations(resolver.getResources(mapperLocations));
            factoryBean.setTypeAliasesPackage(typeAliasesPackage);
            factoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCase(mapUnderscoreToCamelCase);
            factoryBean.getObject().getConfiguration().setCacheEnabled(true);
            factoryBean.getObject().getConfiguration().setLazyLoadingEnabled(true);
            factoryBean.getObject().getConfiguration().setAggressiveLazyLoading(false);

            factoryBean.getObject().getConfiguration().setLogPrefix("###SPRING_BOOT###MYBATIS###");
            factoryBean.getObject().getConfiguration().setDefaultExecutorType(ExecutorType.REUSE);
            factoryBean.getObject().getConfiguration().setUseGeneratedKeys(useGeneratedKeys);
            return factoryBean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SqlSessionTemplate primarySqlSessionTemplate() {
        return new SqlSessionTemplate(this.masterSqlSessionFactory());
    }

//    @Bean("db1TransactionManager")
//    @Primary // 默认使用事务的数据源
//    public DataSourceTransactionManager db1TransactionManager(@Qualifier("db1DataSource") DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }

}