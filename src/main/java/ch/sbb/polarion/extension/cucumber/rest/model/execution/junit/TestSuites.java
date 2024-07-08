package ch.sbb.polarion.extension.cucumber.rest.model.execution.junit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings("java:S1700")
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "testsuites")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestSuites {

    @XmlElement(name = "testsuite")
    private List<TestSuite> testSuites;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private double time;

    @XmlAttribute
    private int tests;

    @XmlAttribute
    private int failures;

    @XmlAttribute
    private boolean disabled;

    @XmlAttribute
    private int errors;
}
