package ch.sbb.polarion.extension.cucumber.rest.model;

import io.cucumber.messages.types.ParseError;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a validation error with details about the error message, line, and column number")
public class ValidationError {

    @Schema(description = "The error message describing the validation issue")
    private String message;

    @Schema(description = "The line number where the validation error occurred")
    private Long line;

    @Schema(description = "The column number where the validation error occurred")
    private Long column;

    public static ValidationError fromParseError(ParseError parseError) {
        ValidationError validationError = new ValidationError();
        validationError.setMessage(parseError.getMessage());
        if (parseError.getSource() != null) {
            parseError.getSource().getLocation().ifPresent(location -> {
                validationError.setLine(location.getLine());
                location.getColumn().ifPresent(validationError::setColumn);
            });
        }
        return validationError;
    }
}
