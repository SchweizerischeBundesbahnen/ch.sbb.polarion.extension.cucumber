package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.subterra.base.data.model.ICustomField;
import com.polarion.subterra.base.data.model.IListType;
import com.polarion.subterra.base.data.model.IType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnumConverterTest {

    @Test
    void convertSingleValueExtractsValue() {
        ICustomField fieldPrototype = mock(ICustomField.class);
        when(fieldPrototype.getType()).thenReturn(mock(IType.class));

        Object result = new EnumConverter().convert(mock(ITrackerService.class), mock(ITestRun.class),
                fieldPrototype, Map.of("value", "high"));

        assertEquals("high", result);
    }

    @Test
    void convertListExtractsNames() {
        ICustomField fieldPrototype = mock(ICustomField.class);
        when(fieldPrototype.getType()).thenReturn(mock(IListType.class));

        Object result = new EnumConverter().convert(mock(ITrackerService.class), mock(ITestRun.class),
                fieldPrototype, List.of(Map.of("name", "comp1"), Map.of("name", "comp2")));

        assertEquals(List.of("comp1", "comp2"), result);
    }
}
