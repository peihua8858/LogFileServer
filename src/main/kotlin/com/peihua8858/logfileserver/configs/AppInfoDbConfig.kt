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
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import javax.sql.DataSource


@Configuration
@MapperScan(
    basePackages = ["com.peihua8858.logfileserver.mappers.appinfo"],
    sqlSessionTemplateRef = "appInfoSqlSessionTemplate"
)
class AppInfoDbConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.appinfo")
    fun appInfoDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean(name = ["appInfoDataSource"])
    fun appInfoDataSource(
        @Qualifier("appInfoDataSourceProperties") properties: DataSourceProperties
    ): DataSource? {
        return properties.initializeDataSourceBuilder().build()
    }

    @Bean(name = ["appInfoSqlSessionFactory"])
    @Throws(Exception::class)
    fun appInfoSqlSessionFactory(
        @Qualifier("appInfoDataSource") dataSource: DataSource?
    ): SqlSessionFactory? {
        val factoryBean = MybatisSqlSessionFactoryBean()
        factoryBean.setDataSource(dataSource)
        return factoryBean.getObject()
    }

    @Bean(name = ["appInfoSqlSessionTemplate"])
    fun appInfoSqlSessionTemplate(
        @Qualifier("appInfoSqlSessionFactory") sqlSessionFactory: SqlSessionFactory
    ): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }

    @Bean(name = ["appInfoTransactionManager"])
    fun appInfoTransactionManager(
        @Qualifier("appInfoDataSource") dataSource: DataSource
    ): DataSourceTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }
}