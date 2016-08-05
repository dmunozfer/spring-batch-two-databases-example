package es.dmunozfer.example.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import es.dmunozfer.example.batch.conf.DestinationDatabaseConfig;
import es.dmunozfer.example.batch.conf.SourceDatabaseConfig;
import es.dmunozfer.example.batch.data.destination.DestinationEntity;
import es.dmunozfer.example.batch.data.source.SourceEntity;
import es.dmunozfer.example.batch.processor.CustomItemProcessor;

@Configuration
@EnableAutoConfiguration
public class TwoDatabaseExampleJobConfiguration {
	private static final String JOB_NAME = "twoDatabaseExampleJob";
	private static final int READER_PAGE_SIZE = 50;
	private static final int CHUNK_COMMIT_SIZE = 100;
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	@Qualifier(SourceDatabaseConfig.ENTITY_MANAGER_FACTORY)
	LocalContainerEntityManagerFactoryBean sourceEntityManagerFactory;
	
	@Autowired
	@Qualifier(DestinationDatabaseConfig.ENTITY_MANAGER_FACTORY)
	LocalContainerEntityManagerFactoryBean destinationEntityManagerFactory;
	
    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<SourceEntity> reader,
                      ItemWriter<DestinationEntity> writer, ItemProcessor<SourceEntity, DestinationEntity> processor) {
        return stepBuilderFactory.get("step1")
                .<SourceEntity, DestinationEntity>chunk(CHUNK_COMMIT_SIZE)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    
	@Bean
	public Job job(Step step1) throws Exception {
		return jobBuilderFactory.get(JOB_NAME)			
				.incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
				.build();
	}

	@Bean(destroyMethod="")
	public ItemReader<SourceEntity> reader() throws Exception {
		String jpqlQuery = "select se from SourceEntity se";

		JpaPagingItemReader<SourceEntity> reader = new JpaPagingItemReader<SourceEntity>();
		reader.setQueryString(jpqlQuery);
		reader.setEntityManagerFactory(sourceEntityManagerFactory.getObject());
		reader.setPageSize(READER_PAGE_SIZE);
		reader.afterPropertiesSet();
		reader.setSaveState(true);

		return reader;
	}

	@Bean
	public ItemProcessor<SourceEntity, DestinationEntity> processor() {
		return new CustomItemProcessor();
	}

	@Bean
	public ItemWriter<DestinationEntity> writer() {
		JpaItemWriter<DestinationEntity> writer = new JpaItemWriter<DestinationEntity>();
		writer.setEntityManagerFactory(destinationEntityManagerFactory.getObject());
		return writer;
	}
}