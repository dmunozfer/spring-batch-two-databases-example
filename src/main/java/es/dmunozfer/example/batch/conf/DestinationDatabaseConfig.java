package es.dmunozfer.example.batch.conf;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = DestinationDatabaseConfig.ENTITY_MANAGER_FACTORY, transactionManagerRef = DestinationDatabaseConfig.TRANSACTION_MANAGER, basePackageClasses = es.dmunozfer.example.batch.data.destination.PackageMarker.class)
public class DestinationDatabaseConfig {

	public static final String DATASOURCE_NAME = "destinationDatasource";
	public static final String ENTITY_MANAGER_FACTORY = "destinationEntityManagerFactory";
	public static final String TRANSACTION_MANAGER = "destinationTransactionManager";
	public static final String DATASOURCE_PROPERTY_PREFIX = "datasource.destination";

	private static final boolean GENERATE_DDL = true;

	@Primary // Default datasource (spring batch schema)
	@Bean(name = DATASOURCE_NAME)
	@ConfigurationProperties(prefix = DATASOURCE_PROPERTY_PREFIX)
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Primary // Default datasource (spring batch schema)
	@Bean(name = ENTITY_MANAGER_FACTORY)
	LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
		jpaVendorAdapter.setGenerateDdl(GENERATE_DDL);

		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource());
		factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
		factoryBean.setPackagesToScan(es.dmunozfer.example.batch.data.destination.PackageMarker.class.getPackage().getName());

		return factoryBean;
	}

	@Primary // Default datasource (spring batch schema)
	@Bean(name = TRANSACTION_MANAGER)
	PlatformTransactionManager transactionManager() {
		return new JpaTransactionManager(entityManagerFactory().getObject());
	}
}
