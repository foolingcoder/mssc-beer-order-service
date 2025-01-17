package guru.sfg.beer.order.service.sm;

import java.util.EnumSet;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.sm.action.AllocateOrderAction;
import guru.sfg.beer.order.service.sm.action.AllocationFailureAction;
import guru.sfg.beer.order.service.sm.action.DeallocateOrderAction;
import guru.sfg.beer.order.service.sm.action.ValidateOrderAction;
import guru.sfg.beer.order.service.sm.action.ValidationFailureAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@EnableStateMachineFactory
@Configuration
public class BeerOrderStateMachineConfig
		extends StateMachineConfigurerAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

	private final ValidateOrderAction validateOrderAction;

	private final AllocateOrderAction allocateOrderAction;

	private final AllocationFailureAction allocationFailureAction;

	private final ValidationFailureAction validationFailureAction;

	private final DeallocateOrderAction deallocateOrderAction;

	@Override
	public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> states)
			throws Exception {
		states.withStates().initial(BeerOrderStatusEnum.NEW).states(EnumSet.allOf(BeerOrderStatusEnum.class))
				.end(BeerOrderStatusEnum.VALIDATION_EXCEPTION).end(BeerOrderStatusEnum.ALLOCATION_EXCEPTION)
				.end(BeerOrderStatusEnum.PICKED_UP).end(BeerOrderStatusEnum.DELIVERED)
				.end(BeerOrderStatusEnum.DELIVERY_EXCEPTION).end(BeerOrderStatusEnum.CANCELLED);
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> transitions)
			throws Exception {
				transitions.withExternal().source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATION_PENDING)
				.event(BeerOrderEventEnum.VALIDATE_ORDER).action(validateOrderAction)

				.and().withExternal().source(BeerOrderStatusEnum.VALIDATION_PENDING)
				.target(BeerOrderStatusEnum.VALIDATED).event(BeerOrderEventEnum.VALIDATION_PASSED)

				.and().withExternal().source(BeerOrderStatusEnum.VALIDATION_PENDING)
				.target(BeerOrderStatusEnum.VALIDATION_EXCEPTION).event(BeerOrderEventEnum.VALIDATION_FAILED)
				.action(validationFailureAction)

				.and().withExternal().source(BeerOrderStatusEnum.VALIDATION_PENDING)
				.target(BeerOrderStatusEnum.CANCELLED).event(BeerOrderEventEnum.CANCEL_ORDER)

				.and().withExternal().source(BeerOrderStatusEnum.VALIDATED)
				.target(BeerOrderStatusEnum.ALLOCATION_PENDING).event(BeerOrderEventEnum.ALLOCATE_ORDER)
				.action(allocateOrderAction)

				.and().withExternal().source(BeerOrderStatusEnum.ALLOCATION_PENDING)
				.target(BeerOrderStatusEnum.ALLOCATED).event(BeerOrderEventEnum.ALLOCATION_SUCCESS)

				.and().withExternal().source(BeerOrderStatusEnum.ALLOCATION_PENDING)
				.target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION).event(BeerOrderEventEnum.ALLOCATION_FAILED)
				.action(allocationFailureAction)

				.and().withExternal().source(BeerOrderStatusEnum.ALLOCATION_PENDING)
				.target(BeerOrderStatusEnum.CANCELLED).event(BeerOrderEventEnum.CANCEL_ORDER)

				.and().withExternal().source(BeerOrderStatusEnum.ALLOCATION_PENDING)
				.target(BeerOrderStatusEnum.PENDING_INVENTORY).event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)

				.and().withExternal().source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.PICKED_UP)
				.event(BeerOrderEventEnum.BEERORDER_PICKED_UP)

				.and().withExternal().source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.CANCELLED)
				.event(BeerOrderEventEnum.CANCEL_ORDER).action(deallocateOrderAction);

	}

}
