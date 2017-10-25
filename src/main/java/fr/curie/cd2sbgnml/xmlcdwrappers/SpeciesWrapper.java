package fr.curie.cd2sbgnml.xmlcdwrappers;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;
import org.sbgn.bindings.Glyph;
import org.sbml.x2001.ns.celldesigner.CelldesignerAntisenseRNADocument.CelldesignerAntisenseRNA;
import org.sbml.x2001.ns.celldesigner.CelldesignerClassDocument.CelldesignerClass;
import org.sbml.x2001.ns.celldesigner.CelldesignerComplexSpeciesAliasDocument.CelldesignerComplexSpeciesAlias;
import org.sbml.x2001.ns.celldesigner.CelldesignerComplexSpeciesDocument.CelldesignerComplexSpecies;
import org.sbml.x2001.ns.celldesigner.CelldesignerGeneDocument.CelldesignerGene;
import org.sbml.x2001.ns.celldesigner.CelldesignerModificationDocument.CelldesignerModification;
import org.sbml.x2001.ns.celldesigner.CelldesignerModificationResidueDocument.CelldesignerModificationResidue;
import org.sbml.x2001.ns.celldesigner.CelldesignerProteinDocument.CelldesignerProtein;
import org.sbml.x2001.ns.celldesigner.CelldesignerRNADocument.CelldesignerRNA;
import org.sbml.x2001.ns.celldesigner.CelldesignerRegionDocument;
import org.sbml.x2001.ns.celldesigner.CelldesignerRegionDocument.CelldesignerRegion;
import org.sbml.x2001.ns.celldesigner.CelldesignerSpeciesAliasDocument.CelldesignerSpeciesAlias;
import org.sbml.x2001.ns.celldesigner.CelldesignerSpeciesDocument.CelldesignerSpecies;
import org.sbml.x2001.ns.celldesigner.CelldesignerSpeciesIdentityDocument.CelldesignerSpeciesIdentity;
import org.sbml.x2001.ns.celldesigner.CelldesignerStateDocument.CelldesignerState;
import org.sbml.x2001.ns.celldesigner.SpeciesDocument.Species;
import org.sbml.x2001.ns.celldesigner.impl.CelldesignerRegionDocumentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * wraps species and includedSpecies as they have a lot in common
 */
public class SpeciesWrapper {

    private final Logger logger = LoggerFactory.getLogger(SpeciesWrapper.class);

    public enum ReferenceType {
        // protein types
        GENERIC, ION_CHANNEL, RECEPTOR, TRUNCATED,
        // for the 3 others, just 1 type possible
        GENE, RNA, ANTISENSE_RNA}

    private boolean isIncludedSpecies;
    private boolean isComplex;

    private String id;
    private String name;
    private String compartment;
    private String complex;
    private String cdClass;
    private int multimer;
    private String structuralState;
    private ReferenceType type;
    private Element notes;
    private Element referenceNotes;
    private Element annotations;

    private List<AliasWrapper> aliases;
    private List<ResidueWrapper> residues;

    public SpeciesWrapper(Species species, ModelWrapper modelW) {
        this.isIncludedSpecies = false;
        CelldesignerClass cdClassClass = species.getAnnotation().getCelldesignerSpeciesIdentity().getCelldesignerClass();
        this.cdClass = cdClassClass.getDomNode().getChildNodes().item(0).getNodeValue();
        this.id = species.getId();
        this.name = species.getName().getStringValue();
        this.compartment = species.getCompartment();
        this.complex = null;
        this.notes = Utils.getNotes(species);
        this.annotations = Utils.getRDFAnnotations(species.getAnnotation());
        System.out.println("annotation for species: "+this.id+" "+this.annotations);

        this.commonConstructor(species.getAnnotation().getCelldesignerSpeciesIdentity(), modelW);

    }

    public SpeciesWrapper(CelldesignerSpecies species, ModelWrapper modelW) {
        this.isIncludedSpecies = true;
        CelldesignerClass cdClassClass = species.getCelldesignerAnnotation().getCelldesignerSpeciesIdentity().getCelldesignerClass();
        this.cdClass = cdClassClass.getDomNode().getChildNodes().item(0).getNodeValue();
        this.id = species.getId();
        this.name = species.getName().getStringValue();
        CelldesignerComplexSpecies complexSpecies = species.getCelldesignerAnnotation().getCelldesignerComplexSpecies();
        this.complex = complexSpecies.getDomNode().getChildNodes().item(0).getNodeValue();
        this.compartment = null;
        this.notes = Utils.getNotes(species);
        this.annotations = Utils.getRDFAnnotations(species);

        this.commonConstructor(species.getCelldesignerAnnotation().getCelldesignerSpeciesIdentity(), modelW);
    }


    public SpeciesWrapper(Glyph sbgnGlyph) {
        this(sbgnGlyph, null);
    }

    public SpeciesWrapper(Glyph sbgnGlyph, Glyph parentGlyph) {
        this.id = "s"+sbgnGlyph.getId();

        if(sbgnGlyph.getLabel() != null) {
            this.name = sbgnGlyph.getLabel().getText();
        }
        else {
            this.name = this.id;
        }

        if(sbgnGlyph.getCompartmentRef() != null) {
            this.compartment = ((Glyph) sbgnGlyph.getCompartmentRef()).getId();
        }
        else {
            this.compartment = "default";
        }

        if(parentGlyph != null) {
            this.isIncludedSpecies = true;
            this.complex = "cs"+parentGlyph.getId();
        }
        else {
            this.isIncludedSpecies = false;
            this.complex = null;
        }



    }

    private void commonConstructor(CelldesignerSpeciesIdentity identity, ModelWrapper modelW) {
        this.multimer = 1; // default to 1 if nothing else found
        this.residues = new ArrayList<>();
        this.aliases = new ArrayList<>();
        this.isComplex = this.cdClass.equals("COMPLEX");


        if(this.isComplex) {
            logger.debug("Species has "+modelW.getComplexSpeciesAliasFor(this.id).size()+" complexSpeciesAliases");
            for(CelldesignerComplexSpeciesAlias complexAlias : modelW.getComplexSpeciesAliasFor(this.id)) {
                if (complexAlias == null) {
                    continue;
                }
                logger.debug("Parse complex alias: " + complexAlias.getId());
                this.aliases.add(new AliasWrapper(complexAlias, this));
            }
        }
        //else {
        /*
         * here we shouldn't have to check normal aliases after complex aliases.
         * normally complex species should only have complex aliases, and not additional normal aliases
         * this only happens in ACSN
         */
        if(modelW.getSpeciesAliasFor(this.id) != null) {
            if(this.isComplex) {
                logger.warn("Complex species: "+this.id+" shouldn't have non-complex aliases");
            }

            logger.debug("Species has "+modelW.getSpeciesAliasFor(this.id).size()+" speciesAliases");
            for(CelldesignerSpeciesAlias alias : modelW.getSpeciesAliasFor(this.id)) {
                if (alias == null) {
                    continue;
                }
                logger.debug("Parse alias: " + alias.getId());
                this.aliases.add(new AliasWrapper(alias, this));
            }
        }

        // get information from the species' reference
        List<CelldesignerModificationResidue> listOfReferenceModif;
        HashMap<String, ResidueWrapper> mapOfReferenceModif = new HashMap<>();
        if(identity.isSetCelldesignerProteinReference()) {
            String protId = identity.getCelldesignerProteinReference().getDomNode().getChildNodes().item(0).getNodeValue();
            CelldesignerProtein prot = modelW.getProtein(protId);

            if(prot.isSetCelldesignerListOfModificationResidues()) {
                // loop through reference residues
                listOfReferenceModif =
                        Arrays.asList(prot.getCelldesignerListOfModificationResidues().getCelldesignerModificationResidueArray());

                mapOfReferenceModif = new HashMap<>();
                for (CelldesignerModificationResidue modif : listOfReferenceModif) {
                    System.out.println("Residue found for "+prot.getId()+" resid "+modif.getId()+" angle "+modif.getAngle());
                    ResidueWrapper residueWrapper = new ResidueWrapper(modif.getId());
                    residueWrapper.angle = Float.parseFloat(modif.getAngle());
                    if(modif.isSetName()) {
                        residueWrapper.name = modif.getName().getStringValue();
                    }
                    mapOfReferenceModif.put(residueWrapper.id, residueWrapper);
                }
                System.out.println(mapOfReferenceModif.size()+" res for protein "+protId);
                logger.debug(mapOfReferenceModif.size()+" res for protein "+protId);
            }

            this.type = getTypeFromString(prot.getType().toString());

            // manage protein reference notes
            this.referenceNotes = Utils.getNotes(prot);
        }
        else if(identity.isSetCelldesignerRnaReference()) {
            String rnaId = identity.getCelldesignerRnaReference().getDomNode().getChildNodes().item(0).getNodeValue();
            CelldesignerRNA rna = modelW.getRNA(rnaId);
            this.type = getTypeFromString(rna.getType());

            if(rna.isSetCelldesignerListOfRegions()) {
                NodeList listofnodes = rna.getDomNode().getChildNodes();
                mapOfReferenceModif = mapOfRegion(listofnodes);
            }

            // manage reference notes
            this.referenceNotes = Utils.getNotes(rna);

        }
        else if(identity.isSetCelldesignerAntisensernaReference()) {
            String asrnaId = identity.getCelldesignerAntisensernaReference().getDomNode().getChildNodes().item(0).getNodeValue();
            CelldesignerAntisenseRNA asrna = modelW.getAntisenseRNA(asrnaId);
            this.type = getTypeFromString(asrna.getType());

            if(asrna.isSetCelldesignerListOfRegions()) {
                // loop through reference list of regions
                NodeList listofnodes = asrna.getDomNode().getChildNodes();
                mapOfReferenceModif = mapOfRegion(listofnodes);
            }

            // manage reference notes
            this.referenceNotes = Utils.getNotes(asrna);

        }
        else if(identity.isSetCelldesignerGeneReference()) {
            String geneId = identity.getCelldesignerGeneReference().getDomNode().getChildNodes().item(0).getNodeValue();
            CelldesignerGene gene = modelW.getGene(geneId);
            this.type = getTypeFromString(gene.getType());

            if(gene.isSetCelldesignerListOfRegions()) {
                // loop through reference list of regions
                NodeList listofnodes = gene.getDomNode().getChildNodes();
                mapOfReferenceModif = mapOfRegion(listofnodes);
                System.out.println("GENE MODIF COUNT: "+mapOfReferenceModif.size()+" "+listofnodes.getLength());

            }


            // manage reference notes
            this.referenceNotes = Utils.getNotes(gene);
        }


        if(identity.isSetCelldesignerState()) {
            CelldesignerState state = identity.getCelldesignerState();

            // parse multimer and infounit
            if(state.isSetCelldesignerHomodimer()) {
                this.multimer = Integer.parseInt(state.getCelldesignerHomodimer().
                        getDomNode().getChildNodes().item(0).getNodeValue());
            }

            if(state.isSetCelldesignerListOfStructuralStates()) {
                // assume that there is only 1 state per species
                this.structuralState = state.getCelldesignerListOfStructuralStates().
                        getCelldesignerStructuralStateArray(0).getStructuralState().getStringValue();
            }

            // parse state variable/residues
            if(state.isSetCelldesignerListOfModifications()) {

                // list and map this species' residues
                List<CelldesignerModification> listOfModif =
                        Arrays.asList(state.getCelldesignerListOfModifications().getCelldesignerModificationArray());


                // loop through the species' residues
                for (CelldesignerModification modif : listOfModif) {
                    System.out.println("adding state: "+modif.getState().getStringValue()+" for res "+modif.getResidue());
                    String residueId = modif.getResidue();
                    ResidueWrapper residueWrapper = mapOfReferenceModif.get(residueId);
                    /*
                    In ACSN, residueWrapper might produce null result, because some residue listed in the species
                    does not exist in the referenced protein ex: e_p47 doesn't list residue d_rs1, but its species
                    has a residue d_rs1.
                     */
                    if(residueWrapper != null) {
                        residueWrapper.state = modif.getState().getStringValue();
                    }
                    else {
                        logger.error("Residue "+residueId+" doesn't exist in referenced protein.");
                    }
                }

            }

        }
        // finally set this species' residue wrapper list
        this.residues.addAll(mapOfReferenceModif.values());
        System.out.println("final residue size for species "+this.getId()+" : "+this.residues.size());

    }

    /**
     * Getting a list of region is impossible because of broken Binom api, so we use raw NodeList
     * @param listOfNode NodeList of children of the RNA/gene/ASRNA! (not of the listOfRegions)
     * @return
     */
    private HashMap<String, ResidueWrapper> mapOfRegion(NodeList listOfNode) {
        HashMap<String, ResidueWrapper> mapOfReferenceModif = new HashMap<>();


        for(int i=0; i < listOfNode.getLength(); i++) {
            Node n = listOfNode.item(i);
            if(n.getNodeName().equals("celldesigner_listOfRegions")) {
                Node listOfRegions = n;
                for(int j=0; j < listOfRegions.getChildNodes().getLength(); j++) {
                    Node n2 = listOfRegions.getChildNodes().item(j);
                    if(n2.getNodeName().equals("celldesigner_region")) {
                        String type = n2.getAttributes().getNamedItem("type").getNodeValue();
                        String id = n2.getAttributes().getNamedItem("id").getNodeValue();
                        String pos = n2.getAttributes().getNamedItem("pos").getNodeValue();
                        if(type.equals("Modification Site")) {
                            ResidueWrapper residueWrapper = new ResidueWrapper(id);
                            residueWrapper.relativePos = Float.parseFloat(pos);
                            residueWrapper.useAngle = false;
                            if(n2.getAttributes().getNamedItem("name") != null) {
                                residueWrapper.name = n2.getAttributes().getNamedItem("name").getNodeValue();
                            }
                            mapOfReferenceModif.put(residueWrapper.id, residueWrapper);
                        }
                    }
                }
            }
        }

        return mapOfReferenceModif;
    }

    public boolean isIncludedSpecies() {
        return isIncludedSpecies;
    }

    public boolean isComplex() {
        return isComplex;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCompartment() {
        return compartment;
    }

    public String getComplex() {
        return complex;
    }

    public String getCdClass() {
        return cdClass;
    }

    public List<AliasWrapper> getAliases() {
        return aliases;
    }

    public int getMultimer() {
        return multimer;
    }

    public String getStructuralState() {
        return structuralState;
    }

    public List<ResidueWrapper> getResidues() {
        return residues;
    }

    public static ReferenceType getTypeFromString(String t){
        switch(t) {
            case "GENERIC": return ReferenceType.GENERIC;
            case "ION_CHANNEL": return ReferenceType.ION_CHANNEL;
            case "RECEPTOR": return ReferenceType.RECEPTOR;
            case "TRUNCATED": return ReferenceType.TRUNCATED;
            case "GENE": return ReferenceType.GENE;
            case "RNA": return ReferenceType.RNA;
            case "ANTISENSE_RNA": return ReferenceType.ANTISENSE_RNA;
            default: throw new IllegalArgumentException("Value: "+t+" is not a valid type. Should be one of" +
                    Arrays.toString(ReferenceType.values()));
        }
    }

    public ReferenceType getType() {
        return type;
    }


    public Element getNotes() {
        return notes;
    }

    public Element getReferenceNotes() {
        return referenceNotes;
    }

    public Element getAnnotations() {
        return annotations;
    }
}
