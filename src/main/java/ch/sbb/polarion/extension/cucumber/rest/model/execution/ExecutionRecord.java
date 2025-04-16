package ch.sbb.polarion.extension.cucumber.rest.model.execution;

import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.CucumberExecutionResult;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.Element;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.Tag;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.junit.TestCase;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.junit.TestSuite;
import com.polarion.core.util.StringUtils;
import com.polarion.core.util.types.Text;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.sbb.polarion.extension.cucumber.helper.PolarionTestRunStatus.FAILED;
import static ch.sbb.polarion.extension.cucumber.helper.PolarionTestRunStatus.PASSED;

@Getter
public class ExecutionRecord {

    public static final int NANOSECONDS_IN_SECOND = 1000000000;
    private final List<String> testCaseIds = new ArrayList<>();
    private Date date;
    private Float duration;
    private String status;
    private Text comment;
    private String testCaseTitle;

    public static List<ExecutionRecord> fromJUnitReport(TestSuite testSuite) {
        List<ExecutionRecord> resultList = new ArrayList<>();

        for (TestCase testCase : testSuite.getTestCases()) {
            ExecutionRecord executionRecord = new ExecutionRecord();
            executionRecord.duration = testCase.getTime() != null ? testCase.getTime().floatValue() : null;
            executionRecord.date = new Date(); // using current date (probably must be filled from the source testCase/testSuite?)
            executionRecord.status = getJUnitRecordStatus(testCase);
            executionRecord.testCaseTitle = testCase.getName();
            resultList.add(executionRecord);
        }

        return resultList;
    }

    public static List<ExecutionRecord> fromCucumberExecutions(List<CucumberExecutionResult> results) {
        List<ExecutionRecord> resultList = new ArrayList<>();
        for (CucumberExecutionResult result : results) {
            Iterator<Element> iter = result.getElements().iterator();

            Element elem;
            Element backgroundElement = null;
            Element scenarioElement;
            while (iter.hasNext()) {
                elem = iter.next();
                backgroundElement =
                        backgroundElement == null ? getElementByType("background", elem) : backgroundElement;

                if ("background".equals(elem.getType()) && backgroundElement != null) {
                    continue;
                }

                scenarioElement = getElementByType("scenario", elem);

                if (scenarioElement != null) {
                    resultList.add(getExecutionRecord(backgroundElement, scenarioElement));
                }

                backgroundElement = null;
            }
        }
        return resultList;
    }

    @NotNull
    private static ExecutionRecord getExecutionRecord(Element backgroundElement, Element scenarioElement) {
        ExecutionRecord executionRecord = new ExecutionRecord();
        executionRecord.duration = getTestRecordDuration(backgroundElement, scenarioElement);
        executionRecord.date = scenarioElement.getStartTimestamp() != null ? scenarioElement.getStartTimestamp() : new Date();
        executionRecord.status = getTestRecordStatus(backgroundElement, scenarioElement);
        executionRecord.comment = getTestRecordComment(backgroundElement, scenarioElement);

        if (scenarioElement.getTags() != null) {
            executionRecord.testCaseIds.addAll(
                    scenarioElement.getTags().stream()
                            .map(ExecutionRecord::getTagName)
                            .filter(Objects::nonNull)
                            .filter(tag -> tag.matches("^[a-zA-Z]*-\\d*$"))
                            .toList()
            );
        }
        return executionRecord;
    }

    private static Element getElementByType(String expectedElementType, Element element) {
        return expectedElementType.equals(element.getType()) ? element : null;
    }

    private static @Nullable Float getTestRecordDuration(@Nullable Element backgroundElement, @NotNull Element scenarioElement) {
        Float duration = null;
        if (backgroundElement != null && backgroundElement.getSteps() != null && !backgroundElement.getSteps().isEmpty()) {
            duration = getDuration(backgroundElement);
        }
        if (scenarioElement.getSteps() != null && !scenarioElement.getSteps().isEmpty()) {
            float scenarioDuration = getDuration(scenarioElement);
            duration = duration == null ? scenarioDuration : duration + scenarioDuration;
        }
        return duration;
    }

    private static float getDuration(@NotNull Element scenarioElement) {
        return scenarioElement.getSteps().stream()
                .map(step -> step.getResult().getDuration())
                .reduce((long) 0, Long::sum).floatValue() / NANOSECONDS_IN_SECOND;
    }

    private static @Nullable String getTestRecordStatus(@Nullable Element backgroundElement, @NotNull Element scenarioElement) {
        String backgroundStatus = null;
        if (backgroundElement != null && backgroundElement.getSteps() != null && !backgroundElement.getSteps().isEmpty()) {
            backgroundStatus = getStatus(backgroundElement);
        }

        String scenarioStatus = null;
        if (scenarioElement.getSteps() != null && !scenarioElement.getSteps().isEmpty()) {
            scenarioStatus = getStatus(scenarioElement);
        }

        if (backgroundStatus == null) {
            return scenarioStatus;
        } else {
            return scenarioStatus == null ? backgroundStatus : getCombinedStatus(backgroundStatus, scenarioStatus);
        }
    }

    @NotNull
    private static String getCombinedStatus(String backgroundStatus, String scenarioStatus) {
        return PASSED.getId().equals(backgroundStatus) && PASSED.getId().equals(scenarioStatus) ? PASSED.getId() : FAILED.getId();
    }

    @NotNull
    private static String getStatus(@NotNull Element backgroundElement) {
        return backgroundElement.getSteps().stream()
                .map(step -> step.getResult().getStatus())
                .reduce(PASSED.getId(), (a, b) -> a.equals(PASSED.getId()) && b.equals(PASSED.getId()) ? PASSED.getId() : FAILED.getId());
    }

    private static @Nullable Text getTestRecordComment(@Nullable Element backgroundElement, @NotNull Element scenarioElement) {
        return Stream.concat(getErrorMessages(backgroundElement).stream(), getErrorMessages(scenarioElement).stream())
                .filter(errorMessage -> !StringUtils.isEmptyTrimmed(errorMessage))
                .collect(Collectors.collectingAndThen(
                        Collectors.joining(System.lineSeparator()),
                        result -> StringUtils.isEmpty(result) ? null : Text.plain(result)
                ));
    }

    private static List<String> getErrorMessages(@Nullable Element element) {
        return element == null || element.getSteps() == null ? new ArrayList<>() : element.getSteps().stream()
                .map(step -> step.getResult().getErrorMessage())
                .filter(errorMessage -> !StringUtils.isEmptyTrimmed(errorMessage))
                .toList();
    }

    private static @Nullable String getTagName(@NotNull Tag tag) {
        if (tag.getName() != null && tag.getName().startsWith("@")) {
            return tag.getName().length() > 1 ? tag.getName().substring(1) : null;
        }
        return tag.getName();
    }

    private static String getJUnitRecordStatus(TestCase testCase) {
        return testCase.getSkipped() == null &&
                (testCase.getFailures() == null || testCase.getFailures().isEmpty()) &&
                (testCase.getErrors() == null || testCase.getErrors().isEmpty()) ? "passed" : "failed";
    }
}
