package guru.sfg.beer.order.service.domain;

public enum BeerOrderEventEnum {
	VALIDATE_ORDER, VALIDATION_PASSED ,VALIDATION_FAILED,
	ALLOCATE_ORDER ,ALLOCATION_SUCCESS ,ALLOCATION_FAILED, ALLOCATION_NO_INVENTORY
	,BEERORDER_PICKED_UP, CANCEL_ORDER
}
