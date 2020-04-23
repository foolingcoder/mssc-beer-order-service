package guru.sfg.beer.order.service.services;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
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
public class BeerOrderManagerImplIntegrationTests {

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
			WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8083));
			wireMockServer.start();
			return wireMockServer;
		}
	}

	@BeforeEach
	void setUp() {
		testCustomer = customerRepository.save(Customer.builder().customerName("Test Customer").build());
	}

	@Test
	void testNewToValidationFailed() throws JsonProcessingException, InterruptedException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		beerOrder.setCustomerRef("fail-validation");

		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

		await().untilAsserted(() -> {
			BeerOrder foundBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundBeerOrder.getOrderStatus());
		});

	}
	
	@Test
	void testValidationPendingToCancelled() throws JsonProcessingException, InterruptedException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		beerOrder.setCustomerRef("dont-validate");

		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

		await().untilAsserted(() -> {
			BeerOrder foundBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.VALIDATION_PENDING, foundBeerOrder.getOrderStatus());
		});
		
		beerOrderManager.beerOrderCancelled(savedBeerOrder.getId());
		

		await().untilAsserted(() -> {
			BeerOrder foundBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.CANCELLED, foundBeerOrder.getOrderStatus());
		});
	}




	@Test
	void testNewToAllocated() throws JsonProcessingException, InterruptedException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();

		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

		await().untilAsserted(() -> {
			BeerOrder foundBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.ALLOCATED, foundBeerOrder.getOrderStatus());
		});

		BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

		assertNotNull(savedBeerOrder2);
		assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder2.getOrderStatus());

	}
	
	@Test
	void testNewToAllocationFailed() throws JsonProcessingException, InterruptedException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		beerOrder.setCustomerRef("fail-allocation");

		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

		await().untilAsserted(() -> {
			BeerOrder foundBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, foundBeerOrder.getOrderStatus());
		});

	}
	
	@Test
	void testNewToAllocationPartial() throws JsonProcessingException, InterruptedException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		beerOrder.setCustomerRef("partial-allocation");

		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

		await().untilAsserted(() -> {
			BeerOrder foundBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.PENDING_INVENTORY, foundBeerOrder.getOrderStatus());
		});

	}

	
	@Test
	void testAllocationPendingToCancelled() throws JsonProcessingException, InterruptedException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();
		beerOrder.setCustomerRef("dont-allocate");

		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

		await().untilAsserted(() -> {
			BeerOrder foundBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.ALLOCATION_PENDING, foundBeerOrder.getOrderStatus());
		});
		
		beerOrderManager.beerOrderCancelled(savedBeerOrder.getId());
		

		await().untilAsserted(() -> {
			BeerOrder foundBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.CANCELLED, foundBeerOrder.getOrderStatus());
		});
	}

	@Test
	void testNewToPickedUp() throws JsonProcessingException, InterruptedException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

		wireMockServer.stubFor(get(BeerServiceRestTemplateImpl.BEER_UPC_PATH + "12345")
				.willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

		BeerOrder beerOrder = createBeerOrder();

		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

		await().untilAsserted(() -> {
			BeerOrder allocatedOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.ALLOCATED, allocatedOrder.getOrderStatus());
		});

		beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());

		await().untilAsserted(() -> {
			BeerOrder allocatedOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
			assertEquals(BeerOrderStatusEnum.PICKED_UP, allocatedOrder.getOrderStatus());
		});

		BeerOrder finalBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
		assertNotNull(finalBeerOrder);
		assertEquals(BeerOrderStatusEnum.PICKED_UP, finalBeerOrder.getOrderStatus());

	}

	private BeerOrder createBeerOrder() {
		BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();

		Set<BeerOrderLine> lines = new HashSet<>();
		lines.add(BeerOrderLine.builder().beerId(beerId).upc("12345").orderQuantity(1).beerOrder(beerOrder).build());

		beerOrder.setBeerOrderLines(lines);

		return beerOrder;
	}
}
