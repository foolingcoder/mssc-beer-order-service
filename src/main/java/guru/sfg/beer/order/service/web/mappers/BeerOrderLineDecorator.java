package guru.sfg.beer.order.service.web.mappers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.beer.BeerService;
import guru.sfg.beer.order.service.web.model.BeerDto;
import guru.sfg.beer.order.service.web.model.BeerOrderLineDto;

public class BeerOrderLineDecorator implements BeerOrderLineMapper {
	
	private BeerService beerService;
	private BeerOrderLineMapper beerOrderLineMapper;

	@Autowired
	public void setBeerService(BeerService beerService) {
		this.beerService = beerService;
	}
	
	@Qualifier("delegate")
	@Autowired
	public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
		this.beerOrderLineMapper = beerOrderLineMapper;
	}
	
	@Override
	public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
		BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
		Optional<BeerDto> beerDtoOptional = beerService.getBeerByUpc(line.getUpc());		
		
		beerDtoOptional.ifPresent(beerDto -> {
									beerOrderLineDto.setBeerName(beerDto.getBeerName());
									beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
									beerOrderLineDto.setPrice(beerDto.getPrice());
									beerOrderLineDto.setBeerId(beerDto.getId());
									});
		return beerOrderLineDto;
	}

	@Override
	public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
		return beerOrderLineMapper.dtoToBeerOrderLine(dto);
	}

}
