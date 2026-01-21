package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.Item.dto.request.*;
import com.abed.perfumeshop.Item.dto.response.AdminPerfumeCardDTO;
import com.abed.perfumeshop.Item.entity.*;
import com.abed.perfumeshop.Item.repo.*;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.admin.service.AdminPerfumeService;
import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeSize;
import com.abed.perfumeshop.common.enums.PerfumeType;
import com.abed.perfumeshop.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminPerfumeServiceImpl implements AdminPerfumeService {

    private static final String BASE_IMAGE_URL = "/api/public/perfumes";

    private final ItemRepo itemRepo;
    private final PerfumeRepo perfumeRepo;
    private final ItemPriceRepo itemPriceRepo;
    private final ItemTranslationRepo itemTranslationRepo;
    private final PerfumeImageRepo perfumeImageRepo;
    private final AdminHelper adminHelper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminPerfumeCardDTO> getAllPerfumes(
            int page,
            int size,
            PerfumeType perfumeType,
            PerfumeSeason perfumeSeason
    ) {
        // Convert PerfumeSeason to String (or null)
        String perfumeSeasonString = perfumeSeason != null ? perfumeSeason.name() : null;
        Page<Perfume> perfumesPage = perfumeRepo.findAllWithFilters(perfumeType, perfumeSeasonString, PageRequest.of(page, size));

        return buildAdminPerfumeCardResponse(perfumesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminPerfumeCardDTO> searchPerfumes(int page, int size, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("perfume.search.keyword.required");
        }

        if (keyword.trim().length() < 2) {
            throw new ValidationException("perfume.search.keyword.too.short");
        }

        Page<Perfume> perfumesPage = perfumeRepo.searchAllPerfumes(keyword, PageRequest.of(page, size));

        return buildAdminPerfumeCardResponse(perfumesPage);
    }

    @Override
    @Transactional
    public void createPerfume(
            CreatePerfumeRequest createPerfumeRequest,
            List<MultipartFile> images,
            Integer primaryImageIndex,
            List<Integer> imageOrder
    ) {
        adminHelper.getCurrentLoggedInUser();

        validateCreatePerfumeRequest(createPerfumeRequest, images, primaryImageIndex, imageOrder);

        // Create and save base item
        Item item = Item.builder()
                .name(createPerfumeRequest.getName())
                .brand(createPerfumeRequest.getBrand())
                .build();
        itemRepo.save(item);

        // Create and save item prices for all sizes
        List<ItemPrice> itemPrices = createPerfumeRequest.getPrices().stream()
                .map(priceRequest -> ItemPrice.builder()
                        .price(priceRequest.getPrice())
                        .quantity(priceRequest.getQuantity())
                        .perfumeSize(priceRequest.getPerfumeSize())
                        .item(item)
                        .build())
                .toList();
        itemPriceRepo.saveAll(itemPrices);

        // Create and save translations for different locales
        List<ItemTranslation> translations = createPerfumeRequest.getTranslations().stream()
                .map(translationRequest ->  ItemTranslation.builder()
                        .locale(translationRequest.getLocale())
                        .name(translationRequest.getName())
                        .description(translationRequest.getDescription())
                        .item(item)
                        .build())
                .toList();
        itemTranslationRepo.saveAll(translations);

        // Convert perfume seasons Set to comma-separated String
        String perfumeSeasons = createPerfumeRequest.getPerfumeSeasons().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(","));

        // Create and save perfume-specific details
        Perfume perfume = Perfume.builder()
                .perfumeType(createPerfumeRequest.getPerfumeType())
                .perfumeSeasons(perfumeSeasons)
                .item(item)
                .build();
        perfumeRepo.save(perfume);

        // Create and save Perfume Images
        List<PerfumeImage> perfumeImages = new ArrayList<>();

        for (int i = 0; i < images.size(); i++){
            MultipartFile image = images.get(i);
            int displayOrder = imageOrder.get(i);

            PerfumeImage perfumeImage = createPerfumeImageFromFile(
                    image,
                    perfume,
                    i == primaryImageIndex,
                    displayOrder
            );
            perfumeImages.add(perfumeImage);
        }

        perfumeImageRepo.saveAll(perfumeImages);
    }

    @Override
    @Transactional
    public void updatePerfume(Long id, UpdatePerfumeRequest updatePerfumeRequest) {
        adminHelper.getCurrentLoggedInUser();

        Perfume perfume = perfumeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("perfume.not.found"));

        Item item = perfume.getItem();

        // Check for duplicate name/brand
        if (itemRepo.existsByNameAndBrandAndIdNot(
                updatePerfumeRequest.getName(),
                updatePerfumeRequest.getBrand(),
                item.getId())) {
            throw new AlreadyExistsException("item.already.exists");
        }

        // Validate active status consistency with prices
        validateActiveStatusConsistency(updatePerfumeRequest);

        // Update item
        item.setName(updatePerfumeRequest.getName());
        item.setBrand(updatePerfumeRequest.getBrand());
        item.setActive(updatePerfumeRequest.getActive());

        // Convert perfume seasons Set to comma-separated String
        String perfumeSeasons = updatePerfumeRequest.getPerfumeSeasons().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(","));

        // Update perfume
        perfume.setPerfumeType(updatePerfumeRequest.getPerfumeType());
        perfume.setPerfumeSeasons(perfumeSeasons);

        // Track processed sizes to identify removed ones later
        Set<PerfumeSize> processedSizes = new HashSet<>();

        // Process each price in the request
        for (UpdatePerfumePriceRequest priceRequest : updatePerfumeRequest.getPrices()) {
            // Check if active price exists for this size
            Optional<ItemPrice> existingActivePrice = itemPriceRepo.findByItemIdAndPerfumeSizeAndIsActiveTrue(
                    item.getId(),
                    priceRequest.getPerfumeSize()
            );

            if (existingActivePrice.isPresent()) {
                ItemPrice currentPrice = existingActivePrice.get();
                boolean priceChanged = currentPrice.getPrice().compareTo(priceRequest.getPrice()) != 0;

                if (priceChanged) {
                    // Price changed: require note, deactivate old, create new
                    if (priceRequest.getNote() == null || priceRequest.getNote().trim().isBlank()) {
                        throw new ValidationException("item.note.required.when.price.changed");
                    }

                    // Deactivate old price
                    currentPrice.setEffectiveTo(LocalDateTime.now());
                    currentPrice.setNotes(priceRequest.getNote());
                    currentPrice.setIsActive(false);
                    itemPriceRepo.saveAndFlush(currentPrice);

                    // Create new active price
                    ItemPrice itemPrice = ItemPrice.builder()
                            .price(priceRequest.getPrice())
                            .quantity(priceRequest.getQuantity())
                            .perfumeSize(priceRequest.getPerfumeSize())
                            .isActive(priceRequest.getIsActive())
                            .item(item)
                            .build();
                    itemPriceRepo.save(itemPrice);
                } else {
                    // Price unchanged: update quantity and status only
                    currentPrice.setQuantity(priceRequest.getQuantity());
                    currentPrice.setIsActive(priceRequest.getIsActive());
                    itemPriceRepo.save(currentPrice);
                }
            } else {
                // No active price exists: create new one
                ItemPrice newPrice = ItemPrice.builder()
                        .price(priceRequest.getPrice())
                        .quantity(priceRequest.getQuantity())
                        .perfumeSize(priceRequest.getPerfumeSize())
                        .isActive(priceRequest.getIsActive())
                        .item(item)
                        .build();
                itemPriceRepo.save(newPrice);
            }

            processedSizes.add(priceRequest.getPerfumeSize());
        }

        // Deactivate sizes that are no longer in the request
        List<ItemPrice> currentActivePrices = itemPriceRepo.findByItemIdAndIsActiveTrue(item.getId());
        for (ItemPrice currentPrice : currentActivePrices) {
            if (!processedSizes.contains(currentPrice.getPerfumeSize())) {
                currentPrice.setEffectiveTo(LocalDateTime.now());
                currentPrice.setIsActive(false);
                itemPriceRepo.save(currentPrice);
            }
        }

        // Update translations
        List<ItemTranslation> toSave = new ArrayList<>();

        for (ItemTranslationRequest translation : updatePerfumeRequest.getTranslations()) {
            ItemTranslation existing = itemTranslationRepo.findByItemIdAndLocale(item.getId(), translation.getLocale())
                    .orElse(null);

            if (existing != null) {
                existing.setName(translation.getName());
                existing.setDescription(translation.getDescription());
                toSave.add(existing);
            } else {
                ItemTranslation newTranslation = ItemTranslation.builder()
                        .locale(translation.getLocale())
                        .name(translation.getName())
                        .description(translation.getDescription())
                        .item(item)
                        .build();
                toSave.add(newTranslation);
            }
        }
        itemTranslationRepo.saveAll(toSave);

        itemRepo.save(item);
        perfumeRepo.save(perfume);
    }

    @Override
    @Transactional
    public void addPerfumeImage(
            Long perfumeId,
            MultipartFile image,
            Boolean isPrimary
    ) {
        adminHelper.getCurrentLoggedInUser();

        if (image == null || image.isEmpty()) {
            throw new ValidationException("image.required");
        }

        Perfume perfume = perfumeRepo.findById(perfumeId)
                .orElseThrow(() -> new NotFoundException("perfume.not.found"));

        int currentImageCount = perfumeImageRepo.countByPerfumeId(perfumeId);
        if (currentImageCount >= 5) {
            throw new MaxImagesExceededException("image.perfume.limit.exceeded");
        }

        // Handle primary logic
        if (isPrimary) {
            perfumeImageRepo.findByPerfumeIdAndIsPrimaryTrue(perfumeId)
                    .ifPresent(oldPerfumeImage -> {
                        oldPerfumeImage.setIsPrimary(false);
                        perfumeImageRepo.save(oldPerfumeImage);
                    });
        }

        Integer maxDisplayOrder = perfumeImageRepo.findMaxDisplayOrderByPerfumeId(perfumeId);
        int newDisplayOrder = maxDisplayOrder + 1;

        PerfumeImage perfumeImage = createPerfumeImageFromFile(
                image,
                perfume,
                isPrimary,
                newDisplayOrder
        );

        perfumeImageRepo.save(perfumeImage);
    }

    @Override
    @Transactional
    public void updatePerfumeImage(
            Long perfumeId,
            Long imageId,
            MultipartFile image
    ) {
        adminHelper.getCurrentLoggedInUser();

        if (image == null || image.isEmpty()) {
            throw new ValidationException("image.required");
        }

        PerfumeImage perfumeImage = perfumeImageRepo.findByIdAndPerfumeId(imageId, perfumeId)
                .orElseThrow(() -> new NotFoundException("image.not.found"));

        try {
            validateImage(image);

            perfumeImage.setImageData(image.getBytes());
            perfumeImage.setMimeType(image.getContentType());
            perfumeImage.setFileSize(image.getSize());

            perfumeImageRepo.save(perfumeImage);
        } catch (IOException e){
            throw new ImageProcessingException(
                    "image.processing.failed",
                    new Object[]{image.getOriginalFilename()}
            );
        }
    }

    @Override
    @Transactional
    public void deletePerfumeImage(Long perfumeId, Long imageId) {
        adminHelper.getCurrentLoggedInUser();

        PerfumeImage perfumeImage = perfumeImageRepo.findByIdAndPerfumeId(imageId, perfumeId)
                .orElseThrow(() -> new NotFoundException("image.not.found"));

        if (perfumeImage.getIsPrimary()) {
            throw new InvalidOperationException("image.primary.cannot.delete");
        }

        perfumeImageRepo.delete(perfumeImage);
    }

    // ========== Private Helper Methods ==========
    private PageResponse<AdminPerfumeCardDTO> buildAdminPerfumeCardResponse(Page<Perfume> perfumesPage) {
        List<Perfume> perfumes = perfumesPage.getContent();

        List<Long> perfumeIds = perfumes.stream()
                .map(Perfume::getId)
                .toList();

        List<Long> itemIds = perfumes.stream()
                .map(p -> p.getItem().getId())
                .toList();

        List<PerfumeImage> primaryImages = perfumeImageRepo.findPrimaryImagesByPerfumeIds(perfumeIds);
        List<ItemPrice> allPrices = itemPriceRepo.findCurrentActivePricesByItemIds(itemIds);
        List<ItemTranslation> allTranslations = itemTranslationRepo.findByItemIdsAndLocale(
                itemIds,
                LocaleContextHolder.getLocale().getLanguage()
        );

        Map<Long, PerfumeImage> primaryImageByPerfume = primaryImages.stream()
                .collect(Collectors.toMap(
                        img -> img.getPerfume().getId(),
                        img -> img
                ));

        Map<Long, ItemPrice> pricesByItem = allPrices.stream()
                .collect(Collectors.toMap(
                        itemPrice -> itemPrice.getItem().getId(),
                        p -> p
                ));

        Map<Long, ItemTranslation> translationsByItem = allTranslations.stream()
                .collect(Collectors.toMap(
                        itemTranslation -> itemTranslation.getItem().getId(),
                        t -> t
                ));

        List<AdminPerfumeCardDTO> perfumeCards = perfumes.stream()
                .map(perfume -> mapToAdminPerfumeCard(
                        perfume,
                        primaryImageByPerfume.get(perfume.getId()),
                        pricesByItem.get(perfume.getItem().getId()),
                        translationsByItem.get(perfume.getItem().getId())
                ))
                .toList();

        return PageResponse.<AdminPerfumeCardDTO>builder()
                .content(perfumeCards)
                .page(PageResponse.PageInfo.builder()
                        .size(perfumesPage.getSize())
                        .number(perfumesPage.getNumber())
                        .totalElements(perfumesPage.getTotalElements())
                        .totalPages(perfumesPage.getTotalPages())
                        .build())
                .build();
    }

    private AdminPerfumeCardDTO mapToAdminPerfumeCard(
            Perfume perfume,
            PerfumeImage primaryImage,
            ItemPrice lowestPrice,
            ItemTranslation translation
    ) {
        Item item = perfume.getItem();

        if (primaryImage == null) {
            throw new NotFoundException("image.not.found");
        }

        return AdminPerfumeCardDTO.builder()
                .id(perfume.getId())
                .name(item.getName())
                .brand(item.getBrand())
                .active(item.getActive())
                .lowestPrice(lowestPrice.getPrice())
                .quantity(lowestPrice.getQuantity())
                .translatedName(translation != null ? translation.getName() : item.getName())
                .primaryImageUrl(BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + primaryImage.getId())
                .build();
    }

    private void validateCreatePerfumeRequest(
            CreatePerfumeRequest createPerfumeRequest,
            List<MultipartFile> images,
            Integer primaryImageIndex,
            List<Integer> imageOrder
    ){
        // Validate images presence and count
        if (images == null || images.isEmpty()){
            throw new ValidationException("images.perfume.required");
        }

        if (images.size() > 5){
            throw new MaxImagesExceededException("image.perfume.limit.exceeded");
        }

        // Validate image order
        if (imageOrder == null || imageOrder.size() != images.size()) {
            throw new ValidationException("image.order.size.mismatch");
        }

        // Validate primary image index
        if (primaryImageIndex == null) {
            throw new ValidationException("image.primary.required");
        }

        if (primaryImageIndex < 0 || primaryImageIndex >= images.size()) {
            throw new ValidationException("image.primary.index.invalid");
        }

        // Validate image order values are unique and sequential
        Set<Integer> orderSet = new HashSet<>(imageOrder);
        if (orderSet.size() != images.size() ||
                !orderSet.containsAll(IntStream.range(0, images.size()).boxed().toList())) {
            throw new ValidationException("image.order.invalid");
        }

        // Check for duplicate perfume (name + brand)
        if (itemRepo.existsByNameAndBrand(createPerfumeRequest.getName(), createPerfumeRequest.getBrand())) {
            throw new AlreadyExistsException("item.already.exists");
        }

        // Validate no duplicate perfume sizes
        Set<PerfumeSize> uniqueSizes = new HashSet<>();
        for (CreatePerfumePriceRequest price : createPerfumeRequest.getPrices()) {
            if (!uniqueSizes.add(price.getPerfumeSize())) {
                throw new ValidationException(
                        "perfume.prices.duplicate.size",
                        new Object[]{price.getPerfumeSize().name()}
                );
            }
        }
    }

    private void validateActiveStatusConsistency(UpdatePerfumeRequest request) {
        // Active perfume must have at least one available price
        if (Boolean.TRUE.equals(request.getActive())) {
            boolean hasAvailablePrice = request.getPrices().stream()
                    .anyMatch(p -> Boolean.TRUE.equals(p.getIsActive()) && p.getQuantity() > 0);

            if (!hasAvailablePrice) {
                throw new ValidationException("perfume.active.must.have.available.price");
            }
        }

        // Inactive perfume cannot have active prices
        if (Boolean.FALSE.equals(request.getActive())) {
            boolean hasActivePrice = request.getPrices().stream()
                    .anyMatch(p -> Boolean.TRUE.equals(p.getIsActive()));

            if (hasActivePrice) {
                throw new ValidationException("perfume.inactive.cannot.have.active.prices");
            }
        }

        // Active price cannot have zero quantity
        for (UpdatePerfumePriceRequest price : request.getPrices()) {
            if (Boolean.TRUE.equals(price.getIsActive()) && price.getQuantity() == 0) {
                throw new ValidationException(
                        "perfume.price.active.requires.quantity",
                        new Object[]{price.getPerfumeSize().name()}
                );
            }
        }

        // Validate no duplicate perfume sizes
        Set<PerfumeSize> uniqueSizes = new HashSet<>();
        for (UpdatePerfumePriceRequest price : request.getPrices()) {
            if (!uniqueSizes.add(price.getPerfumeSize())) {
                throw new ValidationException(
                        "perfume.prices.duplicate.size",
                        new Object[]{price.getPerfumeSize().name()}
                );
            }
        }
    }

    private PerfumeImage createPerfumeImageFromFile(
            MultipartFile image,
            Perfume perfume,
            boolean isPrimary,
            int displayOrder
    ) {
        try {
            validateImage(image);

            return PerfumeImage.builder()
                    .imageData(image.getBytes())
                    .mimeType(image.getContentType())
                    .fileSize(image.getSize())
                    .isPrimary(isPrimary)
                    .displayOrder(displayOrder)
                    .perfume(perfume)
                    .build();
        } catch (IOException e) {
            throw new ImageProcessingException(
                    "image.processing.failed",
                    new Object[]{image.getOriginalFilename()}
            );
        }
    }

    private void validateImage(MultipartFile image) {
        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new ImageProcessingException(
                    "image.invalid.format",
                    new Object[]{image.getOriginalFilename()}
            );
        }

        if (image.getSize() > 5 * 1024 * 1024) {
            throw new ImageProcessingException(
                    "image.size.exceeded",
                    new Object[]{image.getOriginalFilename()}
            );
        }
    }

}
