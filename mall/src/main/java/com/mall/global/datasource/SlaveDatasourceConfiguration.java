package com.mall.global.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class SlaveDatasourceConfiguration {

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

    @Value("${spring.datasource.druid.slave.url}")
    private String url;

    @Value("${spring.datasource.druid.slave.username}")
    private String username;

    @Value("${spring.datasource.druid.slave.password}")
    private String password;

    @Value("${spring.datasource.druid.slave.driver-class-name}")
    private String driverClassName;
    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.slave")//我们配置文件中的前缀
    public DruidDataSource getSlaveDateSource() {
        return DruidDataSourceBuilder.create().build();
    }


    @Bean(name = "slaveSqlSessionFactory")
    public SqlSessionFactory primarySqlSessionFactory(@Qualifier("slaveDataSource") DruidDataSource datasource)
            throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(getSlaveDateSource());
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


    @Bean("slaveSqlSessionTemplate")
    public SqlSessionTemplate primarySqlSessionTemplate(
            @Qualifier("slaveSqlSessionFactory") SqlSessionFactory slaveSqlSessionFactory) {
        return new SqlSessionTemplate(slaveSqlSessionFactory);
    }

}