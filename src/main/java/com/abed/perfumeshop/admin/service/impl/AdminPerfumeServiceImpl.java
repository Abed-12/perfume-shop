package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.Item.dto.CreatePerfumeRequest;
import com.abed.perfumeshop.Item.dto.ItemTranslationRequest;
import com.abed.perfumeshop.Item.dto.UpdatePerfumeRequest;
import com.abed.perfumeshop.Item.entity.*;
import com.abed.perfumeshop.Item.repo.*;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.admin.service.AdminPerfumeService;
import com.abed.perfumeshop.common.exception.*;
import com.abed.perfumeshop.common.res.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminPerfumeServiceImpl implements AdminPerfumeService {

    private final ItemRepo itemRepo;
    private final PerfumeRepo perfumeRepo;
    private final ItemPriceRepo itemPriceRepo;
    private final ItemTranslationRepo itemTranslationRepo;
    private final PerfumeImageRepo perfumeImageRepo;
    private final AdminHelper adminHelper;

    @Override
    @Transactional
    public Response<?> createPerfume(
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

        return Response.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("perfume.created")
                .build();
    }

    @Override
    @Transactional
    public Response<?> updatePerfume(Long id, UpdatePerfumeRequest updatePerfumeRequest) {
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

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("perfume.updated")
                .build();
    }

    @Override
    @Transactional
    public Response<?> addPerfumeImage(
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

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("image.added.successfully")
                .build();
    }

    @Override
    @Transactional
    public Response<?> updatePerfumeImage(
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

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("image.updated.successfully")
                .build();
    }

    @Override
    @Transactional
    public Response<?> deletePerfumeImage(Long perfumeId, Long imageId) {
        adminHelper.getCurrentLoggedInUser();

        PerfumeImage perfumeImage = perfumeImageRepo.findByIdAndPerfumeId(imageId, perfumeId)
                .orElseThrow(() -> new NotFoundException("image.not.found"));

        if (perfumeImage.getIsPrimary()) {
            throw new InvalidOperationException("image.primary.cannot.delete");
        }

        perfumeImageRepo.delete(perfumeImage);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("image.deleted.successfully")
                .build();
    }

    // ========== Private Helper Methods ==========
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
