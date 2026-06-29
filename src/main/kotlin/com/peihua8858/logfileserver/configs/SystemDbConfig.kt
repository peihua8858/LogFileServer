package com.peihua8858.logfileserver.configs

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import javax.sql.DataSource


@Configuration
@MapperScan(
    basePackages = ["com.peihua8858.logfileserver.mappers.system"],
    sqlSessionTemplateRef = "systemSqlSessionTemplate"
)
class SystemDbConfig {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.system")
    fun systemDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean(name = ["systemDataSource"])
    @Primary
    fun systemDataSource(
        @Qualifier("systemDataSourceProperties") properties: DataSourceProperties
    ): DataSource? {
        return properties.initializeDataSourceBuilder().build()
    }

    @Bean(name = ["systemSqlSessionFactory"])
    @Primary
    @Throws(Exception::class)
    fun systemSqlSessionFactory(
        @Qualifier("systemDataSource") dataSource: DataSource?
    ): SqlSessionFactory? {
        val factoryBean = MybatisSqlSessionFactoryBean()
        factoryBean.setDataSource(dataSource)
        return factoryBean.getObject()
    }

    @Bean(name = ["systemSqlSessionTemplate"])
    @Primary
    fun systemSqlSessionTemplate(
        @Qualifier("systemSqlSessionFactory") sqlSessionFactory: SqlSessionFactory
    ): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }

    @Bean(name = ["systemTransactionManager"])
    @Primary
    fun systemTransactionManager(
        @Qualifier("systemDataSource") dataSource: DataSource
    ): DataSourceTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }
}