package guru.sfg.beer.order.service.sm;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;

@SpringBootTest
class BeerOrderStateMachineConfigTest {

	private static final String ORDER_ID_HEADER = "beer_order_id";
	
	@Autowired
	private StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> factory;

	@Autowired
	private StateMachineInterceptor<BeerOrderStatusEnum, BeerOrderEventEnum> beerOrderStateChangeInterceptor;

	@Test
	void test() {
		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = factory.getStateMachine(UUID.randomUUID());
		sm.start();
		System.out.println(sm.getState().toString());
		
		
		sendBeerOrderEvent(BeerOrderStatusEnum.NEW , BeerOrderEventEnum.VALIDATE_ORDER, UUID.randomUUID());
		System.out.println(sm.getState().toString());
		
	}
	

	private void sendBeerOrderEvent(BeerOrderStatusEnum beerOrderStatusEnum, BeerOrderEventEnum beerOrderEventEnum, UUID uuid) {

		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrderStatusEnum,uuid);

		Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(beerOrderEventEnum)
				.setHeader(ORDER_ID_HEADER, uuid.toString()).build();

		sm.sendEvent(message);

	}

	private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrderStatusEnum beerOrderStatusEnum, UUID uuid) {

		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = factory
				.getStateMachine(uuid);
		sm.stop();

		sm.getStateMachineAccessor().doWithAllRegions(sma -> {
		    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
			sma.resetStateMachine(new DefaultStateMachineContext<BeerOrderStatusEnum, BeerOrderEventEnum>(
					beerOrderStatusEnum, null, null, null));
		});

		sm.start();

		return sm;
	}


}
