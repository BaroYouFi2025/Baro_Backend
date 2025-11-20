package baro.baro.domain.ai.service.generator;

import baro.baro.domain.ai.entity.AssetType;
import baro.baro.domain.missingperson.entity.MissingPerson;

import java.util.List;

public interface AiImageGenerationStrategy {

    boolean supports(AssetType assetType);

    List<String> generate(MissingPerson missingPerson);
}
