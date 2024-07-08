package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import ch.sbb.polarion.extension.generic.fields.model.FieldMetadata;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class FieldDefinition {

    public static final FieldDefinition TITLE = fromStaticFieldMapping("title", "summary");
    public static final FieldDefinition DESCRIPTION = fromStaticFieldMapping("description", "description");
    public static final FieldDefinition TEST_RUN_TEMPLATE_ID = fromStaticFieldMapping("testRunTemplateId", "testRunTemplateId");

    private final String id;
    private final String name;
    private final boolean custom;
    private final List<String> clauseNames;
    private final Schema schema;

    private FieldDefinition(String id, String name, boolean custom, List<String> clauseNames, Schema schema) {
        this.id = id;
        this.name = name;
        this.custom = custom;
        this.clauseNames = clauseNames;
        this.schema = schema;
    }

    public static FieldDefinition fromFieldMetadata(FieldMetadata fieldMetadata) {
        FieldType fieldType = FieldType.fromIType(fieldMetadata.getType());
        return new FieldDefinition(
                fieldMetadata.getId(),
                fieldMetadata.getLabel(),
                fieldMetadata.isCustom(),
                Collections.singletonList(fieldMetadata.getLabel()),
                new Schema(fieldType.getMavenPluginTypeName(), fieldType.getMavenPluginItemsName())
        );
    }

    private static FieldDefinition fromStaticFieldMapping(String staticFieldId, String staticFieldClauseName) {
        return new FieldDefinition(
                staticFieldId,
                staticFieldClauseName,
                false,
                Collections.singletonList(staticFieldClauseName),
                new Schema(FieldType.STRING.getMavenPluginTypeName())
        );
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isCustom() {
        return custom;
    }

    public List<String> getClauseNames() {
        return clauseNames;
    }

    public Schema getSchema() {
        return schema;
    }

    public static class Schema {

        private final String type;
        private final String items;

        private Schema(String type) {
            this.type = type;
            this.items = null;
        }

        public Schema(String type, String items) {
            this.type = type;
            this.items = items;
        }

        public String getType() {
            return type;
        }

        public String getItems() {
            return items;
        }
    }
}
