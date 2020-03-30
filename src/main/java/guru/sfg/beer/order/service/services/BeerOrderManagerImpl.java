package guru.sfg.beer.order.service.services;

import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.stereotype.Service;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

	public static String ORDER_ID_HEADER = "beer_order_id";

	private final BeerOrderRepository beerRepository;

	private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;

	private final StateMachineInterceptor<BeerOrderStatusEnum, BeerOrderEventEnum> beerOrderStateChangeInterceptor;

	@Transactional
	@Override
	public BeerOrder newBeerOrder(BeerOrder beerOrder) {
		beerOrder.setId(null);
		beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

		BeerOrder savedBeerOrder = beerRepository.saveAndFlush(beerOrder);

		sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

		return savedBeerOrder;
	}

	@Override
	public void processValidationResult(UUID beerOrderId, boolean isValid) {
				
	    beerRepository.findById(beerOrderId).ifPresentOrElse((beerOrder ->{
	    	if (isValid) {
				sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);
			} else {
				sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
			}	
	        }),
	    	()->{ 
	    		log.debug("BeerOrderId is invalid: ",beerOrderId);
	    	});		
	}

	private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum beerOrderEventEnum) {

		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

		Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(beerOrderEventEnum)
				.setHeader(ORDER_ID_HEADER, beerOrder.getId().toString()).build();

		sm.sendEvent(message);

	}

	private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {

		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory
				.getStateMachine(beerOrder.getId());
		sm.stop();

		sm.getStateMachineAccessor().doWithAllRegions(sma -> {
			sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
			sma.resetStateMachine(new DefaultStateMachineContext<BeerOrderStatusEnum, BeerOrderEventEnum>(
					beerOrder.getOrderStatus(), null, null, null));
		});

		sm.start();

		return sm;
	}

	@Override
	public void processAllocationResult(UUID beerOrderId, boolean allocationError, boolean pendingInventory) {
	    beerRepository.findById(beerOrderId).ifPresentOrElse((beerOrder ->{
	    	if (!allocationError && !pendingInventory) {
				sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
			} else {
				sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
			}	
	        }),
	    	()->{ 
	    		log.debug("BeerOrderId is invalid: ",beerOrderId);
	    	});		
		
	}

}
