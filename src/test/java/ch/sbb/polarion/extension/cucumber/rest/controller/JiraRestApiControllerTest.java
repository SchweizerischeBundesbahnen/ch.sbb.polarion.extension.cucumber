package ch.sbb.polarion.extension.cucumber.rest.controller;

import ch.sbb.polarion.extension.cucumber.rest.model.fields.FieldDefinition;
import ch.sbb.polarion.extension.generic.fields.model.FieldMetadata;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.projects.model.IProject;
import com.polarion.alm.shared.api.transaction.ReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.RunnableInReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.core.util.types.Text;
import com.polarion.subterra.base.data.identification.IContextId;
import com.polarion.subterra.base.data.model.internal.PrimitiveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.BadRequestException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class JiraRestApiControllerTest {

    @Test
    void testExceptionOnEmptyProject() {
        try (MockedStatic<TransactionalExecutor> mockedExecutor = mockStatic(TransactionalExecutor.class)) {

            PolarionService polarionService = mockCommonThings(mockedExecutor);
            JiraRestApiController controller = new JiraRestApiController(polarionService);
            BadRequestException thrown = assertThrows(BadRequestException.class, () ->
                    controller.field(null)
            );
            assertEquals("No proper projectId header found!", thrown.getMessage());
            thrown = assertThrows(BadRequestException.class, () ->
                    controller.field("   ")
            );
            assertEquals("No proper projectId header found!", thrown.getMessage());
        }
    }

    @Test
    void testField() {
        try (MockedStatic<TransactionalExecutor> mockedExecutor = mockStatic(TransactionalExecutor.class)) {

            PolarionService polarionService = mockCommonThings(mockedExecutor);
            IProject project = mock(IProject.class);
            when(project.getContextId()).thenReturn(mock(IContextId.class));
            when(polarionService.getProject(anyString())).thenReturn(project);
            when(polarionService.getCustomFields(anyString(), any(IContextId.class), isNull())).thenReturn(Set.of(
                    new FieldMetadata("id1", "label1", new PrimitiveType(Text.class.getName(), null), true, true, false, false, Set.of()),
                    new FieldMetadata("id2", "label2", new PrimitiveType(Text.class.getName(), null), true, true, false, false, Set.of())
            ));
            List<FieldDefinition> fields = new JiraRestApiController(polarionService).field("projId");
            assertEquals(4, fields.size());
            assertTrue(fields.stream().map(FieldDefinition::getId).toList().containsAll(List.of("id1", "id2", "title", "description")));
            assertTrue(fields.stream().map(FieldDefinition::getName).toList().containsAll(List.of("label1", "label2", "summary", "description")));
        }
    }

    private PolarionService mockCommonThings(MockedStatic<TransactionalExecutor> mockedExecutor) {
        mockedExecutor.when(() -> TransactionalExecutor.executeInReadOnlyTransaction(any(RunnableInReadOnlyTransaction.class)))
                .thenAnswer(invocation -> {
                    RunnableInReadOnlyTransaction transaction = invocation.getArgument(0);
                    return transaction.run(mock(ReadOnlyTransaction.class));
                });

        PolarionService polarionService = mock(PolarionService.class);
        lenient().when(polarionService.callPrivileged(any(Callable.class))).thenAnswer(invocation -> {
            Callable callable = invocation.getArgument(0);
            return callable.call();
        });

        return polarionService;
    }

}
