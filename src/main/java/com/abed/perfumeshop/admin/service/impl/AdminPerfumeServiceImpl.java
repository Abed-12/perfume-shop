package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.Item.dto.*;
import com.abed.perfumeshop.Item.entity.*;
import com.abed.perfumeshop.Item.repo.*;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.admin.service.AdminPerfumeService;
import com.abed.perfumeshop.common.dto.PageResponse;
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
    public PageResponse<AdminPerfumeCardDTO> getAllPerfumes(int page, int size) {
        Page<Perfume> perfumesPage = perfumeRepo.findAll(PageRequest.of(page, size));
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
                .quantity(createPerfumeRequest.getQuantity())
                .brand(createPerfumeRequest.getBrand())
                .build();
        itemRepo.save(item);

        // Create and save item price
        ItemPrice itemPrice = ItemPrice.builder()
                .price(createPerfumeRequest.getPrice())
                .item(item)
                .build();
        itemPriceRepo.save(itemPrice);

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

        // Create and save perfume-specific details
        Perfume perfume = Perfume.builder()
                .perfumeSize(createPerfumeRequest.getPerfumeSize())
                .perfumeType(createPerfumeRequest.getPerfumeType())
                .perfumeSeason(createPerfumeRequest.getPerfumeSeason())
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

        if (itemRepo.existsByNameAndBrandAndIdNot(
                updatePerfumeRequest.getName(),
                updatePerfumeRequest.getBrand(),
                item.getId())) {
            throw new AlreadyExistsException("item.already.exists");
        }

        // Update item
        item.setName(updatePerfumeRequest.getName());
        item.setBrand(updatePerfumeRequest.getBrand());
        item.setQuantity(updatePerfumeRequest.getQuantity());

        if (updatePerfumeRequest.getQuantity() == 0) {
            item.setActive(false);
        } else {
            item.setActive(updatePerfumeRequest.getActive());
        }

        // Update perfume
        perfume.setPerfumeSize(updatePerfumeRequest.getPerfumeSize());
        perfume.setPerfumeType(updatePerfumeRequest.getPerfumeType());
        perfume.setPerfumeSeason(updatePerfumeRequest.getPerfumeSeason());

        // Update price
        ItemPrice currentPrice = itemPriceRepo.findCurrentActivePriceByItemId(item.getId())
                .orElse(null);

        boolean priceChanged = currentPrice != null &&
                currentPrice.getPrice().compareTo(updatePerfumeRequest.getPrice()) != 0;

        if (priceChanged &&
                (updatePerfumeRequest.getNote() == null || updatePerfumeRequest.getNote().trim().isBlank())) {
            throw new ValidationException("item.note.required.when.price.changed");
        }

        if (priceChanged || currentPrice == null) {
            if (currentPrice != null) {
                currentPrice.setEffectiveTo(LocalDateTime.now());
                currentPrice.setNotes(updatePerfumeRequest.getNote());
                currentPrice.setIsActive(false);
                itemPriceRepo.save(currentPrice);
            }

            ItemPrice itemPrice = ItemPrice.builder()
                    .price(updatePerfumeRequest.getPrice())
                    .item(item)
                    .build();
            itemPriceRepo.save(itemPrice);
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
            ItemPrice price,
            ItemTranslation translation
    ) {
        Item item = perfume.getItem();

        if (primaryImage == null) {
            throw new NotFoundException("image.not.found");
        }

        AdminPerfumeCardDTO.AdminPerfumeCardDTOBuilder builder = AdminPerfumeCardDTO.builder()
                .id(perfume.getId())
                .name(item.getName())
                .brand(item.getBrand())
                .active(item.getActive())
                .quantity(item.getQuantity())
                .primaryImageUrl(BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + primaryImage.getId());

        Optional.ofNullable(price)
                .ifPresent(p -> builder.currentPrice(p.getPrice()));

        Optional.ofNullable(translation)
                .ifPresent(t -> builder.translatedName(t.getName()));

        return builder.build();
    }

    private void validateCreatePerfumeRequest(
            CreatePerfumeRequest createPerfumeRequest,
            List<MultipartFile> images,
            Integer primaryImageIndex,
            List<Integer> imageOrder
    ){
        if (images == null || images.isEmpty()){
            throw new ValidationException("images.perfume.required");
        }

        if (images.size() > 5){
            throw new MaxImagesExceededException("image.perfume.limit.exceeded");
        }

        if (imageOrder == null || imageOrder.size() != images.size()) {
            throw new ValidationException("image.order.size.mismatch");
        }

        if (primaryImageIndex == null) {
            throw new ValidationException("image.primary.required");
        }

        if (primaryImageIndex < 0 || primaryImageIndex >= images.size()) {
            throw new ValidationException("image.primary.index.invalid");
        }

        Set<Integer> orderSet = new HashSet<>(imageOrder);
        if (orderSet.size() != images.size() ||
                !orderSet.containsAll(IntStream.range(0, images.size()).boxed().toList())) {
            throw new ValidationException("image.order.invalid");
        }

        if (itemRepo.existsByNameAndBrand(createPerfumeRequest.getName(), createPerfumeRequest.getBrand())) {
            throw new AlreadyExistsException("item.already.exists");
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
