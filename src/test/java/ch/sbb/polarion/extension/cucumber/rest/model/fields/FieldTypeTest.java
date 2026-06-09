package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import com.polarion.core.util.types.Text;
import com.polarion.subterra.base.data.model.internal.EnumType;
import com.polarion.subterra.base.data.model.internal.ListType;
import com.polarion.subterra.base.data.model.internal.PrimitiveType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class FieldTypeTest {

    @Test
    void fromITypeResolvesString() {
        assertEquals(FieldType.STRING, FieldType.fromIType(new PrimitiveType(String.class.getName(), null)));
    }

    @Test
    void fromITypeResolvesText() {
        assertEquals(FieldType.TEXT, FieldType.fromIType(new PrimitiveType(Text.class.getName(), null)));
    }

    @Test
    void fromITypeResolvesStringList() {
        assertEquals(FieldType.STRING_LIST, FieldType.fromIType(new PrimitiveType(Text.class.getName(), "html")));
    }

    @Test
    void fromITypeResolvesEnum() {
        assertEquals(FieldType.ENUM, FieldType.fromIType(new EnumType("priority")));
    }

    @Test
    void fromITypeResolvesComponentsListWhenItemTypeIsEnum() {
        ListType listType = new ListType("array", new EnumType("component"));
        assertEquals(FieldType.COMPONENTS_LIST, FieldType.fromIType(listType));
    }

    @Test
    void fromITypeReturnsNullForListWithNonEnumItemType() {
        ListType listType = new ListType("array", new PrimitiveType(String.class.getName(), null));
        assertNull(FieldType.fromIType(listType));
    }

    @Test
    void getters() {
        assertEquals("array", FieldType.COMPONENTS_LIST.getMavenPluginTypeName());
        assertEquals("component", FieldType.COMPONENTS_LIST.getMavenPluginItemsName());
        assertInstanceOf(EnumConverter.class, FieldType.COMPONENTS_LIST.getConverter());
    }
}
