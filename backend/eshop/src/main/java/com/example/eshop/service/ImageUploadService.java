package com.example.eshop.service;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class ImageUploadService {

    private final Cloudinary cloudinary;
    private static final String PRODUCT_FOLDER = "products";

    public ImageUploadService(@Value("${CLOUDINARY_URL}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    public String uploadImage(MultipartFile image, String productName){
        Map param = ObjectUtils.asMap(
                "public_id", toSKU(productName),
                "folder", PRODUCT_FOLDER,
                "overwrite", true,
                "use_filename", true,
                "unique_filename", false
        );

        try {
            Map uploadResult = cloudinary.uploader().upload(image.getBytes(), param);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    public String renameImage(String oldName, String newName) {
        try {
            String oldPublicId = PRODUCT_FOLDER + "/" + toSKU(oldName);
            String newPublicId = PRODUCT_FOLDER + "/" + toSKU(newName);

            if (newPublicId.equalsIgnoreCase(oldPublicId)){
                return null;
            }

            Map result = cloudinary.uploader().rename(oldPublicId, newPublicId, ObjectUtils.asMap("invalidate", true));
            return (String) result.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Failed to rename image on Cloudinary", e);
        }
    }

    public void deleteImage(String productName) {
        try {
            String publicId = PRODUCT_FOLDER + "/" + toSKU(productName);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Failed to delete image for product '{}' from Cloudinary: {}", productName, e.getMessage());
        }
    }

    // Private helper method
    private String toSKU(String productName){

        return productName.trim()
                .toLowerCase()
                .replaceAll("[\\s&?#\\\\%<>+'_]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
