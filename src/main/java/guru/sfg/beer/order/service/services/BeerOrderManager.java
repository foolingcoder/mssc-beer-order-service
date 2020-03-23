package guru.sfg.beer.order.service.services;

import java.util.UUID;

import guru.sfg.beer.order.service.domain.BeerOrder;

public interface BeerOrderManager {
	
	BeerOrder newBeerOrder(BeerOrder beerOrder);

	void processValidationResult(UUID beerOrderId, boolean isValid);

	void processAllocationResult(UUID beerOrderId, boolean allocationError, boolean pendingInventory);

}
