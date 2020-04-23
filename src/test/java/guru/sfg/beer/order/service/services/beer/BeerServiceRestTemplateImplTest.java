package guru.sfg.beer.order.service.services.beer;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import guru.sfg.brewery.model.BeerDto;

@Disabled
@SpringBootTest
class BeerServiceRestTemplateImplTest {

	@Autowired
	private BeerService beerService;

	@Test
	public void testGetBeerById() {
		Optional<BeerDto> beerDtoOptional = beerService
				.getBeerById(UUID.fromString("0a818933-087d-47f2-ad83-2f986ed087eb"));

		beerDtoOptional.ifPresent(beerDto -> System.out.print(beerDto));
	}

	@Test
	public void testGetBeerByUPC() {
		Optional<BeerDto> beerDtoOptional = beerService.getBeerByUpc("0631234200036");

		beerDtoOptional.ifPresent(beerDto -> System.out.print(beerDto));
	}

}
