package ch.sbb.polarion.extension.cucumber.rest.model;

import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.ParseError;
import io.cucumber.messages.types.SourceReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ValidationErrorTest {

    @Test
    void fromParseErrorWithLineAndColumn() {
        ParseError parseError = new ParseError(SourceReference.of(new Location(12L, 5L)), "syntax error");

        ValidationError error = ValidationError.fromParseError(parseError);

        assertEquals("syntax error", error.getMessage());
        assertEquals(12L, error.getLine());
        assertEquals(5L, error.getColumn());
    }

    @Test
    void fromParseErrorWithoutColumn() {
        ParseError parseError = new ParseError(SourceReference.of(new Location(7L, null)), "no column");

        ValidationError error = ValidationError.fromParseError(parseError);

        assertEquals("no column", error.getMessage());
        assertEquals(7L, error.getLine());
        assertNull(error.getColumn());
    }

    @Test
    void dataObjectAccessors() {
        ValidationError error = new ValidationError("message", 3L, 4L);
        assertEquals("message", error.getMessage());
        assertEquals(3L, error.getLine());
        assertEquals(4L, error.getColumn());
        assertEquals(new ValidationError("message", 3L, 4L), error);
    }
}
