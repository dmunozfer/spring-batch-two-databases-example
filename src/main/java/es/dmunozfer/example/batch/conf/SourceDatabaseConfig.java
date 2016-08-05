package es.dmunozfer.example.batch.conf;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.init.Jackson2RepositoryPopulatorFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = SourceDatabaseConfig.ENTITY_MANAGER_FACTORY, transactionManagerRef = SourceDatabaseConfig.TRANSACTION_MANAGER, basePackageClasses = es.dmunozfer.example.batch.data.source.PackageMarker.class)
public class SourceDatabaseConfig {

	public static final String DATASOURCE_NAME = "sourceDatasource";
	public static final String ENTITY_MANAGER_FACTORY = "sourceEntityManagerFactory";
	public static final String TRANSACTION_MANAGER = "sourceTransactionManager";
	public static final String DATASOURCE_PROPERTY_PREFIX = "datasource.source";

	private static final boolean GENERATE_DDL = true;

	@Bean(name = DATASOURCE_NAME)
	@ConfigurationProperties(prefix = DATASOURCE_PROPERTY_PREFIX)
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = ENTITY_MANAGER_FACTORY)
	LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
		jpaVendorAdapter.setGenerateDdl(GENERATE_DDL);

		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		DataSource ds = dataSource();
		factoryBean.setDataSource(ds);
		factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
		factoryBean.setPackagesToScan(es.dmunozfer.example.batch.data.source.PackageMarker.class.getPackage().getName());

		return factoryBean;
	}

	@Bean(name = TRANSACTION_MANAGER)
	PlatformTransactionManager transactionManager() {
		return new JpaTransactionManager(entityManagerFactory().getObject());
	}

	// Initialize database
	@Bean
	public Jackson2RepositoryPopulatorFactoryBean repositoryPopulator() {
		Resource sourceData = new ClassPathResource("source-data.json");
		Jackson2RepositoryPopulatorFactoryBean factory = new Jackson2RepositoryPopulatorFactoryBean();
		factory.setResources(new Resource[] { sourceData });
		return factory;
	}
}
