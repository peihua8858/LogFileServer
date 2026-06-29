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
    basePackages = ["com.peihua8858.logfileserver.mappers.filemeta"],
    sqlSessionTemplateRef = "fileMetaSqlSessionTemplate"
)
class FileMetaDbConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.filemeta")
    fun fileMetaDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean(name = ["fileMetaDataSource"])
    fun fileMetaDataSource(
        @Qualifier("fileMetaDataSourceProperties") properties: DataSourceProperties
    ): DataSource? {
        return properties.initializeDataSourceBuilder().build()
    }

    @Bean(name = ["fileMetaSqlSessionFactory"])
    @Throws(Exception::class)
    fun fileMetaSqlSessionFactory(
        @Qualifier("fileMetaDataSource") dataSource: DataSource?
    ): SqlSessionFactory? {
        val factoryBean = MybatisSqlSessionFactoryBean()
        factoryBean.setDataSource(dataSource)
        return factoryBean.getObject()
    }

    @Bean(name = ["fileMetaSqlSessionTemplate"])
    fun fileMetaSqlSessionTemplate(
        @Qualifier("fileMetaSqlSessionFactory") sqlSessionFactory: SqlSessionFactory
    ): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }

    @Bean(name = ["fileMetaTransactionManager"])
    fun fileMetaTransactionManager(
        @Qualifier("fileMetaDataSource") dataSource: DataSource
    ): DataSourceTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }
}