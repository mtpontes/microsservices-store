package br.com.ecommerce.products.business.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.ecommerce.products.api.dto.product.CompletePriceDataDTO;
import br.com.ecommerce.products.api.dto.product.CreateProductDTO;
import br.com.ecommerce.products.api.dto.product.DataProductDTO;
import br.com.ecommerce.products.api.dto.product.DataProductStockDTO;
import br.com.ecommerce.products.api.dto.product.DataStockDTO;
import br.com.ecommerce.products.api.dto.product.InternalProductDataDTO;
import br.com.ecommerce.products.api.dto.product.ProductUnitsRequestedDTO;
import br.com.ecommerce.products.api.dto.product.SchedulePromotionDTO;
import br.com.ecommerce.products.api.dto.product.SchedulePromotionResponseDTO;
import br.com.ecommerce.products.api.dto.product.StockWriteOffDTO;
import br.com.ecommerce.products.api.dto.product.UpdatePriceDTO;
import br.com.ecommerce.products.api.dto.product.UpdateProductDTO;
import br.com.ecommerce.products.api.dto.product.UpdateProductImagesResponseDTO;
import br.com.ecommerce.products.api.dto.product.UpdateProductPriceResponseDTO;
import br.com.ecommerce.products.api.dto.product.UpdateProductResponseDTO;
import br.com.ecommerce.products.api.dto.product.UpdatePromotionalPriceDTO;
import br.com.ecommerce.products.api.mapper.PriceMapper;
import br.com.ecommerce.products.api.mapper.ProductMapper;
import br.com.ecommerce.products.api.mapper.StockMapper;
import br.com.ecommerce.products.api.mapper.factory.ProductDTOFactory;
import br.com.ecommerce.products.business.validator.UniqueNameProductValidator;
import br.com.ecommerce.products.infra.constants.CacheName;
import br.com.ecommerce.products.infra.entity.category.Category;
import br.com.ecommerce.products.infra.entity.manufacturer.Manufacturer;
import br.com.ecommerce.products.infra.entity.product.Price;
import br.com.ecommerce.products.infra.entity.product.Product;
import br.com.ecommerce.products.infra.exception.exceptions.CategoryNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.ManufacturerNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.ProductNotFoundException;
import br.com.ecommerce.products.infra.repository.CategoryRepository;
import br.com.ecommerce.products.infra.repository.ManufacturerRepository;
import br.com.ecommerce.products.infra.repository.ProductRepository;
import br.com.ecommerce.products.infra.scheduling.scheduler.PriceJobScheduler;
import br.com.ecommerce.products.infra.scheduling.scheduler.PriceJobScheduler.PromotionOperation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ManufacturerRepository manufacturerRepository;

	private final ProductDTOFactory dtoFactory;

	private final PriceMapper priceMapper;
	private final StockMapper stockMapper;
	private final ProductMapper productMapper;

	private final UniqueNameProductValidator uniqueNameValidator;

	private final PriceJobScheduler priceScheduler;

	private final PromotionService promotionService;


	@Cacheable(cacheNames = CacheName.PRODUCTS, key = "#id")
	public DataProductDTO getProduct(Long id) {
		return productRepository.findById(id)
			.map(dtoFactory::createDataProductDTO)
			.orElseThrow(ProductNotFoundException::new);
	}

	@Cacheable(
		cacheNames = CacheName.PRODUCTS, 
		key = """
			#root.methodName + ':' +
			#name + ':' +
			#categoryName + ':' +
			#minPrice + ':' +
			#maxPrice + ':' +
			#manufacturer + ':' +
			#pageable.pageNumber + ':' +
			#pageable.pageSize + ':'
			""")
	public Page<DataProductDTO> getAllProductWithParams(
		String name, 
		String categoryName, 
		BigDecimal minPrice, 
		BigDecimal maxPrice,
		String manufacturer,
		Pageable pageable 
	) {
		return productRepository.findAllByParams(
			name, 
			categoryName, 
			minPrice, 
			maxPrice, 
			manufacturer,
			pageable 
		)
		.map(dtoFactory::createDataProductDTO);
	}

	public List<Product> checkWichProductsExceedsStock(List<ProductUnitsRequestedDTO> productsRequest) {
		Map<Long, Integer> unitiesRequested = productsRequest.stream()
			.collect(Collectors.toMap(i -> i.getId(), u -> u.getUnit()));

		List<Long> listOfProductIds = productsRequest.stream()
			.map(p -> p.getId())
			.toList();
		
		return productRepository.findAllById(listOfProductIds).stream()
			.filter(product -> product != null && product.getStock().getUnit() < unitiesRequested.get(product.getId())) 
			.collect(Collectors.toList());
	}

	@Transactional
	@CacheEvict(cacheNames = CacheName.PRODUCTS, key = "#id")
	public UpdateProductResponseDTO updateProductData(Long id, UpdateProductDTO dto) {
		uniqueNameValidator.validate(dto.getName());
		return productRepository.findById(id)
			.map(product -> {
				product.update(dto.getName(), dto.getDescription(), dto.getSpecs());
				return product;
			})
			.map(productMapper::toProductUpdateResponseDTO)
			.orElseThrow(ProductNotFoundException::new);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheName.PRODUCTS, key = "#id")
	public UpdateProductPriceResponseDTO updateProductPrice(Long id, UpdatePriceDTO dto) {
		return productRepository.findById(id)
			.map(product -> {
				Price newPrice = priceMapper.toPrice(dto);
				product.updatePrice(newPrice);
				return product;
			})
			.map(this::createUpdateProductPriceResponseDTO)
			.orElseThrow(ProductNotFoundException::new);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheName.PRODUCTS, key = "#id")
	public UpdateProductPriceResponseDTO updateProductPricePromotional(Long id, UpdatePromotionalPriceDTO dto) {
		return productRepository.findById(id)
			.map(product -> {
				product.getPrice().setPromotionalPrice(dto.getPrice());
				return product;
			})
			.map(this::createUpdateProductPriceResponseDTO)
			.orElseThrow(ProductNotFoundException::new);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheName.PRODUCTS, key = "#id")
	public UpdateProductPriceResponseDTO startPromotionImediatly(Long id, LocalDateTime endPromotion) {
		return productRepository.findById(id)
			.map(product -> {
				product.getPrice().setEndPromotion(endPromotion);
				product.startPromotion();

				priceScheduler.createSchedulerForPromotionEnd(product.getId(), endPromotion);
				promotionService.createCacheForProductOnPromotion(product);
				return product;
			})
			.map(this::createUpdateProductPriceResponseDTO)
			.orElseThrow(ProductNotFoundException::new);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheName.PRODUCTS, key = "#id")
	public SchedulePromotionResponseDTO schedulePromotion(Long id, SchedulePromotionDTO data) {
		return productRepository.findById(id)
			.map(product -> {
				product.getPrice().setStartPromotion(data.getStart());
				product.getPrice().setEndPromotion(data.getEnd());

				priceScheduler.createSchedulerForPromotionStart(product.getId(), data.getStart(), data.getEnd());
				return product;
			})
			.map(productMapper::toSchedulePromotionResponseDTO)
			.orElseThrow(ProductNotFoundException::new);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheName.PRODUCTS, key = "#id")
	public UpdateProductPriceResponseDTO closePromotion(Long id) {
		return productRepository.findById(id)
			.map(product -> {
				product.endPromotion();
				priceScheduler.removeRedundantSchedulePromotion(product.getId(), PromotionOperation.END_PROMOTION);
				return product;
			})
			.map(this::createUpdateProductPriceResponseDTO)
			.orElseThrow(ProductNotFoundException::new);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheName.PRODUCTS, key = "#id")
	public DataProductStockDTO updateStockByProductId(Long id, DataStockDTO dto) {
		try {
			Product target = productRepository.getReferenceById(id);
			target.updateStock(dto.getUnit());
			return stockMapper.toDataProductStock(target);

		} catch (EntityNotFoundException e) {
			throw new ProductNotFoundException(e);
		}
	}

	@Transactional
	@CacheEvict(cacheNames = CacheName.PRODUCTS, allEntries = true)
	public void updateStocks(List<StockWriteOffDTO> dto) {
		Map<Long, Integer> writeOffValueMap = dto.stream()
			.collect(Collectors.toMap(StockWriteOffDTO::getProductId, p -> (p.getUnit() < 0) ? p.getUnit() : Math.negateExact(p.getUnit())));
		
		Set<Long> ids = dto.stream().map(StockWriteOffDTO::getProductId).collect(Collectors.toSet());
		productRepository.findAllById(ids)
			.forEach(p -> p.updateStock(writeOffValueMap.get(p.getId())));
	}

	@Transactional
	public DataProductDTO createProduct(CreateProductDTO dto) {
		uniqueNameValidator.validate(dto.getName());

		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(CategoryNotFoundException::new);
		Manufacturer manufacturer = manufacturerRepository.findById(dto.getManufacturerId())
			.orElseThrow(ManufacturerNotFoundException::new);
		Product product = productMapper.toProduct(dto, category, manufacturer);

		category.addProduct(product);
		manufacturer.addProduct(product);

		productRepository.save(product);
		
		return dtoFactory.createDataProductDTO(product);
	}

	@Cacheable(value = CacheName.PRODUCTS)
	public Map<String, InternalProductDataDTO> getAllProductsByListOfIds(Set<Long> productsIds) {
		return productRepository.findAllById(productsIds).stream()
			.collect(Collectors.toMap(
				product -> String.valueOf(product.getId()), 
				productMapper::toInternalProductDataDTO));
	}

	@Transactional
	public UpdateProductImagesResponseDTO addMainImage(Long productId, String imageLink) {
		return productRepository.findById(productId)
			.map(product -> {
				product.getImages().setMainImage(imageLink);
				return productMapper.toUpdateProductImagesResponseDTO(product);
			})
			.orElseThrow(ProductNotFoundException::new);
    }

	@Transactional
	public UpdateProductImagesResponseDTO addImages(Long productId, Set<String> newImages) {
		return productRepository.findById(productId)
			.map(product -> {
				product.getImages().addAdditionalImages(newImages);
				return productMapper.toUpdateProductImagesResponseDTO(product);
			})
			.orElseThrow(ProductNotFoundException::new);
    }

	@Transactional
	public UpdateProductImagesResponseDTO removeImages(Long productId, Set<String> newImages) {
		return productRepository.findById(productId)
			.map(product -> {
				product.getImages().remove(newImages);
				return productMapper.toUpdateProductImagesResponseDTO(product);
			})
			.orElseThrow(ProductNotFoundException::new);
    }

	private UpdateProductPriceResponseDTO createUpdateProductPriceResponseDTO(Product product) {
		CompletePriceDataDTO priceData = priceMapper.toCompletePriceDataDTO(product.getPrice());
		return productMapper.toUpdateProductPriceResponseDTO(product, priceData);
	}

    public boolean existsProduct(Long productId) {
		return productRepository.existsById(productId);
    }
}