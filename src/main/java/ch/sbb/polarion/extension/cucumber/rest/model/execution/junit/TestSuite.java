package ch.sbb.polarion.extension.cucumber.rest.model.execution.junit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "testsuite")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestSuite {

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<Property> properties;

    @XmlElement(name = "testcase")
    private List<TestCase> testCases;

    @XmlElement(name = "system-out")
    private String systemOut;

    @XmlElement(name = "system-err")
    private String systemErr;

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute(required = true)
    private int tests;

    @XmlAttribute
    private int failures;

    @XmlAttribute
    private int errors;

    @XmlAttribute
    private double time;

    @XmlAttribute
    private boolean disabled;

    @XmlAttribute
    private int skipped;

    @XmlAttribute
    private String timestamp;

    @XmlAttribute
    private String hostname;

    @XmlAttribute
    private String id;

    @XmlAttribute(name = "package")
    private String pkg;
}
