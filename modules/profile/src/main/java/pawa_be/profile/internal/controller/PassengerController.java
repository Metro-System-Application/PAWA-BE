package pawa_be.profile.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.profile.internal.dto.*;
import pawa_be.profile.internal.enumeration.ImageType;
import pawa_be.profile.internal.service.PassengerService;

import static pawa_be.infrastructure.jwt.misc.Miscellaneous.getUserIdFromAuthentication;

@SecurityRequirement(name = "user_auth")
@RestController
@RequestMapping("/profile")
@Tag(name = "Profile Controller", description = "Operations about passengers")
@RequiredArgsConstructor
class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @GetMapping("/my-info")
    @Operation(
            summary = "Get passenger info",
            description = "Returns the currently logged-in passenger info"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger info retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    ResponseEntity<GenericResponseDTO<ResponsePassengerDTO>> getPassengerInfo(
            @Parameter(hidden = true) Authentication authentication) {

        final String passengerId = getUserIdFromAuthentication(authentication);
        ResponsePassengerDTO passenger = passengerService.getCurrentPassengerById(passengerId);
        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Passenger info retrieved", passenger));
    }

    @PutMapping("/edit-my-info")
    @Operation(
            summary = "Update passenger info",
            description = "Edits the currently logged-in passenger info with optional fields"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger info updated successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    ResponseEntity<GenericResponseDTO<ResponsePassengerDTO>> updatePassengerInfo(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody
            @Parameter(description = "Updated phone number and/or address") RequestUpdatePassengerDTO updatedInfo) {

        final String passengerId = getUserIdFromAuthentication(authentication);
        passengerService.updateCurrentPassengerById(passengerId, updatedInfo);
        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Passenger info updated", null));
    }

    @PostMapping(value = "/upload-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload or update passenger profile image",
            description = "Uploads or updates the profile picture for the authenticated passenger."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class, example = """
                        {
                            "success": true,
                            "message": "Profile image uploaded",
                            "data": null
                        }
                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Bad request - missing or invalid input", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<GenericResponseDTO<?>> uploadProfileImage(
            @Parameter(hidden = true) Authentication authentication,

            @Parameter(description = "Profile image file", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        String passengerId = getUserIdFromAuthentication(authentication);

        passengerService.uploadOrUpdatePassengerImage(passengerId,
                new RequestUploadImageDTO(ImageType.USER_PROFILE, file));

        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Profile image uploaded", null));
    }

    @PostMapping(value = "/upload-card-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Card images uploaded successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class, example = """
                    {
                        "success": true,
                        "message": "Card images uploaded successfully",
                        "data": null
                    }
                """))
            ),
            @ApiResponse(responseCode = "400", description = "Bad request - missing or invalid input", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @Operation(summary = "Upload both front and back of a card (STUDENT_ID or NATIONAL_ID)")
    ResponseEntity<GenericResponseDTO<?>> uploadCardImages(
            @Parameter(hidden = true) Authentication authentication,

            @RequestParam("frontImageType") ImageType frontImageType,
            @RequestPart("frontFile") MultipartFile frontFile,

            @RequestParam("backImageType") ImageType backImageType,
            @RequestPart("backFile") MultipartFile backFile
    ) {
        if (!ImageType.isMatchingPair(frontImageType, backImageType)) {
            throw new IllegalArgumentException("Provided image types do not form a valid front-back pair.");
        }

        String passengerId = getUserIdFromAuthentication(authentication);

        passengerService.uploadOrUpdatePassengerImage(passengerId,
                new RequestUploadImageDTO(frontImageType, frontFile));
        passengerService.uploadOrUpdatePassengerImage(passengerId,
                new RequestUploadImageDTO(backImageType, backFile));

        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Card images uploaded successfully", null));
    }

    @GetMapping("/profile-image")
    @Operation(summary = "Get profile image", description = "Returns the base64-encoded profile picture for the logged-in passenger")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile image retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class, example = """
                        {
                            "success": true,
                            "message": "Profile image retrieved",
                            "data": {
                                "profileImage": {
                                    "base64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA...",
                                    "mimeType": "image/png",
                                    "imageType": "USER_PROFILE"
                                }
                            }
                        }
                    """))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profile image not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<GenericResponseDTO<ResponseProfileImageDTO>> getProfileImage(
            @Parameter(hidden = true) Authentication authentication) {

        String passengerId = getUserIdFromAuthentication(authentication);
        ResponseProfileImageDTO profileImageDTO = passengerService.getProfileImage(passengerId);

        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Profile image retrieved", profileImageDTO));
    }


    @GetMapping("/card-images")
    @Operation(summary = "Get card images", description = "Returns the base64-encoded student ID and national ID images for the logged-in passenger")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Card images retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class, example = """
                        {
                            "success": true,
                            "message": "Card images retrieved",
                            "data": {
                                "studentIdPictures": [
                                    {
                                        "base64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...",
                                        "mimeType": "image/jpeg",
                                        "imageType": "STUDENT_ID_FRONT"
                                    },
                                    {
                                        "base64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...",
                                        "mimeType": "image/jpeg",
                                        "imageType": "STUDENT_ID_BACK"
                                    }
                                ],
                                "nationalIdPictures": [
                                    {
                                        "base64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...",
                                        "mimeType": "image/jpeg",
                                        "imageType": "NATIONAL_ID_FRONT"
                                    },
                                    {
                                        "base64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...",
                                        "mimeType": "image/jpeg",
                                        "imageType": "NATIONAL_ID_BACK"
                                    }
                                ]
                            }
                        }
                    """))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "One or more card images not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<GenericResponseDTO<ResponseCardImageDTO>> getCardImages(
            @Parameter(hidden = true) Authentication authentication) {

        String passengerId = getUserIdFromAuthentication(authentication);
        ResponseCardImageDTO cardImagesDTO = passengerService.getCardImagesBase64(passengerId);

        return ResponseEntity.ok(new GenericResponseDTO<>(true, "Card images retrieved", cardImagesDTO));
    }
}
