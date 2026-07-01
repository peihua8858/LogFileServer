package com.peihua8858.logfileserver.configs

import com.peihua8858.logfileserver.data.DataStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.*
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.io.File
import java.util.*

@Configuration
class WebConfig(
    private val properties: AppProperties
) : WebMvcConfigurer {
//    override fun configurePathMatch(configurer: PathMatchConfigurer) {
//        configurer.setUseSuffixPatternMatch(false).setUseTrailingSlashMatch(true)
//    }
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/docs/**").addResourceLocations("classpath:/docs/")
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/")
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
        try {
            val parentFile: File = properties.dataDirFile
            val keystoreFile = File(parentFile, DataStore.KEYSTORE_DIR)
            val uploadParentPath = parentFile.absolutePath + File.separatorChar
            val keystoreParentPath = keystoreFile.absolutePath + File.separatorChar
            registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + parentFile.absolutePath)
            registry.addResourceHandler("/keystore/**")
                .addResourceLocations("file:" + keystoreFile.absolutePath)
            registry.addResourceHandler(
                "/files/**",
                "/files/**/*.html",
                "/keystore/**",
                "**/*.html"
            ).addResourceLocations(
                    "file:" + parentFile.absolutePath, "classpath:/templates/",
                    "file:$uploadParentPath", "file:$keystoreParentPath"
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
//    override fun addViewControllers(registry: ViewControllerRegistry) {
//
//        registry.addViewController("/logcat/logconsole").setViewName("logcat/logconsole")
//        registry.addViewController("/logcat/logconsole.html").setViewName("logcat/logconsole")
//    }
    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("abc-")
        executor.keepAliveSeconds = 60
        executor.initialize()
        configurer.setTaskExecutor(executor)
        //因上传数据需要时间比较久
        configurer.setDefaultTimeout(30000)
    }
    /**
     * 开启跨域
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        // 设置允许跨域的路由
        registry.addMapping("/**") // 设置允许跨域请求的域名
            .allowedOriginPatterns("*") // 是否允许证书（cookies）
            .allowCredentials(true) // 设置允许的方法
            .allowedMethods("*") // 跨域允许时间
            .maxAge(3600)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeChangeInterceptor())
//        registry.addInterceptor(deviceResolverHandlerInterceptor())
//        registry.addInterceptor(sitePreferenceHandlerInterceptor())
    }
    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
//        argumentResolvers.add(deviceHandlerMethodArgumentResolver())
//        argumentResolvers.add(sitePreferenceHandlerMethodArgumentResolver())
    }

    @Bean
    fun localeResolver(): LocaleResolver {
        val slr = SessionLocaleResolver()

        slr.setDefaultLocale(Locale.CHINA)
        return slr
    }

    @Bean
    fun localeChangeInterceptor(): LocaleChangeInterceptor {
        val lci = LocaleChangeInterceptor()
        lci.setParamName("lang")
        return lci
    }


    /**
     * 配置servlet处理
     */
    override fun configureDefaultServletHandling(configurer: DefaultServletHandlerConfigurer) {
        //不可加上这句，否则全局异常无效
//        configurer.enable();
    }

//    @Bean
//    fun deviceResolverHandlerInterceptor(): DeviceResolverHandlerInterceptor? {
//        return DeviceResolverHandlerInterceptor()
//    }
//
//    @Bean
//    fun deviceHandlerMethodArgumentResolver(): DeviceHandlerMethodArgumentResolver? {
//        return DeviceHandlerMethodArgumentResolver()
//    }
//
//
//    @Bean
//    fun sitePreferenceHandlerInterceptor(): SitePreferenceHandlerInterceptor? {
//        return SitePreferenceHandlerInterceptor()
//    }
//
//    @Bean
//    fun sitePreferenceHandlerMethodArgumentResolver(): SitePreferenceHandlerMethodArgumentResolver? {
//        return SitePreferenceHandlerMethodArgumentResolver()
//    }
}
