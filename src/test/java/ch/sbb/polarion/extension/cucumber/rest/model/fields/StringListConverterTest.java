package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import com.polarion.alm.tracker.IPlanningManager;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.IPlan;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.core.util.types.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StringListConverterTest {

    @Test
    void testConvert() {
        ITrackerService trackerService = mock(ITrackerService.class);
        ITestRun testRun = mock(ITestRun.class);
        Object result = new StringListConverter().convert(trackerService, testRun, null, List.of());
        assertEquals(Text.html(""), result);

        when(testRun.getProjectId()).thenReturn("projId");
        IPlanningManager planningManager = mock(IPlanningManager.class);
        when(trackerService.getPlanningManager()).thenReturn(planningManager);
        when(planningManager.searchPlans(anyString(), anyString(), anyInt())).thenReturn(List.of());
        result = new StringListConverter().convert(trackerService, testRun, null, List.of("some"));
        assertEquals(Text.html("<span>some</span><span> </span>"), result);

        IPlan plan = mock(IPlan.class);
        when(plan.getId()).thenReturn("planId");
        when(plan.getName()).thenReturn("planName");
        when(planningManager.searchPlans(anyString(), anyString(), anyInt())).thenReturn(List.of(plan));
        result = new StringListConverter().convert(trackerService, testRun, null, List.of("some"));
        assertEquals(Text.html("<a href=\"/polarion/#/project/projId/plan?id=planId\" target=\"_blank\">planName</a><span> </span>"), result);
    }
}
