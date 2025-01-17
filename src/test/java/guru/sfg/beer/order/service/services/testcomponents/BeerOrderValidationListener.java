package guru.sfg.beer.order.service.services.testcomponents;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

	private final JmsTemplate jmsTemplate;

	@JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
	public void list(Message<ValidateOrderRequest> msg) {

		boolean isValid = true;
		boolean sendResponse = true;

		ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

		// condition to fail validation
		if (request.getBeerOrder().getCustomerRef() != null) {
			if (request.getBeerOrder().getCustomerRef().equals("fail-validation")) {
				isValid = false;
			} else if (request.getBeerOrder().getCustomerRef().equals("dont-validate")) {
				sendResponse = false;
			}
		}

		if (sendResponse) {
			jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
					ValidateOrderResult.builder().isValid(isValid).orderId(request.getBeerOrder().getId()).build());
		}
	}

}
