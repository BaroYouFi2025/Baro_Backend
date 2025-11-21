package baro.baro.domain.ai.service;

import baro.baro.domain.ai.entity.AssetType;
import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.service.generator.AiImageGenerationStrategy;
import baro.baro.domain.missingperson.entity.MissingPerson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleGenAiService {

    private final List<AiImageGenerationStrategy> generationStrategies;

    public List<String> generateImages(MissingPerson missingPerson, AssetType assetType) {
        return generationStrategies.stream()
                .filter(strategy -> strategy.supports(assetType))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("지원되지 않는 AssetType: {}", assetType);
                    return new AiException(AiErrorCode.INVALID_ASSET_TYPE);
                })
                .generate(missingPerson);
    }
}
