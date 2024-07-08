package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import com.polarion.core.util.types.Text;
import com.polarion.subterra.base.data.model.IType;
import com.polarion.subterra.base.data.model.internal.EnumType;
import com.polarion.subterra.base.data.model.internal.ListType;
import com.polarion.subterra.base.data.model.internal.PrimitiveType;

import java.util.Arrays;
import java.util.function.Function;

public enum FieldType {

    STRING(FieldType.TYPE_STRING, null,
            type -> new PrimitiveType(String.class.getName(), null).equals(type),
            new TextConverter(false)),

    STRING_LIST(FieldType.TYPE_ARRAY, FieldType.TYPE_STRING,
            type -> type.equals(new PrimitiveType(Text.class.getName(), "html")),
            new StringListConverter()),

    TEXT(FieldType.TYPE_STRING, null,
            type -> new PrimitiveType(Text.class.getName(), null).equals(type),
            new TextConverter(true)),

    ENUM(FieldType.TYPE_OPTION, null,
            EnumType.class::isInstance,
            new EnumConverter()),

    COMPONENTS_LIST(FieldType.TYPE_ARRAY, FieldType.TYPE_COMPONENT,
            type -> type instanceof ListType listType && listType.getItemType() instanceof EnumType,
            new EnumConverter());

    private static final String TYPE_STRING = "string";
    private static final String TYPE_ARRAY = "array";
    private static final String TYPE_OPTION = "option";
    private static final String TYPE_COMPONENT = "component";

    private final Function<IType, Boolean> typeMatcher;
    private final String mavenPluginItemsName;
    private final String mavenPluginTypeName;
    private final FieldValueConverter converter;

    FieldType(String mavenPluginTypeName, String mavenPluginItemsName,
              Function<IType, Boolean> typeMatcher, FieldValueConverter converter) {
        this.mavenPluginItemsName = mavenPluginItemsName;
        this.typeMatcher = typeMatcher;
        this.mavenPluginTypeName = mavenPluginTypeName;
        this.converter = converter;
    }

    public static FieldType fromIType(IType type) {
        return Arrays.stream(values()).filter(t -> t.typeMatcher.apply(type)).findFirst().orElse(null);
    }

    public String getMavenPluginTypeName() {
        return mavenPluginTypeName;
    }

    public String getMavenPluginItemsName() {
        return mavenPluginItemsName;
    }

    public FieldValueConverter getConverter() {
        return converter;
    }
}
