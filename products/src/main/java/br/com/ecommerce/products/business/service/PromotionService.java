package br.com.ecommerce.products.business.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.com.ecommerce.products.api.dto.product.DataProductDTO;
import br.com.ecommerce.products.api.mapper.factory.ProductDTOFactory;
import br.com.ecommerce.products.infra.constants.CacheName;
import br.com.ecommerce.products.infra.entity.product.Product;
import br.com.ecommerce.products.infra.repository.ProductRepository;
import br.com.ecommerce.products.infra.scheduling.scheduler.PriceJobScheduler;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class PromotionService {

	private final ProductRepository productRepository;
	private final ProductDTOFactory productDtoFactory;
	private final PriceJobScheduler priceJobScheduler;
	private final CacheManager cacheManager;	


	@Transactional
	public void removeExpiredPromotions() {
		productRepository.findAllExpiredPromotions(LocalDateTime.now())
			.forEach(product -> product.endPromotion());
	}

	public void createScheduleForPromotionsThatWillStart() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourLater = now.plusHours(1);
		productRepository.findAllByPromotionStartingBetween(now, oneHourLater)
			.forEach(product -> priceJobScheduler.createSchedulerForPromotionStart(
				product.getId(),
				product.getPrice().getStartPromotion(),
				product.getPrice().getEndPromotion()));
	}

	public void createScheduleForPromotionsThatWillExpire() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourLate = now.plusHours(1);
		productRepository.findAllOnPromotionEndingBetween(now, oneHourLate)
			.forEach(product -> priceJobScheduler.createSchedulerForPromotionEnd(
				product.getId(), 
				product.getPrice().getEndPromotion()));
    }

	
	public void createCacheForProductOnPromotion(Product product) {
		Cache productCache = Optional.ofNullable(cacheManager.getCache(CacheName.PRODUCTS))
			.orElseThrow(() -> new RuntimeException("Could not create cache"));

		DataProductDTO productData = productDtoFactory.createDataProductDTO(product);
		productCache.put(product.getId(), productData);
	}

	@Async
	@Transactional
	public void createCacheForProductsOnPromotion() {
		productRepository.findAllByEndOfPromotionIsAfterOf(LocalDateTime.now())
			.forEach(this::createCacheForProductOnPromotion);
	}
}