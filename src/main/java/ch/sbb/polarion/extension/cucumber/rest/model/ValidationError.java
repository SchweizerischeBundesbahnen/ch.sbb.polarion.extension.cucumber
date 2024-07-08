package ch.sbb.polarion.extension.cucumber.rest.model;

import io.cucumber.messages.types.ParseError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    private String message;
    private Long line;
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
