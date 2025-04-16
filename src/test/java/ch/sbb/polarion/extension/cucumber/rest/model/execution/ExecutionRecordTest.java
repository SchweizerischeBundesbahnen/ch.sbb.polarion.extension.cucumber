package ch.sbb.polarion.extension.cucumber.rest.model.execution;

import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.CucumberExecutionResult;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.junit.TestSuite;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarion.core.util.types.Text;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.sbb.polarion.extension.cucumber.helper.PolarionTestRunStatus.FAILED;
import static ch.sbb.polarion.extension.cucumber.helper.PolarionTestRunStatus.PASSED;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExecutionRecordTest {

    @Test
    void checkJunitRecordsCreation() {
        List<ExecutionRecord> goodRecords = ExecutionRecord.fromJUnitReport(loadJunitSuite("good.xml"));
        Assertions.assertThat(goodRecords).hasSize(3);
        assertThat(goodRecords.get(2).getDuration()).isEqualTo(3.333f);
        assertThat(goodRecords.stream().map(ExecutionRecord::getStatus))
                .containsOnly(PASSED.getId());
        assertThat(goodRecords.stream().map(ExecutionRecord::getTestCaseTitle))
                .containsExactly("testCase1", "testCase2", "testCase3");

        List<ExecutionRecord> someFailedRecords = ExecutionRecord.fromJUnitReport(loadJunitSuite("someFailed.xml"));
        assertThat(someFailedRecords.stream()
                .map(ExecutionRecord::getStatus)
                .toList())
                .isEqualTo(List.of(PASSED.getId(), FAILED.getId(), FAILED.getId(), FAILED.getId()));
    }

    @Test
    void checkCucumberRecordsCreation() {
        List<ExecutionRecord> goodRecords = ExecutionRecord.fromCucumberExecutions(loadCucumberExecutions("good.json"));
        Assertions.assertThat(goodRecords).hasSize(6);
        assertThat(goodRecords.get(0).getDuration()).isEqualTo(0.012877f);
        assertThat(goodRecords.stream().map(ExecutionRecord::getStatus))
                .containsOnly(PASSED.getId());
        assertThat(goodRecords.stream().flatMap(r -> r.getTestCaseIds().stream()))
                .containsExactly("EL-1", "EL-2", "EL-3", "EL-4", "EL-21", "EL-22");

        List<ExecutionRecord> someFailedRecords =
                ExecutionRecord.fromCucumberExecutions(loadCucumberExecutions("someFailed.json"));
        assertThat(someFailedRecords.get(0).getTestCaseIds()).isEmpty();
        assertThat(someFailedRecords.get(1).getTestCaseIds())
                .containsExactly("EL-222", "EL-333");
        assertThat(Stream.of(someFailedRecords.get(0), someFailedRecords.get(1))
                .map(ExecutionRecord::getStatus))
                .containsOnly(PASSED.getId());
        assertThat(Stream.of(someFailedRecords.get(2), someFailedRecords.get(3))
                .map(ExecutionRecord::getStatus))
                .containsOnly(FAILED.getId());
        assertThat(Stream.of(someFailedRecords.get(2), someFailedRecords.get(3))
                .map(ExecutionRecord::getComment))
                .containsOnly(Text.plain("single failed message"), Text.plain("failed message" + System.lineSeparator() + "passed message"));
    }

    @Test
    void checkFilteringTagsDuringCucumberRecordsCreation() {
        List<ExecutionRecord> taaRecords = ExecutionRecord.fromCucumberExecutions(loadCucumberExecutions("taa-small.json"));
        Assertions.assertThat(taaRecords).hasSize(11);
        for (ExecutionRecord taaRecord : taaRecords) {
            List<String> testCaseIds = taaRecord.getTestCaseIds();
            Assertions.assertThat(testCaseIds).hasSize(1);
            Assertions.assertThat(testCaseIds.get(0)).startsWith("TIADREQ-");
        }
    }

    private TestSuite loadJunitSuite(String fileName) {
        try (InputStream is = this.getClass().getResourceAsStream("/import/junit/" + fileName)) {
            JAXBContext jaxbContext = JAXBContext.newInstance(TestSuite.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (TestSuite) unmarshaller.unmarshal(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<CucumberExecutionResult> loadCucumberExecutions(String fileName) {
        try (InputStream is = this.getClass().getResourceAsStream("/import/cucumber/" + fileName)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(is,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CucumberExecutionResult.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
