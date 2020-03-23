package guru.sfg.beer.order.service.config;

import javax.jms.Destination;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import guru.sfg.beer.order.service.sm.action.ValidateOrderAction;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
public class JmsConfig {

    public static final String MY_QUEUE = "MY_QUEUE";
    
    public static final String MY_SEND_RCV_QUEUE = "MY_SEND_RCV_QUEUE";

	public static final String VALIDATE_ORDER_QUEUE = "VALIDATE_ORDER_QUEUE";
	
	public static final String ALLOCATE_ORDER_QUEUE = "ALLOCATE_ORDER_QUEUE";

	public static final String ALLOCATE_FAILURE_QUEUE = "ALLOCATE_FAILURE_QUEUE";

	public static final String DEALLOCATE_ORDER_QUEUE = "DEALLOCATE_ORDER_QUEUE";

	public static final String  VALIDATE_FAILURE_QUEUE = "VALIDATE_FAILURE_QUEUE";
	
	public static final String VALIDATE_ORDER_RESPONSE_QUEUE = "VALIDATE_ORDER_RESPONSE_QUEUE";

    @Bean
    public MessageConverter messageConverter(){
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}