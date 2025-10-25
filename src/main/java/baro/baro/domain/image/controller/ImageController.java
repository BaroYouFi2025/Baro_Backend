package baro.baro.domain.image.controller;

import baro.baro.domain.image.dto.ImageUploadResponse;
import baro.baro.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Tag(name = "이미지", description = "이미지 업로드 및 관리 API")
public class ImageController {
    
    private final ImageService imageService;
    
    @Operation(
        summary = "이미지 업로드", 
        description = "이미지 파일을 업로드하고 접근 가능한 URL을 반환합니다. 인증된 사용자만 사용 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "업로드 성공",
            content = @Content(
                schema = @Schema(implementation = ImageUploadResponse.class),
                examples = @ExampleObject(
                    value = "{\"url\": \"http://localhost:8080/images/2025/01/26/abc-123-def.jpg\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (파일 없음, 이미지가 아님, 파일 크기 초과)",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @Parameter(
                description = "업로드할 이미지 파일 (jpg, png, webp 등)",
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file
    ) {
        String url = imageService.uploadImage(file);
        return ResponseEntity.ok(ImageUploadResponse.create(url));
    }
}
