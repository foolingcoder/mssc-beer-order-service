package guru.sfg.beer.order.service.services.beer;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import guru.sfg.brewery.model.BeerDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Component
public class BeerServiceRestTemplateImpl implements BeerService {

	private final String BEER_ID_PATH_V1 = "/api/v1/beer/{beerId}";
	private final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/{upc}";
	
	private final RestTemplate restTemplate;		

	private String beerServiceHost;

	public BeerServiceRestTemplateImpl(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public void setBeerServiceHost(String beerServiceHost) {
		this.beerServiceHost = beerServiceHost;
	}

	@Override
	public Optional<BeerDto> getBeerById(UUID beerId) {

		ResponseEntity<Optional<BeerDto>> responseEntity = restTemplate.exchange(beerServiceHost + BEER_ID_PATH_V1,
				HttpMethod.GET, null, new ParameterizedTypeReference<Optional<BeerDto>>() {
				}, beerId);

		return responseEntity.getBody();
	}

	@Override
	public Optional<BeerDto> getBeerByUpc(String upc) {
		ResponseEntity<Optional<BeerDto>> responseEntity = restTemplate.exchange(beerServiceHost + BEER_UPC_PATH_V1,
				HttpMethod.GET, null, new ParameterizedTypeReference<Optional<BeerDto>>() {
				}, upc);

		return responseEntity.getBody();
	}

}
