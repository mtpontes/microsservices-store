package br.com.ecommerce.products.api.controller.product;

import java.net.URI;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.ecommerce.products.api.dto.product.CreateProductDTO;
import br.com.ecommerce.products.api.dto.product.DataProductDTO;
import br.com.ecommerce.products.api.dto.product.DataProductStockDTO;
import br.com.ecommerce.products.api.dto.product.DataStockDTO;
import br.com.ecommerce.products.api.dto.product.EndOfPromotionDTO;
import br.com.ecommerce.products.api.dto.product.SchedulePromotionDTO;
import br.com.ecommerce.products.api.dto.product.SchedulePromotionResponseDTO;
import br.com.ecommerce.products.api.dto.product.UpdatePriceDTO;
import br.com.ecommerce.products.api.dto.product.UpdateProductDTO;
import br.com.ecommerce.products.api.dto.product.UpdateProductImagesResponseDTO;
import br.com.ecommerce.products.api.dto.product.UpdateProductPriceResponseDTO;
import br.com.ecommerce.products.api.dto.product.UpdateProductResponseDTO;
import br.com.ecommerce.products.api.dto.product.UpdatePromotionalPriceDTO;
import br.com.ecommerce.products.api.openapi.IAdminProductController;
import br.com.ecommerce.products.business.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/admin/products")
public class AdminProductController implements IAdminProductController {

	private final ProductService service;


	@PostMapping
	public ResponseEntity<DataProductDTO> createProduct(
		@RequestBody @Valid CreateProductDTO dto, 
		UriComponentsBuilder uriBuilder
	) {
		DataProductDTO responseBody = service.createProduct(dto);
		URI uri = uriBuilder
			.path("/products/{productId}")
			.buildAndExpand(responseBody.getId())
			.toUri();
		return ResponseEntity.created(uri).body(responseBody);
	}

	@PutMapping("/{productId}")
	public ResponseEntity<UpdateProductResponseDTO> updateProduct(
		@PathVariable Long productId, 
		@RequestBody UpdateProductDTO dto
	) {
		return ResponseEntity.ok(service.updateProductData(productId, dto));
	}

	@PutMapping("/{productId}/stocks")
	public ResponseEntity<DataProductStockDTO> updateStock(
		@PathVariable Long productId, 
		@RequestBody @Valid DataStockDTO dto
	) {
		return ResponseEntity.ok(service.updateStockByProductId(productId, dto));
	}

	@PutMapping("/{productId}/prices")
	public ResponseEntity<UpdateProductPriceResponseDTO> updatePrice(
		@PathVariable Long productId, 
		@RequestBody @Valid UpdatePriceDTO dto
	) {
		return ResponseEntity.ok(service.updateProductPrice(productId, dto));
	}

	@PutMapping("/{productId}/prices/promotion")
	public ResponseEntity<UpdateProductPriceResponseDTO> updatePromotionalPrice(
		@PathVariable Long productId, 
		@RequestBody @Valid UpdatePromotionalPriceDTO dto
	) {
		return ResponseEntity.ok(service.updateProductPricePromotional(productId, dto));
	}

	@PutMapping("/{productId}/prices/promotion/start")
	public ResponseEntity<UpdateProductPriceResponseDTO> iniciatePromotion(
		@PathVariable Long productId,
		@RequestBody @Valid EndOfPromotionDTO requestBody
	) {
		return ResponseEntity.ok(service.startPromotionImediatly(productId, requestBody.getEndPromotion()));
	}

	@PutMapping("/{productId}/prices/promotion/schedule")
	public ResponseEntity<SchedulePromotionResponseDTO> schedulePromotion(
		@PathVariable Long productId,
		@RequestBody @Valid SchedulePromotionDTO requestBody
	) {
		return ResponseEntity.ok(service.schedulePromotion(productId, requestBody));
	}

	@PutMapping("/{productId}/prices/promotion/end")
	public ResponseEntity<UpdateProductPriceResponseDTO> finalizePromotion(
		@PathVariable Long productId
	) {
		return ResponseEntity.ok(service.closePromotion(productId));
	}

	@PatchMapping("/{productId}/images")
	public ResponseEntity<UpdateProductImagesResponseDTO> addMainImage(
		@PathVariable Long productId, @RequestParam String imageLink
	) {
		return ResponseEntity.ok(service.addMainImage(productId, imageLink));
	}

	@PutMapping("/{productId}/images")
	public ResponseEntity<UpdateProductImagesResponseDTO> addImages(
		@PathVariable Long productId, @RequestBody Set<String> newImages
	) {
		return ResponseEntity.ok(service.addImages(productId, newImages));
	}

	@DeleteMapping("/{productId}/images")
	public ResponseEntity<UpdateProductImagesResponseDTO> removeImages(
		@PathVariable Long productId, @RequestBody Set<String> imagesToRemove
	) {
		return ResponseEntity.ok(service.removeImages(productId, imagesToRemove));
	}
}