package com.softawii.capivara.config;

import com.softawii.capivara.listeners.PackageGroup;
import com.softawii.curupira.core.Curupira;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("com.softawii.capivara.repository")
@PropertySource("classpath:application.properties")
@PropertySource(value = "${spring.config.location}", ignoreResourceNotFound = true)
public class SpringConfig {

    private final Environment env;

    public SpringConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.softawii.capivara.entity","com.softawii.capivara.repository", "com.softawii.capivara.services");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    @Value("${token}")
    private String discordToken;

    @Bean
    public JDA jda() {
        JDA jda;
        try {
            JDABuilder builder = JDABuilder.create(discordToken, GatewayIntent.GUILD_MEMBERS);
            builder.enableIntents(GatewayIntent.GUILD_EMOJIS);
            builder.setMemberCachePolicy(MemberCachePolicy.ALL);
            builder.enableCache(CacheFlag.EMOTE, CacheFlag.ROLE_TAGS, CacheFlag.MEMBER_OVERRIDES);
            builder.addEventListeners(new PackageGroup.AutoCompleter());
            jda = builder.build();
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return jda;
    }

    @Bean
    public Curupira curupira() {
        JDA jda = jda();
        String pkg   = "com.softawii.capivara.listeners";
        String resetEnv = env.getProperty("curupira.reset", "false");
        boolean reset = Boolean.parseBoolean(resetEnv);
        Curupira curupira = new Curupira(jda, reset, pkg);

        return curupira;
    }


    Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.setProperty("hibernate.dialect", env.getProperty("spring.jpa.database-platform"));
        return properties;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}
