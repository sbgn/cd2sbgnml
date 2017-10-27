//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.10.27 at 05:19:15 PM CEST 
//


package org.sbml._2001.ns.celldesigner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Link between operators, residues, binding sites, effect sites. and degradation.
 * 
 * <p>Java class for internalLinkInBlockDiagram complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="internalLinkInBlockDiagram">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="startingPointInBlockDiagram" type="{http://www.sbml.org/2001/ns/celldesigner}startingPointInBlockDiagram"/>
 *         &lt;element name="endPointInBlockDiagram" type="{http://www.sbml.org/2001/ns/celldesigner}endPointInBlockDiagram"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}short">
 *             &lt;minInclusive value="0"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="type" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="activation"/>
 *             &lt;enumeration value="absoluteActivation"/>
 *             &lt;enumeration value="inhibition"/>
 *             &lt;enumeration value="absoluteInhibition"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "internalLinkInBlockDiagram", propOrder = {
    "startingPointInBlockDiagram",
    "endPointInBlockDiagram"
})
public class InternalLinkInBlockDiagram {

    @XmlElement(required = true)
    protected StartingPointInBlockDiagram startingPointInBlockDiagram;
    @XmlElement(required = true)
    protected EndPointInBlockDiagram endPointInBlockDiagram;
    @XmlAttribute(name = "id", required = true)
    protected short id;
    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * Gets the value of the startingPointInBlockDiagram property.
     * 
     * @return
     *     possible object is
     *     {@link StartingPointInBlockDiagram }
     *     
     */
    public StartingPointInBlockDiagram getStartingPointInBlockDiagram() {
        return startingPointInBlockDiagram;
    }

    /**
     * Sets the value of the startingPointInBlockDiagram property.
     * 
     * @param value
     *     allowed object is
     *     {@link StartingPointInBlockDiagram }
     *     
     */
    public void setStartingPointInBlockDiagram(StartingPointInBlockDiagram value) {
        this.startingPointInBlockDiagram = value;
    }

    /**
     * Gets the value of the endPointInBlockDiagram property.
     * 
     * @return
     *     possible object is
     *     {@link EndPointInBlockDiagram }
     *     
     */
    public EndPointInBlockDiagram getEndPointInBlockDiagram() {
        return endPointInBlockDiagram;
    }

    /**
     * Sets the value of the endPointInBlockDiagram property.
     * 
     * @param value
     *     allowed object is
     *     {@link EndPointInBlockDiagram }
     *     
     */
    public void setEndPointInBlockDiagram(EndPointInBlockDiagram value) {
        this.endPointInBlockDiagram = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public short getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(short value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
