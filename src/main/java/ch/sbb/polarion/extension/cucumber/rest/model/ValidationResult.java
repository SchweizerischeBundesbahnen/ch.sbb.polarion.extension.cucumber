package ch.sbb.polarion.extension.cucumber.rest.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents the result of a validation")
public class ValidationResult {
    @Schema(description = "The overall result of the validation process", example = "success")
    private String result;

    @ArraySchema(schema = @Schema(description = "A list of validation errors encountered during the validation process", implementation = ValidationError.class))
    private List<ValidationError> errors;
}
