package ch.sbb.polarion.extension.cucumber;

import ch.sbb.polarion.extension.generic.test_extensions.PlatformContextMockExtension;
import com.polarion.alm.portal.web.shared.layouts.sections.ExtensionSection;
import com.polarion.alm.portal.web.shared.layouts.sections.SourceLayout;
import com.polarion.alm.portal.web.shared.layouts.sections.VerticalSection;
import com.polarion.alm.projects.model.IProject;
import com.polarion.alm.server.ServerUiContext;
import com.polarion.alm.shared.api.SharedContext;
import com.polarion.alm.shared.api.model.ModelObject;
import com.polarion.alm.shared.api.model.document.Document;
import com.polarion.alm.shared.api.transaction.ReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.RunnableInReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.shared.api.utils.html.HtmlBuilderTargetSelector;
import com.polarion.alm.shared.api.utils.html.HtmlFragmentBuilder;
import com.polarion.alm.tracker.model.IAttachmentBase;
import com.polarion.alm.tracker.model.ICategory;
import com.polarion.alm.tracker.model.ITypeOpt;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.alm.tracker.web.internal.server.PDIConfigResolver;
import com.polarion.alm.ui.server.forms.extensions.IFormExtensionContext;
import com.polarion.alm.ui.server.forms.extensions.impl.FormExtensionContextImpl;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.spi.PObjectList;
import com.polarion.subterra.base.data.identification.IContextId;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.polarion.platform.persistence.model.IPObjectList.EMPTY_POBJECTLIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, PlatformContextMockExtension.class})
@SuppressWarnings({"unchecked", "rawtypes"})
class CucumberIntegrationFormExtensionTest {
    @Mock
    private CucumberIntegrationFormExtension extension;
    @Mock
    private IWorkItem iWorkItem;
    @Mock
    private IFormExtensionContext formContext;
    @Mock
    private SharedContext context;
    @Mock
    private HtmlBuilderTargetSelector<HtmlFragmentBuilder> builder;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HtmlFragmentBuilder htmlFragmentBuilder;
    @Mock
    private ReadOnlyTransaction transaction;
    @Mock
    private SourceLayout layout;

    private MockedStatic<TransactionalExecutor> transactionalExecutor;
    private MockedStatic<IOUtils> ioUtils;
    private MockedStatic<PDIConfigResolver> configResolver;

    private static Stream<Arguments> testValuesForRenderIntegrationTestIsPersistedIssue() {

        var workItem = mock(IWorkItem.class);
        var iProject = mock(IProject.class);

        return Stream.of(
                Arguments.of(false, workItem),
                Arguments.of(true, iProject),
                Arguments.of(false, iProject)
        );
    }

    private static Stream<Arguments> testValuesForgetContentWithAttachment() {

        var object = mock(IWorkItem.class);
        when(object.getId()).thenReturn("id");

        var attachment = mock(IAttachmentBase.class);
        when(attachment.getFileName()).thenReturn("id.feature");
        when(attachment.getDataStream()).thenAnswer(arg -> new ByteArrayInputStream("test".getBytes()));

        var attachment2 = mock(ICategory.class);

        var attachment3 = mock(IAttachmentBase.class);
        when(attachment3.getFileName()).thenReturn("id3.feature");
        when(attachment3.getDataStream()).thenReturn(new ByteArrayInputStream("test3".getBytes()));

        var attachment4 = mock(IAttachmentBase.class);
        when(attachment4.getFileName()).thenReturn("id.feature");
        when(attachment4.getDataStream()).thenReturn(new ByteArrayInputStream("test4".getBytes()));

        return Stream.of(
                Arguments.of(object, new PObjectList(null, List.of(attachment)), "test"),
                Arguments.of(object, new PObjectList(null, List.of(attachment, attachment2)), "test"),
                Arguments.of(object, new PObjectList(null, List.of(attachment, attachment2, attachment3)), "test"),
                Arguments.of(object, new PObjectList(null, List.of(attachment, attachment4)), "test4")
        );
    }

    @BeforeEach
    public void setup() {
        transactionalExecutor = mockStatic(TransactionalExecutor.class);
        transactionalExecutor.when(() -> TransactionalExecutor.executeSafelyInReadOnlyTransaction(any())).thenAnswer(invocation -> {
            RunnableInReadOnlyTransaction runnable = invocation.getArgument(0);
            return runnable.run(transaction);
        });
        ioUtils = mockStatic(IOUtils.class);
        ioUtils.when(() -> IOUtils.resourceToString(eq("layout/form.html"), any(), any()))
                .thenReturn("{BUNDLE},{PROJECT_ID},{WORK_ITEM_ID},{FILENAME},{VALIDATE},{CONTENT}");
        ioUtils.when(() -> IOUtils.resourceToString(eq("layout/not-configured.html"), any(), any()))
                .thenReturn("Not configured");

        configResolver = mockStatic(PDIConfigResolver.class);
        configResolver.when(() -> PDIConfigResolver.resolveComplexConfig(anyString(), any(), any(), any(), any()))
                .thenReturn(layout);
    }

    @AfterEach
    void cleanup() {
        transactionalExecutor.close();
        ioUtils.close();
        configResolver.close();
    }

    @Test
    void getLabel() {
        when(extension.getLabel(iWorkItem, new HashMap<>())).thenCallRealMethod();
        assertThat(extension.getLabel(iWorkItem, new HashMap<>()))
                .isEqualTo("Cucumber Test");
    }

    @Test
    void getIcon() {
        when(extension.getIcon(iWorkItem, new HashMap<>())).thenCallRealMethod();

        assertThat(extension.getIcon(iWorkItem, new HashMap<>())).isNull();
    }

    @ParameterizedTest
    @MethodSource("testValuesForRenderIntegrationTestIsPersistedIssue")
    void renderIntegrationTestIsPersistedIssue(Boolean isPersisted, IPObject object) {
        when(extension.renderIntegrationTest(formContext, context, object, true)).thenCallRealMethod();

        when(context.createHtmlFragmentBuilderFor()).thenReturn(builder);
        when(builder.gwt()).thenReturn(htmlFragmentBuilder);

        String builderText = "text";
        when(htmlFragmentBuilder.toString()).thenReturn(builderText);
        when(htmlFragmentBuilder.tag().div().append().tag().b().append().text(anyString())).thenReturn(htmlFragmentBuilder);

        when(object.isPersisted()).thenReturn(isPersisted);

        assertThat(extension.renderIntegrationTest(formContext, context, object, true)).isEqualTo(builderText);

        verify(htmlFragmentBuilder, times(0)).html(anyString());
        verify(htmlFragmentBuilder, times(1)).finished();
    }

    @Test
    void renderIntegrationTestWithProjectAsWorkItem() throws IOException {
        var object = mock(IWorkItem.class);

        when(extension.renderIntegrationTest(formContext, context, object, true)).thenCallRealMethod();

        when(extension.getContent(object, EMPTY_POBJECTLIST)).thenCallRealMethod();

        when(context.createHtmlFragmentBuilderFor()).thenReturn(builder);
        when(builder.gwt()).thenReturn(htmlFragmentBuilder);

        String builderText = "text";
        when(htmlFragmentBuilder.toString()).thenReturn(builderText);

        when(object.getId()).thenReturn("WI-1");
        when(object.getProjectId()).thenReturn("TestProjectId");
        when(object.isPersisted()).thenReturn(true);
        when(object.getAttachments()).thenReturn(EMPTY_POBJECTLIST);

        assertThat(extension.renderIntegrationTest(formContext, context, object, true)).isEqualTo(builderText);

        verify(htmlFragmentBuilder, times(1)).html(",TestProjectId,WI-1,WI-1.feature,true,");
        verify(htmlFragmentBuilder, times(1)).finished();
    }

    @Test
    void renderDocumentSidebarFormDependingOnWorkItemType() throws IOException {
        var object = mock(IWorkItem.class);

        when(extension.renderIntegrationTest(any(), any(), any(), anyBoolean())).thenCallRealMethod();

        when(extension.getContent(object, EMPTY_POBJECTLIST)).thenCallRealMethod();

        when(object.getId()).thenReturn("WI-1");
        when(object.getType()).thenReturn(mock(ITypeOpt.class));
        when(object.getContextId()).thenReturn(mock(IContextId.class));
        when(object.getProjectId()).thenReturn("TestProjectId");
        when(object.isPersisted()).thenReturn(true);
        when(object.getAttachments()).thenReturn(EMPTY_POBJECTLIST);

        FormExtensionContextImpl formContextImpl = mock(FormExtensionContextImpl.class, RETURNS_DEEP_STUBS);
        formContextImpl.contextObject = mock(Document.class, RETURNS_DEEP_STUBS);
        ServerUiContext sharedContext = mock(ServerUiContext.class, RETURNS_DEEP_STUBS);

        when(sharedContext.currentUiRole()).thenReturn("someRole");
        VerticalSection verticalSection = mock(VerticalSection.class);
        when(layout.getRootSection()).thenReturn(verticalSection);

        when(verticalSection.getRows()).thenReturn(new ArrayList<>(List.of()));

        assertThat(extension.renderIntegrationTest(formContextImpl, sharedContext, object, true)).isEqualTo("Not configured");

        ExtensionSection section = mock(ExtensionSection.class);
        when(section.getExtenstionId()).thenReturn("cucumber");
        when(verticalSection.getRows()).thenReturn(new ArrayList<>(List.of(section)));

        when(sharedContext.createHtmlFragmentBuilderFor()).thenReturn(builder);
        when(builder.gwt()).thenReturn(htmlFragmentBuilder);

        extension.renderIntegrationTest(formContextImpl, sharedContext, object, true);
        verify(htmlFragmentBuilder, times(1)).html(",TestProjectId,WI-1,WI-1.feature,true,");
    }

    @Test
    void getContentWithoutAttachment() throws IOException {
        var object = mock(IWorkItem.class);
        when(extension.getContent(object, EMPTY_POBJECTLIST)).thenCallRealMethod();

        var b = extension.getContent(object, EMPTY_POBJECTLIST);
        assertThat(b).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("testValuesForgetContentWithAttachment")
    void getContentWithAttachment(IWorkItem object, PObjectList list, String expected) throws IOException {
        when(extension.getContent(object, list)).thenCallRealMethod();
        var result = extension.getContent(object, list);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testRender() {
        when(formContext.object()).thenReturn(mock(ModelObject.class, RETURNS_DEEP_STUBS));
        when(extension.render(any())).thenCallRealMethod();

        when(formContext.attributes()).thenReturn(Map.of("validateOnSave", "true"));
        extension.render(formContext);
        verify(extension, times(1)).renderIntegrationTest(any(), any(), any(), eq(true));

        when(formContext.attributes()).thenReturn(Map.of("validateOnSave", "false"));
        extension.render(formContext);
        verify(extension, times(1)).renderIntegrationTest(any(), any(), any(), eq(false));
    }
}
