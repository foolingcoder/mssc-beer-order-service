package guru.sfg.beer.order.service.sm.action;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.model.events.AllocationFailureRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum>{

	private final JmsTemplate jmsTemplate;
	private final BeerOrderRepository beerOrderRepository;
	private final BeerOrderMapper beerOrderMapper;


	@Override
	public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
		String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);

		Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));

		beerOrderOptional.ifPresentOrElse(beerOrder -> {
			jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_FAILURE_QUEUE,
					AllocationFailureRequest.builder().beerOrder(beerOrderMapper.beerOrderToDto(beerOrder)).build());
		}, () -> log.error("Order Not Found. Id: " + beerOrderId));

		log.debug("Sent to allocation failure request for order id " + beerOrderId);
		
	}
	


}
