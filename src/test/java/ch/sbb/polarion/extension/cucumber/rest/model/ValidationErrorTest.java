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
        ParseError parseError = new ParseError(SourceReference.of(new Location(12, 5)), "syntax error");

        ValidationError error = ValidationError.fromParseError(parseError);

        assertEquals("syntax error", error.getMessage());
        assertEquals(12, error.getLine());
        assertEquals(5, error.getColumn());
    }

    @Test
    void fromParseErrorWithoutColumn() {
        ParseError parseError = new ParseError(SourceReference.of(new Location(7, null)), "no column");

        ValidationError error = ValidationError.fromParseError(parseError);

        assertEquals("no column", error.getMessage());
        assertEquals(7, error.getLine());
        assertNull(error.getColumn());
    }

    @Test
    void dataObjectAccessors() {
        ValidationError error = new ValidationError("message", 3, 4);
        assertEquals("message", error.getMessage());
        assertEquals(3, error.getLine());
        assertEquals(4, error.getColumn());
        assertEquals(new ValidationError("message", 3, 4), error);
    }
}
