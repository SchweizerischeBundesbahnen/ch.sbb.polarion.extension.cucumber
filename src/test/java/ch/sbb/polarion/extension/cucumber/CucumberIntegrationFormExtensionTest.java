package ch.sbb.polarion.extension.cucumber;

import com.polarion.alm.projects.model.IProject;
import com.polarion.alm.shared.api.SharedContext;
import com.polarion.alm.shared.api.utils.html.HtmlBuilderTargetSelector;
import com.polarion.alm.shared.api.utils.html.HtmlFragmentBuilder;
import com.polarion.alm.tracker.model.IAttachmentBase;
import com.polarion.alm.tracker.model.ICategory;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.spi.PObjectList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import static com.polarion.platform.persistence.model.IPObjectList.EMPTY_POBJECTLIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CucumberIntegrationFormExtensionTest {
    @Mock
    CucumberIntegrationFormExtension extension;

    @Mock
    private IWorkItem iWorkItem;

    @Mock
    private SharedContext context;
    @Mock
    private HtmlBuilderTargetSelector<HtmlFragmentBuilder> builder;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HtmlFragmentBuilder htmlFragmentBuilder;

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
                Arguments.of(object, new PObjectList(null, Arrays.asList(attachment)), "test"),
                Arguments.of(object, new PObjectList(null, Arrays.asList(attachment, attachment2)), "test"),
                Arguments.of(object, new PObjectList(null, Arrays.asList(attachment, attachment2, attachment3)), "test"),
                Arguments.of(object, new PObjectList(null, Arrays.asList(attachment, attachment4)), "test4")
        );
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
        when(extension.renderIntegrationTest(context, object, true)).thenCallRealMethod();

        when(context.createHtmlFragmentBuilderFor()).thenReturn(builder);
        when(builder.gwt()).thenReturn(htmlFragmentBuilder);

        String builderText = "text";
        when(htmlFragmentBuilder.toString()).thenReturn(builderText);
        when(htmlFragmentBuilder.tag().div().append().tag().b().append().text(anyString())).thenReturn(htmlFragmentBuilder);

        when(object.isPersisted()).thenReturn(isPersisted);

        assertThat(extension.renderIntegrationTest(context, object, true)).isEqualTo(builderText);

        verify(extension, times(0)).addSource(eq(htmlFragmentBuilder), eq("text/javascript"), anyString());
        verify(htmlFragmentBuilder, times(0)).html(anyString());
        verify(htmlFragmentBuilder, times(1)).finished();
    }

    @Test
    void renderIntegrationTestWithProjectAsWorkItem() throws IOException {
        var object = mock(IWorkItem.class);

        when(extension.renderIntegrationTest(context, object, true)).thenCallRealMethod();

        when(extension.getContent(object, EMPTY_POBJECTLIST)).thenCallRealMethod();

        when(context.createHtmlFragmentBuilderFor()).thenReturn(builder);
        when(builder.gwt()).thenReturn(htmlFragmentBuilder);

        String builderText = "text";
        when(htmlFragmentBuilder.toString()).thenReturn(builderText);

        when(object.isPersisted()).thenReturn(true);
        when(object.getAttachments()).thenReturn(EMPTY_POBJECTLIST);

        assertThat(extension.renderIntegrationTest(context, object, true)).isEqualTo(builderText);

        verify(extension, times(1)).addSource(eq(htmlFragmentBuilder), eq("text/javascript"), anyString());
        verify(extension, times(1)).addSource(eq(htmlFragmentBuilder), eq("module"), anyString());
        verify(htmlFragmentBuilder, times(5)).html(anyString());
        verify(htmlFragmentBuilder, times(1)).finished();
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
}
