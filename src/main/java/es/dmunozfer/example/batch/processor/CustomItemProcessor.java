package es.dmunozfer.example.batch.processor;

import org.springframework.batch.item.ItemProcessor;

import es.dmunozfer.example.batch.data.destination.DestinationEntity;
import es.dmunozfer.example.batch.data.source.SourceEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomItemProcessor implements ItemProcessor<SourceEntity, DestinationEntity> {

	@Override
	public DestinationEntity process(final SourceEntity sourceEntity) throws Exception {
		DestinationEntity destinationEntity = new DestinationEntity();
		destinationEntity.setName(sourceEntity.getName());
		log.info("Process: sourceEntity=[{}] destinationEntity=[{}]", sourceEntity, destinationEntity);
		return destinationEntity;
	}

}
