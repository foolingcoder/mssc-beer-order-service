package guru.sfg.beer.order.service.services.listeners;

import java.util.UUID;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateOrderResponseListener {

	private final BeerOrderManager beerOrderManager;

	@JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
	public void listen(ValidateOrderResult result) {

		final UUID beerOrderId = result.getOrderId();

		log.debug("Validation Result for Order Id: " + beerOrderId);

		beerOrderManager.processValidationResult(beerOrderId, result.getIsValid());

	}

}
