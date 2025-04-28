package ch.sbb.polarion.extension.cucumber;

import ch.sbb.polarion.extension.generic.util.ExtensionInfo;
import com.polarion.alm.portal.web.shared.layouts.sections.ExtensionSection;
import com.polarion.alm.portal.web.shared.layouts.sections.SourceLayout;
import com.polarion.alm.portal.web.shared.layouts.sections.VerticalSection;
import com.polarion.alm.server.ServerUiContext;
import com.polarion.alm.shared.api.SharedContext;
import com.polarion.alm.shared.api.model.document.Document;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.shared.api.utils.html.HtmlFragmentBuilder;
import com.polarion.alm.shared.api.utils.links.HtmlLinkFactory;
import com.polarion.alm.tracker.model.IAttachmentBase;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.alm.tracker.web.internal.server.LayoutDataHandler;
import com.polarion.alm.tracker.web.internal.server.PDIConfigResolver;
import com.polarion.alm.ui.server.forms.extensions.IFormExtension;
import com.polarion.alm.ui.server.forms.extensions.IFormExtensionContext;
import com.polarion.alm.ui.server.forms.extensions.impl.FormExtensionContextImpl;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectList;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
                transaction -> renderIntegrationTest(context, transaction.context(), context.object().getOldApi(), validateOnSave));
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

    public String renderIntegrationTest(@NotNull IFormExtensionContext formContext, @NotNull SharedContext context, @NotNull IPObject object, boolean validateOnSave) {
        HtmlFragmentBuilder builder = context.createHtmlFragmentBuilderFor().gwt();

        try {
            if (object instanceof IWorkItem workItem) {
                if (object.isPersisted()) {

                    // Extension may be rendered in the document's sidebar.
                    // In this case we must check explicitly whether the extension was configured for a currently chosen workitem.
                    if (shouldNotBeShown(formContext, context, workItem)) {
                        return IOUtils.resourceToString("layout/not-configured.html", StandardCharsets.UTF_8, getClass().getClassLoader());
                    }

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

    @SneakyThrows
    private void displayContentInEditor(@NotNull HtmlFragmentBuilder builder, @NotNull IWorkItem workItem,
                                        @NotNull String content, boolean validateOnSave) {
        String formContent = IOUtils.resourceToString("layout/form.html", StandardCharsets.UTF_8, getClass().getClassLoader());
        builder.html(formContent
                .replace("{BUNDLE}", bundleTimestamp)
                .replace("{PROJECT_ID}", workItem.getProjectId())
                .replace("{WORK_ITEM_ID}", workItem.getId())
                .replace("{FILENAME}", workItem.getId() + ".feature")
                .replace("{VALIDATE}", String.valueOf(validateOnSave))
                .replace("{CONTENT}", HtmlUtils.htmlEscape(content))
        );
    }

    private boolean shouldNotBeShown(@NotNull IFormExtensionContext context, @NotNull SharedContext sharedContext, IWorkItem workItem) {
        if (context instanceof FormExtensionContextImpl formExtensionContext && formExtensionContext.contextObject instanceof Document && sharedContext instanceof ServerUiContext serverUiContext) {
            SourceLayout layout = (SourceLayout) PDIConfigResolver.resolveComplexConfig(
                    serverUiContext.currentUiRole(), workItem.getType(), workItem.getContextId(), "form-layout.xml", new LayoutDataHandler());
            if (layout.getRootSection() instanceof VerticalSection verticalSection) {
                return verticalSection.getRows().stream().noneMatch(s -> s instanceof ExtensionSection es && es.getExtenstionId().equals(ID));
            }
        }
        return false;
    }
}
