//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package org.sbml._2001.ns.celldesigner;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for listOfSquares complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listOfSquares">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="layerCompartmentAlias" type="{http://www.sbml.org/2001/ns/celldesigner}layerCompartmentAlias" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listOfSquares", propOrder = {
    "layerCompartmentAlias"
})
public class ListOfSquares {

    @XmlElement(required = true)
    protected List<LayerCompartmentAlias> layerCompartmentAlias;

    /**
     * Gets the value of the layerCompartmentAlias property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the layerCompartmentAlias property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLayerCompartmentAlias().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LayerCompartmentAlias }
     * 
     * 
     */
    public List<LayerCompartmentAlias> getLayerCompartmentAlias() {
        if (layerCompartmentAlias == null) {
            layerCompartmentAlias = new ArrayList<LayerCompartmentAlias>();
        }
        return this.layerCompartmentAlias;
    }

}
