package guru.sfg.beer.order.service.sm.action;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;

@Component
public class AllocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum>{

	@Override
	public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
		// TODO Auto-generated method stub
		
	}
	


}
