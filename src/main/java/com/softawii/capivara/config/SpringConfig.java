package com.softawii.capivara.config;

import com.softawii.capivara.utils.CapivaraExceptionHandler;
import com.softawii.curupira.core.Curupira;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("com.softawii.capivara.repository")
@PropertySource("classpath:application.properties")
@PropertySource(value = "${spring.config.location}", ignoreResourceNotFound = true)
public class SpringConfig {

    private static final Logger      LOGGER = LoggerFactory.getLogger(SpringConfig.class);
    private final        Environment env;
    @Value("${token}")
    private              String      discordToken;

    public SpringConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.softawii.capivara.entity",
                             "com.softawii.capivara.repository",
                             "com.softawii.capivara.services",
                             "com.softawii.capivara.listeners.events");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    @Bean
    public JDA jda() {
        JDA jda;
        try {
            JDABuilder builder = JDABuilder.create(
                    discordToken,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT);
            builder.setMemberCachePolicy(MemberCachePolicy.ALL);
            builder.enableCache(CacheFlag.EMOJI, CacheFlag.ROLE_TAGS, CacheFlag.MEMBER_OVERRIDES, CacheFlag.STICKER);
            jda = builder.build();
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return jda;
    }

    @Bean
    public CapivaraExceptionHandler capivaraExceptionHandler() {
        String logChannelId = env.getProperty("log.channel.id");
        String logDirectory = env.getProperty("log_directory");
        if (logChannelId != null) {
            Path logPath = null;
            if (logDirectory != null) {
                logPath = Path.of(logDirectory);
            }
            return new CapivaraExceptionHandler(logChannelId, logPath);
        }

        return null;
    }

    @Bean
    public Curupira curupira(JDA jda, @Autowired(required = false) CapivaraExceptionHandler exceptionHandler) {
        String  pkg      = "com.softawii.capivara.listeners";
        String  resetEnv = env.getProperty("curupira.reset", "false");
        boolean reset    = Boolean.parseBoolean(resetEnv);
        LOGGER.info("curupira.reset: " + reset);

        return new Curupira(jda, reset, exceptionHandler, pkg);
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
