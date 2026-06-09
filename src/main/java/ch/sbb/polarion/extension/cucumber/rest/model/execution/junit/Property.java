package ch.sbb.polarion.extension.cucumber.rest.model.execution.junit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Property {

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute(required = true)
    private String value;
}
