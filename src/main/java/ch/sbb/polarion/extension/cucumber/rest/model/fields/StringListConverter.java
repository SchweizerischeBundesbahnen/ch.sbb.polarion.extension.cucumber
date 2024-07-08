package ch.sbb.polarion.extension.cucumber.rest.model.fields;

import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.IPlan;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.core.util.StringUtils;
import com.polarion.core.util.types.Text;
import com.polarion.subterra.base.data.model.ICustomField;

import java.util.ArrayList;
import java.util.List;

public class StringListConverter implements FieldValueConverter {

    private static final String INTERNAL_HREF = "<a href=\"/polarion/#/project/%s/%s?id=%s\" target=\"_blank\">%s</a>%s";
    private static final String PATH_PLAN = "plan";
    private static final String PATH_TEST_RUN = "testrun";
    private static final String TEST_RUN_LINKS = "testRunLinks";
    private static final String SPACE = "<span> </span>";

    @Override
    @SuppressWarnings("unchecked")
    public Object convert(ITrackerService trackerService, ITestRun testRun, ICustomField fieldPrototype, Object source) {
        List<String> resultList = new ArrayList<>();
        List<String> sourceList = (List<String>) source;

        String projectId = testRun.getProjectId();
        for (String entry : sourceList) {
            IPlan plan = trackerService.getPlanningManager().searchPlans(String.format("project.id:%s AND id:\"%s\"", projectId, entry), "id", 1).stream()
                    .findFirst().orElse(null);

            //try to create cross-links between Plan and Test Run
            if (plan != null) {
                resultList.add(String.format(INTERNAL_HREF, projectId, PATH_PLAN, plan.getId(), plan.getName(), SPACE));
                String testRunLink = String.format(INTERNAL_HREF, projectId, PATH_TEST_RUN, testRun.getId(), testRun.getId(), SPACE);

                Text text = !(plan.getCustomField(TEST_RUN_LINKS) instanceof Text textObject) ? null : textObject;
                if (text == null || !text.getContent().contains(testRunLink)) {
                    plan.setCustomField(TEST_RUN_LINKS,
                            Text.html((text == null ? "" : StringUtils.getEmptyIfNull(text.getContent())) + testRunLink));
                    plan.save();
                }
            } else {

                //otherwise place it just as a regular text
                resultList.add(String.format("<span>%s</span>%s", entry, SPACE));
            }
        }

        return Text.html(String.join("", resultList));
    }
}
