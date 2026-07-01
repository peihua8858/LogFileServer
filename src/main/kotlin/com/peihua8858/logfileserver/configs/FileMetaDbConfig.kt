package com.peihua8858.logfileserver.configs

import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver
import com.baomidou.mybatisplus.core.config.GlobalConfig
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector
import com.baomidou.mybatisplus.core.injector.ISqlInjector
import com.baomidou.mybatisplus.extension.incrementer.H2KeyGenerator
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.type.JdbcType
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource
import kotlin.io.path.Path


@Configuration
@MapperScan(
    basePackages = ["com.peihua8858.logfileserver.mappers.filemeta"],
    sqlSessionTemplateRef = "fileMetaSqlSessionTemplate"
)
class FileMetaDbConfig(private val properties: AppProperties) {

    @Bean(name = ["fileMetaDataSource"])
    fun fileMetaDataSource(): DataSource? {
        val dbPath = Path(properties.dataDir, "db", "filemeta.db")
            .toAbsolutePath()
            .normalize()

        val dataSource = SQLiteDataSource()
        dataSource.url = "jdbc:sqlite:$dbPath"
        return dataSource
    }

    @Bean(name = ["fileMetaSqlSessionFactory"])
    @Throws(Exception::class)
    fun fileMetaSqlSessionFactory(
        @Qualifier("fileMetaDataSource") dataSource: DataSource?
    ): SqlSessionFactory? {
        val factoryBean = MybatisSqlSessionFactoryBean()
        factoryBean.setDataSource(dataSource)
        val globalConfig = GlobalConfig();
        globalConfig.metaObjectHandler = metaObjectHandler();
        globalConfig.sqlInjector = sqlInjector();
        val dbConfig = GlobalConfig.DbConfig();
        dbConfig.keyGenerators = listOf(keyGenerator());
        globalConfig.dbConfig = dbConfig;
        val configuration = MybatisConfiguration();
        configuration.setDefaultScriptingLanguage(MybatisXMLLanguageDriver::class.java);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        //        configuration.addInterceptor(performanceInterceptor());
//        configuration.addInterceptor(paginationInterceptor());
        factoryBean.configuration = configuration;
        factoryBean.setGlobalConfig(globalConfig);
        //设置mapper.xml文件路径
        factoryBean.setMapperLocations();
//        factoryBean.setMapperLocations(
//            resolveMapperLocations(
//                "classpath:mapper/primary/*.xml",
//                "classpath:dao/primary/*.xml"
//            )
//        );
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

    @Bean
    fun metaObjectHandler(): MetaObjectHandler {
        return MyMetaObjectHandler()
    }

    /**
     * 注入主键生成器
     */
    @Bean
    fun keyGenerator(): IKeyGenerator {
        return H2KeyGenerator()
    }

    /**
     * 注入sql注入器
     */
    @Bean
    fun sqlInjector(): ISqlInjector {
        return DefaultSqlInjector()
    }

}