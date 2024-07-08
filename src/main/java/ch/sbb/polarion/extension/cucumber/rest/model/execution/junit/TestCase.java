package ch.sbb.polarion.extension.cucumber.rest.model.execution.junit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class TestCase {

    @XmlElement
    private Skipped skipped;

    @XmlElement(name = "error")
    private List<Error> errors;

    @XmlElement(name = "failure")
    private List<Failure> failures;

    @XmlElement(name = "system-out")
    private String systemOut;

    @XmlElement(name = "system-err")
    private String systemErr;

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute
    private int assertions;

    @XmlAttribute
    private Double time;

    @XmlAttribute(name = "classname", required = true)
    private String className;

    @XmlAttribute
    private String status;
}
