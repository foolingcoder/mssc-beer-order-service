package guru.sfg.beer.order.service.services;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.jgroups.util.Util.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.beer.BeerServiceRestTemplateImpl;
import guru.sfg.brewery.model.BeerDto;

@SpringBootTest
public class BeerOrderManagerImplIT {

	@Autowired
	BeerOrderManager beerOrderManager;

	@Autowired
	BeerOrderRepository beerOrderRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	WireMockServer wireMockServer;

	@Autowired
	JmsTemplate jmsTemplate;

	Customer testCustomer;

	UUID beerId = UUID.randomUUID();

	@TestConfiguration
	static class RestTemplateBuilderProvider {
		@Bean(destroyMethod = "stop")
		public WireMockServer wireMockServer() {
			WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8084));
			wireMockServer.start();
			return wireMockServer;
		}
	}

	@BeforeEach
	void setUp() {

		// wireMockServer = new WireMockServer(wireMockConfig().port(8084));
		// wireMockServer.start();

		testCustomer = customerRepository.save(Customer.builder().customerName("Test Customer").build());
	}

	@Test
	void testNewToAllocated() throws JsonProcessingException, InterruptedException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();

		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

		assertNotNull(savedBeerOrder);
		assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder.getOrderStatus());

	}

	public BeerOrder createBeerOrder() {
		BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();

		Set<BeerOrderLine> lines = new HashSet<>();
		lines.add(BeerOrderLine.builder().beerId(beerId).upc("12345").orderQuantity(1).beerOrder(beerOrder).build());

		beerOrder.setBeerOrderLines(lines);

		return beerOrder;
	}
}
