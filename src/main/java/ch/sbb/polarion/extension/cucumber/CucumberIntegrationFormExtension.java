package ch.sbb.polarion.extension.cucumber;

import ch.sbb.polarion.extension.generic.util.ExtensionInfo;
import com.polarion.alm.shared.api.SharedContext;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.shared.api.utils.html.HtmlFragmentBuilder;
import com.polarion.alm.shared.api.utils.links.HtmlLinkFactory;
import com.polarion.alm.tracker.model.IAttachmentBase;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.alm.ui.server.forms.extensions.IFormExtension;
import com.polarion.alm.ui.server.forms.extensions.IFormExtensionContext;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"java:S1192", "unchecked"})
public class CucumberIntegrationFormExtension implements IFormExtension {

    public static final String ID = "cucumber";
    private static final Logger logger = Logger.getLogger(CucumberIntegrationFormExtension.class);
    private final String bundleTimestamp = ExtensionInfo.getInstance().getVersion().getBundleBuildTimestampDigitsOnly();

    @NotNull
    public String getContent(IWorkItem workItem, IPObjectList<IPObject> attachments) throws IOException {
        String content = "";
        List<IPObject> list = attachments.stream()
                .filter(IAttachmentBase.class::isInstance)
                .filter(x -> ((IAttachmentBase) x).getFileName().equals(workItem.getId() + ".feature"))
                .toList();

        for (IPObject attachment : list) {
            byte[] attachmentContent = ((IAttachmentBase) attachment).getDataStream().readAllBytes();
            content = new String(attachmentContent, StandardCharsets.UTF_8);
        }

        return content;
    }

    @Override
    @Nullable
    public String render(@NotNull IFormExtensionContext context) {
        boolean validateOnSave = Boolean.parseBoolean(context.attributes().getOrDefault("validateOnSave", "false"));
        return TransactionalExecutor.executeSafelyInReadOnlyTransaction(
                transaction -> renderIntegrationTest(transaction.context(), context.object().getOldApi(), validateOnSave));
    }

    @Override
    @Nullable
    public String getIcon(@NotNull IPObject object, @Nullable Map<String, String> attributes) {
        return null;
    }

    @Override
    @Nullable
    public String getLabel(@NotNull IPObject object, @Nullable Map<String, String> attributes) {
        return "Cucumber Test";
    }

    public String renderIntegrationTest(@NotNull SharedContext context, @NotNull IPObject object, boolean validateOnSave) {
        HtmlFragmentBuilder builder = context.createHtmlFragmentBuilderFor().gwt();

        try {
            if (object instanceof IWorkItem workItem) {
                if (object.isPersisted()) {
                    IPObjectList<IPObject> attachments = workItem.getAttachments();

                    String content = getContent(workItem, attachments);

                    displayContentInEditor(builder, workItem, content, validateOnSave);
                } else {
                    builder.tag().div().append().text("Cucumber editor will be available after Work Item created.");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            builder.tag().div().append().tag().b().append().text("Unknown error - see server log for more information.");
        }

        builder.finished();
        return builder.toString();
    }

    public void addSource(HtmlFragmentBuilder builder, String type, String url) {
        builder.tag().script().attributes().type(type)
                .src(HtmlLinkFactory.fromEncodedRelativeUrl(url));
    }

    private void addCss(HtmlFragmentBuilder builder) {
        builder.html("<link rel='stylesheet' href='/polarion/cucumber/ui/css/petrel.css?bundle=" + bundleTimestamp + "'>");
        builder.html("<link rel='stylesheet' href='/polarion/cucumber/ui/css/highlightjs.css?bundle=" + bundleTimestamp + "'>");
        builder.html("<link rel='stylesheet' href='/polarion/cucumber/ui/css/cucumber.css?bundle=" + bundleTimestamp + "'>");
    }

    private void displayContentInEditor(@NotNull HtmlFragmentBuilder builder, @NotNull IWorkItem workItem,
                                        @NotNull String content, boolean validateOnSave) {
        addCss(builder);

        String filename = workItem.getId() + ".feature";

        builder.html(""
                + "<div class='editor-buttons'>"
                + "  <button type='button' id='edit-feature-button' onclick='handleEditFeature()'>"
                + "    <img class='append-build-number' src='/polarion/ria/images/actions/edit.gif'></img>Edit"
                + "  </button>"
                + "  <button type='button' class='divider'>&nbsp;</button>"
                + "  <button type='button' id='validate-feature-button' disabled onclick='handleValidateFeature()'>"
                + "    <img class='append-build-number' src='/polarion/icons/default/enums/req_status_reviewed.gif'></img>Validate"
                + "  </button>"
                + "  <button type='button' id='save-feature-button' disabled margin-left:5px;' onclick=\"handleSaveFeature('"
                + workItem.getProjectId() + "', '" + workItem.getId() + "', '" + filename + "', '" + validateOnSave + "')\">"
                + "    <img class='append-build-number'src='/polarion/ria/images/actions/save.gif'></img>Save"
                + "  </button>"
                + "  <button type='button' id='cancel-edit-feature-button' disabled onclick='handleCancelEditFeature()'>"
                + "    <img class='append-build-number' src='/polarion/ria/images/actions/cancel.gif'></img>Cancel"
                + "  </button>"
                + "</div>"
                + "<div style='margin-bottom: .5em;'>"
                + "  <span id='feature-validation-result'></span>"
                + "</div>"
        );

        builder.html(""
                + "<div class='editor-wrapper'>"
                + "  <div id='cucumberFeatureCodeEditor'></div>"
                + "</div>"
                + "<div id='cucumberFeatureCodeEditorOriginalContent' style='display: none;'>" + HtmlUtils.htmlEscape(content) + "</div>"
                + "");

        addSource(builder, "module", "/polarion/cucumber/ui/js/gherkin-editor.js?bundle=" + UUID.randomUUID());
        addSource(builder, "text/javascript", "/polarion/cucumber/ui/js/cucumber.js?bundle=" + bundleTimestamp);
    }
}
