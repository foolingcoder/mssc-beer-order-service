package guru.sfg.beer.order.service.services.listeners;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Component
public class AllocateOrderResponseListener {

	private final BeerOrderManager beerOrderManager;

	@JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
	public void listen(AllocateOrderResult result) {

		if (!result.isAllocationError() && !result.isPendingInventory()) {
			// allocated normally
			beerOrderManager.beerOrderAllocationPassed(result.getBeerOrderDto());
		} else if (!result.isAllocationError() && result.isPendingInventory()) {
			// pending inventory
			beerOrderManager.beerOrderAllocationPendingInventory(result.getBeerOrderDto());
		} else if (result.isAllocationError()) {
			// allocation error
			beerOrderManager.beerOrderAllocationFailed(result.getBeerOrderDto());
		}
	}

}
