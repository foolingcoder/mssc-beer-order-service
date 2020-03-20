package guru.sfg.beer.order.service.services.beer;

import java.util.Optional;
import java.util.UUID;

import guru.sfg.brewery.model.BeerDto;

public interface BeerService {
	
	public abstract Optional<BeerDto> getBeerById(UUID beerId);
	
	public abstract Optional<BeerDto> getBeerByUpc(String upc);

}
