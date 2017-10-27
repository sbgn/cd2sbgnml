//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.10.27 at 10:08:13 AM CEST 
//


package org.sbml._2001.ns.celldesigner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * For species aliases of complex species.
 * 
 * <p>Java class for complexSpeciesAlias complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="complexSpeciesAlias">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.sbml.org/2001/ns/celldesigner}activity"/>
 *         &lt;element name="bounds" type="{http://www.sbml.org/2001/ns/celldesigner}bounds"/>
 *         &lt;element name="view" type="{http://www.sbml.org/2001/ns/celldesigner}view"/>
 *         &lt;element name="backupSize">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="w" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="h" type="{http://www.w3.org/2001/XMLSchema}double" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="backupView" type="{http://www.sbml.org/2001/ns/celldesigner}view"/>
 *         &lt;element name="structuralState" type="{http://www.sbml.org/2001/ns/celldesigner}structuralStateAngle" minOccurs="0"/>
 *         &lt;element name="usualView" type="{http://www.sbml.org/2001/ns/celldesigner}usualView"/>
 *         &lt;element name="briefView" type="{http://www.sbml.org/2001/ns/celldesigner}briefView"/>
 *         &lt;element name="listOfSpeciesTag" type="{http://www.sbml.org/2001/ns/celldesigner}listOfSpeciesTag" minOccurs="0"/>
 *         &lt;element name="info" type="{http://www.sbml.org/2001/ns/celldesigner}info"/>
 *       &lt;/sequence>
 *       &lt;attribute name="compartmentAlias">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.sbml.org/2001/ns/celldesigner}SId">
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="id" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.sbml.org/2001/ns/celldesigner}SId">
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="species" use="required" type="{http://www.sbml.org/2001/ns/celldesigner}SId" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "complexSpeciesAlias", propOrder = {
    "activity",
    "bounds",
    "view",
    "backupSize",
    "backupView",
    "structuralState",
    "usualView",
    "briefView",
    "listOfSpeciesTag",
    "info"
})
@XmlSeeAlso({
    org.sbml._2001.ns.celldesigner.ListOfComplexSpeciesAliases.ComplexSpeciesAlias.class
})
public class ComplexSpeciesAlias {

    @XmlElement(required = true)
    protected String activity;
    @XmlElement(required = true)
    protected Bounds bounds;
    @XmlElement(required = true)
    protected View view;
    @XmlElement(required = true)
    protected ComplexSpeciesAlias.BackupSize backupSize;
    @XmlElement(required = true)
    protected View backupView;
    protected StructuralStateAngle structuralState;
    @XmlElement(required = true)
    protected UsualView usualView;
    @XmlElement(required = true)
    protected BriefView briefView;
    protected ListOfSpeciesTag listOfSpeciesTag;
    @XmlElement(required = true)
    protected Info info;
    @XmlAttribute(name = "compartmentAlias")
    protected String compartmentAlias;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "species", required = true)
    protected String species;

    /**
     * Gets the value of the activity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivity() {
        return activity;
    }

    /**
     * Sets the value of the activity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivity(String value) {
        this.activity = value;
    }

    /**
     * Gets the value of the bounds property.
     * 
     * @return
     *     possible object is
     *     {@link Bounds }
     *     
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Sets the value of the bounds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Bounds }
     *     
     */
    public void setBounds(Bounds value) {
        this.bounds = value;
    }

    /**
     * Gets the value of the view property.
     * 
     * @return
     *     possible object is
     *     {@link View }
     *     
     */
    public View getView() {
        return view;
    }

    /**
     * Sets the value of the view property.
     * 
     * @param value
     *     allowed object is
     *     {@link View }
     *     
     */
    public void setView(View value) {
        this.view = value;
    }

    /**
     * Gets the value of the backupSize property.
     * 
     * @return
     *     possible object is
     *     {@link ComplexSpeciesAlias.BackupSize }
     *     
     */
    public ComplexSpeciesAlias.BackupSize getBackupSize() {
        return backupSize;
    }

    /**
     * Sets the value of the backupSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplexSpeciesAlias.BackupSize }
     *     
     */
    public void setBackupSize(ComplexSpeciesAlias.BackupSize value) {
        this.backupSize = value;
    }

    /**
     * Gets the value of the backupView property.
     * 
     * @return
     *     possible object is
     *     {@link View }
     *     
     */
    public View getBackupView() {
        return backupView;
    }

    /**
     * Sets the value of the backupView property.
     * 
     * @param value
     *     allowed object is
     *     {@link View }
     *     
     */
    public void setBackupView(View value) {
        this.backupView = value;
    }

    /**
     * Gets the value of the structuralState property.
     * 
     * @return
     *     possible object is
     *     {@link StructuralStateAngle }
     *     
     */
    public StructuralStateAngle getStructuralState() {
        return structuralState;
    }

    /**
     * Sets the value of the structuralState property.
     * 
     * @param value
     *     allowed object is
     *     {@link StructuralStateAngle }
     *     
     */
    public void setStructuralState(StructuralStateAngle value) {
        this.structuralState = value;
    }

    /**
     * Gets the value of the usualView property.
     * 
     * @return
     *     possible object is
     *     {@link UsualView }
     *     
     */
    public UsualView getUsualView() {
        return usualView;
    }

    /**
     * Sets the value of the usualView property.
     * 
     * @param value
     *     allowed object is
     *     {@link UsualView }
     *     
     */
    public void setUsualView(UsualView value) {
        this.usualView = value;
    }

    /**
     * Gets the value of the briefView property.
     * 
     * @return
     *     possible object is
     *     {@link BriefView }
     *     
     */
    public BriefView getBriefView() {
        return briefView;
    }

    /**
     * Sets the value of the briefView property.
     * 
     * @param value
     *     allowed object is
     *     {@link BriefView }
     *     
     */
    public void setBriefView(BriefView value) {
        this.briefView = value;
    }

    /**
     * Gets the value of the listOfSpeciesTag property.
     * 
     * @return
     *     possible object is
     *     {@link ListOfSpeciesTag }
     *     
     */
    public ListOfSpeciesTag getListOfSpeciesTag() {
        return listOfSpeciesTag;
    }

    /**
     * Sets the value of the listOfSpeciesTag property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOfSpeciesTag }
     *     
     */
    public void setListOfSpeciesTag(ListOfSpeciesTag value) {
        this.listOfSpeciesTag = value;
    }

    /**
     * Gets the value of the info property.
     * 
     * @return
     *     possible object is
     *     {@link Info }
     *     
     */
    public Info getInfo() {
        return info;
    }

    /**
     * Sets the value of the info property.
     * 
     * @param value
     *     allowed object is
     *     {@link Info }
     *     
     */
    public void setInfo(Info value) {
        this.info = value;
    }

    /**
     * Gets the value of the compartmentAlias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompartmentAlias() {
        return compartmentAlias;
    }

    /**
     * Sets the value of the compartmentAlias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompartmentAlias(String value) {
        this.compartmentAlias = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the species property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Sets the value of the species property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecies(String value) {
        this.species = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="w" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="h" type="{http://www.w3.org/2001/XMLSchema}double" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class BackupSize {

        @XmlAttribute(name = "w")
        protected Double w;
        @XmlAttribute(name = "h")
        protected Double h;

        /**
         * Gets the value of the w property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getW() {
            return w;
        }

        /**
         * Sets the value of the w property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setW(Double value) {
            this.w = value;
        }

        /**
         * Gets the value of the h property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getH() {
            return h;
        }

        /**
         * Sets the value of the h property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setH(Double value) {
            this.h = value;
        }

    }

}