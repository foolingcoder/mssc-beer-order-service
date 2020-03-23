package guru.sfg.beer.order.service.services.listeners;

import java.util.UUID;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateOrderResponseListener {

	private final BeerOrderManager beerOrderManager;

	@JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
	public void listen(AllocateOrderResult result) {

		final BeerOrderDto beerOrderDto = result.getBeerOrderDto();

		log.debug("Allocation Result for Order Id: " + beerOrderDto.getId());

		beerOrderManager.processAllocationResult(beerOrderDto.getId(),result.isAllocationError() ,result.isPendingInventory());

	}

}
