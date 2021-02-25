package fr.curie.cd2sbgnml;

import fr.curie.cd2sbgnml.graphics.CdShape;
import fr.curie.cd2sbgnml.graphics.GeometryUtils;
import fr.curie.cd2sbgnml.graphics.Link;
import fr.curie.cd2sbgnml.model.*;
import fr.curie.cd2sbgnml.model.Process;
import fr.curie.cd2sbgnml.xmlcdwrappers.*;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbgn.Language;
import org.sbgn.bindings.*;
import org.sbgn.bindings.Glyph.State;
import org.sbgn.bindings.Map;
import org.sbml._2001.ns.celldesigner.Bounds;
import org.sbml._2001.ns.celldesigner.CompartmentAlias;
import org.sbml._2001.ns.celldesigner.ModelDisplay;
import org.sbml.sbml.level2.version4.Compartment;
import org.sbml.sbml.level2.version4.Sbml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.stream.Collectors;

import static fr.curie.cd2sbgnml.model.ReactantModel.getSbgnClass;

public class CD2SBGNML extends GeneralConverter {

    final Logger logger = LoggerFactory.getLogger(CD2SBGNML.class);

    List<Glyph> glyphList;
    HashMap<String, Glyph> glyphMap;
    List<StyleInfo> styleInfoList;
    HashMap<String, Port> portMap;

    public Sbgn toSbgn(Sbml sbml) {
        Sbgn sbgn = new Sbgn();
        Map map = new Map();
        //map.setId("mapID"); don't put it to ensure 0.2 compatibility
        //sbgn.getMap().add(map);
        sbgn.setMap(map);
        map.setLanguage(Language.PD.toString());

        ModelWrapper modelW = ModelWrapper.create(sbml);

        // put model notes into map notes
        if(modelW.getModel().getNotes() != null) {
            map.setNotes(getSBGNNotes(Utils.getNotes(modelW.getModel().getNotes())));
        }
        // put model annotations into map annotations
        map.setExtension(getSBGNAnnotation(Utils.getRDFAnnotations(modelW.getModel().getAnnotation().getAny()), "mapID"));

        //System.exit(1);

        logger.debug("number of species "+modelW.getListOfSpecies().size());
        logger.debug("number of included species "+modelW.getListOfIncludedSpecies().size());
        logger.debug("number of compartments "+modelW.getListOfCompartments().size());
        logger.debug("compartment aliases count: "+modelW.getListOfCompartmentAliases().size());


        this.glyphList = new ArrayList<>();
        this.glyphMap = new HashMap<>();
        // store all StyleInfos to be aggregated and added later
        this.styleInfoList = new ArrayList<>();
        this.portMap = new HashMap<>();


        // compartment section
        for(Compartment compartment: modelW.getListOfCompartments()) {
            processCompartment(compartment, modelW, map);
        }

        // species section
        for(SpeciesWrapper speciesW: modelW.getListOfSpeciesWrapper()) {
            //SpeciesWrapper speciesW = new SpeciesWrapper(species, modelW);
            processSpecies(speciesW, modelW, map);
        }

        // reactions
        for(ReactionWrapper reactionW: modelW.getListOfReactionWrapper()) {
            //ReactionWrapper reactionW = modelW.getReactionWrapperFor(reaction.getId());
            GenericReactionModel genericReactionModel = ReactionModelFactory.create(reactionW);

            // PROCESS
            logger.debug(reactionW.getId()+" "+reactionW.getReactantList().size());

            String processId = null;
            if(reactionW.hasProcess()) {
                Process process = genericReactionModel.getProcess();
                Point2D processCoord = process.getGlyph().getCenter();

                Glyph processGlyph = new Glyph();
                processGlyph.setClazz(Process.getSbgnClass(genericReactionModel.getCdReactionType().toString()));
                processId = process.getId();
                processGlyph.setId(processId);

                // include process into correct compartment
                // only if base reactants and products are in the same compartment, else no decision is taken
                String processCompartmentId = null;
                boolean sameCompartmentForAllReactants = true;

                List<ReactantWrapper> combinedBaseWrapper = new ArrayList<>(reactionW.getBaseReactants());
                combinedBaseWrapper.addAll(reactionW.getBaseProducts());

                for(ReactantWrapper reactantW: combinedBaseWrapper) {
                    String reactantCompId = reactantW.getAliasW().getSpeciesW().getCompartment();
                    if(processCompartmentId == null) {
                        processCompartmentId = reactantCompId;
                    }
                    else if(!processCompartmentId.equals(reactantCompId)) {
                        sameCompartmentForAllReactants = false;
                        break;
                    }
                }

                logger.debug("Final process compartment is: "+processCompartmentId);

                if (sameCompartmentForAllReactants && processCompartmentId != null && !processCompartmentId.equals("default")) {
                    processGlyph.setCompartmentRef(this.glyphMap.get(processCompartmentId));
                }

                Bbox processBbox = new Bbox();
                processBbox.setX((float) processCoord.getX() - process.getSize() / 2);
                processBbox.setY((float) processCoord.getY() - process.getSize() / 2);
                processBbox.setH(process.getSize());
                processBbox.setW(process.getSize());
                processGlyph.setBbox(processBbox);

                // put reaction into process glyph
                // TODO if no process, add notes into the arc
                processGlyph.setNotes(getSBGNNotes(reactionW.getNotes()));
                processGlyph.setExtension(getSBGNAnnotation(reactionW.getAnnotations(), processId));

                // TODO process style ?

                // ports
                Port p1 = new Port();
                String p1Id = processId+"_p1";
                p1.setId(p1Id);
                p1.setX((float) process.getPortIn().getX());
                p1.setY((float) process.getPortIn().getY());
                processGlyph.getPort().add(p1);
                portMap.put(p1Id, p1);

                Port p2 = new Port();
                String p2Id = processId+"_p2";
                p2.setId(p2Id);
                p2.setX((float) process.getPortOut().getX());
                p2.setY((float) process.getPortOut().getY());
                processGlyph.getPort().add(p2);
                portMap.put(p2Id, p2);

                processGlyph.setOrientation(process.getOrientation().name().toLowerCase());

                glyphList.add(processGlyph);
                glyphMap.put(processId, processGlyph);
                styleInfoList.add(genericReactionModel.getProcess().getStyleInfo());
                map.getGlyph().add(processGlyph);
            }

            // Possible logic gates
            for(ReactionNodeModel nodeModel: genericReactionModel.getReactionNodeModels()) {
                if(nodeModel instanceof LogicGate) {
                    LogicGate logicGate = (LogicGate) nodeModel;

                    Point2D logicCoord = logicGate.getGlyph().getCenter();

                    Glyph logicGlyph = new Glyph();
                    logicGlyph.setClazz(LogicGate.getSbgnClass(logicGate.getType()));

                    String logicId = logicGate.getId();
                    logicGlyph.setId(logicId);

                    Bbox logicBbox = new Bbox();
                    logicBbox.setX((float) logicCoord.getX() - logicGate.getSize() / 2);
                    logicBbox.setY((float) logicCoord.getY() - logicGate.getSize() / 2);
                    logicBbox.setH(logicGate.getSize());
                    logicBbox.setW(logicGate.getSize());
                    logicGlyph.setBbox(logicBbox);

                    // assign compartment
                    // find glyphs associated to this logic gate
                    List<Glyph> connectedGLyphs = new ArrayList<>();
                    for(LinkModel lm: genericReactionModel.getLinkModels()) {
                        if(lm.getEnd().getId().equals(logicGate.getId())) {
                            String modifierId = lm.getStart().getId();
                            connectedGLyphs.add(glyphMap.get(modifierId));
                        }
                        else if(lm.getStart().getId().equals(logicGate.getId())) {
                            String modifierId = lm.getEnd().getId();
                            connectedGLyphs.add(glyphMap.get(modifierId));
                        }
                    }

                    String logicCompartmentId = null;
                    boolean sameCompartmentForAllReactants = true;
                    Glyph compartmentGlyph = null;

                    for(Glyph g: connectedGLyphs) {
                        String glyphCompartmentId;
                        if(g.getCompartmentRef() == null) { // glyph is outside, no compartment defined
                            glyphCompartmentId = "default";
                        }
                        else {
                            glyphCompartmentId = ((Glyph) g.getCompartmentRef()).getId();
                        }

                        if(logicCompartmentId == null) {
                            logicCompartmentId = glyphCompartmentId;
                            compartmentGlyph = (Glyph) g.getCompartmentRef();
                        }
                        else if(!logicCompartmentId.equals(glyphCompartmentId)) {
                            sameCompartmentForAllReactants = false;
                            break;
                        }
                    }

                    logger.debug("Final logic compartment is: "+logicCompartmentId);
                    if (sameCompartmentForAllReactants && !logicCompartmentId.equals("default")) {
                        logicGlyph.setCompartmentRef(compartmentGlyph);
                    }


                    // ports
                    Port p1 = new Port();
                    String p1Id = logicId+"_p1";
                    p1.setId(p1Id);
                    p1.setX((float) logicGate.getPortIn().getX());
                    p1.setY((float) logicGate.getPortIn().getY());
                    logicGlyph.getPort().add(p1);
                    portMap.put(p1Id, p1);

                    Port p2 = new Port();
                    String p2Id = logicId+"_p2";
                    p2.setId(p2Id);
                    p2.setX((float) logicGate.getPortOut().getX());
                    p2.setY((float) logicGate.getPortOut().getY());
                    logicGlyph.getPort().add(p2);
                    portMap.put(p2Id, p2);

                    logicGlyph.setOrientation(logicGate.getOrientation().name().toLowerCase());

                    glyphList.add(logicGlyph);
                    glyphMap.put(logicId, logicGlyph);
                    styleInfoList.add(logicGate.getStyleInfo());
                    map.getGlyph().add(logicGlyph);

                }
            }

            for(LinkModel ln: genericReactionModel.getLinkModels()) {
                styleInfoList.add(ln.getStyleInfo());
                map.getArc().add(getArc(ln));
            }
        }

        // text notes on the map
        for(TextWrapper textW: modelW.getListofTextWrapper()) {
            if(!textW.isVisible()) {
                continue;
            }

            Glyph textGlyph = new Glyph();

            Bbox textBbox = new Bbox();
            textBbox.setX((float) textW.getBbox().getX());
            textBbox.setY((float) textW.getBbox().getY());
            textBbox.setW((float) textW.getBbox().getWidth());
            textBbox.setH((float) textW.getBbox().getHeight());
            textGlyph.setBbox(textBbox);

            Label textLabel = new Label();
            textLabel.setText(textW.getText());
            textGlyph.setLabel(textLabel);

            textGlyph.setClazz("annotation");
            textGlyph.setId("text_"+UUID.randomUUID());

            // set reference point
            Glyph.Callout callout = new Glyph.Callout();
            Point calloutPoint = new Point();
            calloutPoint.setX((float) textW.getRefPoint().getX());
            calloutPoint.setY((float) textW.getRefPoint().getY());
            callout.setPoint(calloutPoint);
            textGlyph.setCallout(callout);

            map.getGlyph().add(textGlyph);
        }


        // finally process style info objects
        SBGNBase.Extension ext = new SBGNBase.Extension();
        ext.getAny().add(getAllStyles(styleInfoList, sbml));
        map.setExtension(ext);


        return sbgn;
    }

    public void processCompartment(Compartment compartment, ModelWrapper modelW, Map map) {
        if(! compartment.getId().equals("default")) {
            for(CompartmentAlias alias : modelW.getCompartmentAliasFor(compartment.getId())) {
                Bounds cdBounds = alias.getBounds();
                org.sbml._2001.ns.celldesigner.Point cdPoint = alias.getPoint();

                Glyph compGlyph = new Glyph();

                // basic info
                compGlyph.setClazz("compartment");
                String compartmentId = compartment.getId()+"_"+alias.getId();
                compGlyph.setId(compartmentId);
                if (!compartment.getOutside().equals("default")) {
                    compGlyph.setCompartmentRef(glyphMap.get(compartment.getOutside()));
                }

                // label
                Label compLabel = new Label();
                compLabel.setText(Utils.interpretToUTF8(compartment.getName()));

                // if a namepoint element is there, add a bbox to place the label correctly
                if(alias.getNamePoint() != null) {
                    Bbox labelBbox = new Bbox();
                    labelBbox.setX(alias.getNamePoint().getX().floatValue());
                    labelBbox.setY(alias.getNamePoint().getY().floatValue());
                    labelBbox.setH(10);
                    labelBbox.setW(GeometryUtils.getLengthForString(compartment.getName()));
                    compLabel.setBbox(labelBbox);
                }

                compGlyph.setLabel(compLabel);



                // bbox
                Bbox compBbox = new Bbox();
                if (cdBounds == null) {
                    // bounds may not be here, point instead
                    // assume that the compartment takes all the remaining map space
                    float pointX = cdPoint.getX().floatValue();
                    float pointY = cdPoint.getY().floatValue();
                    ModelDisplay display =
                            modelW.getModel().getAnnotation().getExtension().getModelDisplay();
                    float mapSizeX = display.getSizeX();
                    float mapSizeY = display.getSizeY();

                    Rectangle2D.Float bbox = GeometryUtils.getCompartmentBbox(
                            alias.getClazz(),
                            pointX, pointY,
                            alias.getDoubleLine().getThickness().floatValue(),
                            mapSizeX, mapSizeY);

                    compBbox.setX((float) bbox.getX());
                    compBbox.setY((float) bbox.getY());
                    compBbox.setW((float) bbox.getWidth());
                    compBbox.setH((float) bbox.getHeight());
                } else {
                    compBbox.setX(cdBounds.getX().floatValue());
                    compBbox.setY(cdBounds.getY().floatValue());
                    compBbox.setH(cdBounds.getH().floatValue());
                    compBbox.setW(cdBounds.getW().floatValue());
                }
                compGlyph.setBbox(compBbox);

                // keep notes
                compGlyph.setNotes(getSBGNNotes(Utils.getNotes(compartment.getNotes())));
                compGlyph.setExtension(getSBGNAnnotation(
                        Utils.getRDFAnnotations(compartment.getAnnotation().getAny()), compartmentId));

                // keep references
                glyphList.add(compGlyph);
                glyphMap.put(compartment.getId(), compGlyph);
                styleInfoList.add(new StyleInfo(alias, compartmentId));

                // add to output
                map.getGlyph().add(compGlyph);
            }
        }
    }

    public Glyph processSpeciesAlias(SpeciesWrapper species, AliasWrapper alias, ModelWrapper modelW, boolean isClone) {
        Glyph glyph = getGlyph(alias, isClone);

        if(species.getReferenceNotes() != null) {
            // TODO is piling up <html> elements in 1 note ok ?
            if(glyph.getNotes() != null) {
                glyph.getNotes().getAny().set(0, Utils.mergeHtmls(glyph.getNotes().getAny().get(0), species.getReferenceNotes()));
                logger.debug("MULTIPLE NOTES "+glyph.getNotes().getAny());
            }
            else {
                glyph.setNotes(getSBGNNotes(species.getReferenceNotes()));
            }
        }

        if(species.isComplex()) {
            if(modelW.getIncludedAliasWrapperFor(alias.getId()) == null) {
                // empty complex, should probably not happen
                //throw new IllegalStateException("empty complex for species "+species.getId()+" alias: "+alias.getId()+" name: "+species.getName());
                logger.warn("Empty complex for species "+species.getId()+" alias: "+alias.getId()+" name: "+species.getName());
            }
            else {
                for(AliasWrapper includedAlias: modelW.getIncludedAliasWrapperFor(alias.getId())) {
                    SpeciesWrapper includedSpecies = modelW.getSpeciesWrapperFor(includedAlias.getSpeciesId());
                    Glyph includedGlyph = processSpeciesAlias(includedSpecies, includedAlias, modelW, isClone);
                    glyph.getGlyph().add(includedGlyph);

                    /*
                    In ACSN, we need to keep references to included species also because some have links. Which
                    shouldn't happen.
                     */
                    glyphList.add(includedGlyph);
                    glyphMap.put(includedGlyph.getId(), includedGlyph);
                    styleInfoList.add(includedAlias.getStyleInfo());
                }
            }
        }

        return glyph;
    }

    public void processSpecies(SpeciesWrapper species, ModelWrapper modelW, Map map) {
        boolean isClone = false;
        if(species.getAliases().size() > 1) {
            isClone = true;
        }
        for(AliasWrapper alias : species.getAliases()) {
            // included species is already added inside its complex when complex is processed
            if(!species.isIncludedSpecies()) {
                Glyph glyph = processSpeciesAlias(species, alias, modelW, isClone);

                // keep references
                glyphList.add(glyph);
                glyphMap.put(glyph.getId(), glyph);
                styleInfoList.add(alias.getStyleInfo());
                // add to map
                map.getGlyph().add(glyph);
            }
        }
    }

    public Glyph getGlyph(AliasWrapper aliasW, boolean isClone) {
        String id = aliasW.getSpeciesW().getId()+"_"+aliasW.getId();
        SpeciesWrapper species = aliasW.getSpeciesW();

        Rectangle2D.Float bboxRect = (Rectangle2D.Float) aliasW.getBounds();

        Glyph glyph = new Glyph();

        // basic info
        glyph.setId(id);
        if (! species.isIncludedSpecies() && !species.getCompartment().equals("default")) {
            glyph.setCompartmentRef(this.glyphMap.get(species.getCompartment()));
        }

        // label
        Label label = new Label();
        label.setText(Utils.interpretToUTF8(species.getName()));
        glyph.setLabel(label);

        // is clone or not
        if(isClone) {
            glyph.setClone(new Glyph.Clone());
        }

        // bbox
        Bbox bbox = new Bbox();
        bbox.setX((float) bboxRect.getX());
        bbox.setY((float) bboxRect.getY());
        bbox.setH((float) bboxRect.getHeight());
        bbox.setW((float) bboxRect.getWidth());
        glyph.setBbox(bbox);

        // structural state
        if(species.getStructuralState() != null) {

            Glyph statevar = getStateVariable("", species.getStructuralState(), bboxRect, 90);
            glyph.getGlyph().add(statevar);

        }

        // alias info unit
        if(aliasW.getInfo() != null) {

            Glyph unitOfInfo = getUnitOfInfo(aliasW.getInfo().getSbgnText(),
                    bboxRect,
                    // clockwork here !!!
                    - GeometryUtils.unsignedRadianToSignedDegree(aliasW.getInfo().angle));
            glyph.getGlyph().add(unitOfInfo);

        }

        // multimerize
        String sbgnClass = getSbgnClass(species.getCdClass());
        if(species.getMultimer() > 1) {
            sbgnClass += " multimer";

            // add another unit of info only if info with prefix N isn't there (from Aliasinfo),
            // else adding another unit of info would be redundant
            if(!(aliasW.getInfo() != null && aliasW.getInfo().prefix.equals("N"))) {

                float angle = 90;
                if (species.getStructuralState() != null) {
                    // if a unit of info is there already, top position is taken.
                    // put multimer info on bottom
                    angle = -90;
                }

                Glyph unitOfInfoMultimer = getUnitOfInfo("N:" + species.getMultimer(),
                        bboxRect,
                        angle);
                glyph.getGlyph().add(unitOfInfoMultimer);
            }

        }

        // state variables
        for(ResidueWrapper residueW: species.getResidues()) {

            Glyph residue = getStateVariableFromResidueWrapper(residueW, bboxRect);

            glyph.getGlyph().add(residue);
        }

        // add additional units of info depending on the situation
        // eg for receptors, ion channels, truncated, genes...
        if(species.getType() == SpeciesWrapper.ReferenceType.RECEPTOR) {
            Glyph receptorUnitOfInfo = getUnitOfInfo("receptor", bboxRect, 90);
            glyph.getGlyph().add(receptorUnitOfInfo);
        }
        else if(species.getType() == SpeciesWrapper.ReferenceType.ION_CHANNEL) {
            Glyph receptorUnitOfInfo = getUnitOfInfo("ion channel", bboxRect, 90);
            glyph.getGlyph().add(receptorUnitOfInfo);

            Glyph activeStateVar;
            if(aliasW.isActive()) {
                activeStateVar = getStateVariable("", "open", bboxRect, -90);
            }
            else {
                activeStateVar = getStateVariable("", "closed", bboxRect, -90);
            }
            glyph.getGlyph().add(activeStateVar);
        }
        else if(species.getType() == SpeciesWrapper.ReferenceType.TRUNCATED) {
            Glyph receptorUnitOfInfo = getUnitOfInfo("truncated", bboxRect, 90);
            glyph.getGlyph().add(receptorUnitOfInfo);
        }
        else if(species.getType() == SpeciesWrapper.ReferenceType.RNA) {
            Glyph rnaUnitOfInfo = getUnitOfInfo("RNA", bboxRect, 90);
            glyph.getGlyph().add(rnaUnitOfInfo);
        }
        else if(species.getType() == SpeciesWrapper.ReferenceType.ANTISENSE_RNA) {
            Glyph rnaUnitOfInfo = getUnitOfInfo("asRNA", bboxRect, 90);
            glyph.getGlyph().add(rnaUnitOfInfo);
        }
        /*else if(species.getCdClass().equals("ION")) {
            Glyph ionUnitOfInfo = getUnitOfInfo("ion", bboxRect, 90);
            glyph.getGlyph().add(ionUnitOfInfo);
        }*/
        else if(species.getCdClass().equals("DRUG")) {
            Glyph drugUnitOfInfo = getUnitOfInfo("drug", bboxRect, 90);
            glyph.getGlyph().add(drugUnitOfInfo);
        }

        glyph.setNotes(getSBGNNotes(species.getNotes()));
        glyph.setExtension(getSBGNAnnotation(species.getAnnotations(), id));


        // class
        glyph.setClazz(sbgnClass);


        return glyph;
    }

    public Glyph getStateVariable(String prefix, String value, Rectangle2D.Float parentBbox, float angle) {

        Glyph unitOfInfo = new Glyph();

        State state = new State();
        state.setValue(value);
        state.setVariable(prefix);
        unitOfInfo.setState(state);

        Rectangle2D.Float infoRect = GeometryUtils.getAuxUnitBboxFromAngle(parentBbox, prefix+":"+value, angle);
        Bbox infoBbox = new Bbox();
        infoBbox.setX((float) infoRect.getX());
        infoBbox.setY((float) infoRect.getY());
        infoBbox.setW((float) infoRect.getWidth());
        infoBbox.setH((float) infoRect.getHeight());
        unitOfInfo.setBbox(infoBbox);

        unitOfInfo.setClazz("state variable");
        unitOfInfo.setId("_" + UUID.randomUUID());

        return unitOfInfo;
    }

    public Glyph getStateVariableFromResidueWrapper(ResidueWrapper residueW, Rectangle2D.Float parentBbox) {

        Glyph unitOfInfo = new Glyph();

        String prefix = residueW.name;
        String value = ResidueWrapper.getShortState(residueW.state);
        State state = new State();
        state.setValue(value);
        state.setVariable(prefix);
        unitOfInfo.setState(state);

        Rectangle2D.Float infoRect;
        if(residueW.useAngle) {
            infoRect = GeometryUtils.getAuxUnitBboxFromAngle(parentBbox, prefix+":"+value,
                    GeometryUtils.unsignedRadianToSignedDegree(residueW.angle));
        }
        else {
            infoRect = GeometryUtils.getAuxUnitBboxFromRelativeTopRatio(parentBbox, prefix+":"+value, residueW.relativePos);
        }

        Bbox infoBbox = new Bbox();
        infoBbox.setX((float) infoRect.getX());
        infoBbox.setY((float) infoRect.getY());
        infoBbox.setW((float) infoRect.getWidth());
        infoBbox.setH((float) infoRect.getHeight());
        unitOfInfo.setBbox(infoBbox);

        unitOfInfo.setClazz("state variable");
        unitOfInfo.setId("_" + UUID.randomUUID());

        return unitOfInfo;
    }

    public Glyph getUnitOfInfo(String text, Rectangle2D.Float parentBbox, float angle) {

        Glyph unitOfInfo = new Glyph();
        Label infoLabel = new Label();
        infoLabel.setText(Utils.interpretToUTF8(text));
        unitOfInfo.setLabel(infoLabel);

        Rectangle2D.Float infoRect = GeometryUtils.getAuxUnitBboxFromAngle(parentBbox, text, angle);
        Bbox infoBbox = new Bbox();
        infoBbox.setX((float) infoRect.getX());
        infoBbox.setY((float) infoRect.getY());
        infoBbox.setW((float) infoRect.getWidth());
        infoBbox.setH((float) infoRect.getHeight());
        unitOfInfo.setBbox(infoBbox);

        unitOfInfo.setClazz("unit of information");
        unitOfInfo.setId("_" + UUID.randomUUID());

        return unitOfInfo;
    }

    public Arc getArc(LinkModel linkM) {

        /*
        In ACSN, some subunits of a complex have connections. They are not added to the global glyphMap index, so they
        cannot be referenced here.
         */
        if(!this.glyphMap.containsKey(linkM.getStart().getId())) {
            logger.error("No source for link: "+linkM.getId()+" missing glyph "+linkM.getStart().getId());
        }
        if(!this.glyphMap.containsKey(linkM.getEnd().getId())) {
            logger.error("No target for link: "+linkM.getId()+" missing glyph "+linkM.getEnd().getId());
        }

        GenericReactionElement genericSource = linkM.getStart();
        GenericReactionElement genericTarget = linkM.getEnd();
        Link link = linkM.getLink();
        String clazz = linkM.getSbgnClass();

        Arc arc1 = new Arc();

        // if linkmodel points to elements which have ports, connect to ports instead of glyphs
        Object source, target;
        if(genericSource instanceof ReactionNodeModel) { // case of process, logic gate...
            String portNumber = "p2";
            if(linkM.isReversed()) {
                portNumber = "p1";
            }
            source = portMap.get(genericSource.getId()+"_"+portNumber);
            // TODO for reversible reactions, some product link can start from the input port!
        }
        else {
            if(genericSource instanceof ReactantModel
                    && genericSource.getGlyph().getCdShape() == CdShape.PHENOTYPE) {
                logger.warn("Arc with id: "+linkM.getId()+" is coming from phenotype glyph "+genericSource.getId()
                        +". Outgoing arcs are forbidden for phenotypes in SBGN, but are kept here.");
            }

            source = glyphMap.get(genericSource.getId());
        }

        // here we want to avoid linking to a process' port if the link is a catalysis, stimulation and so on.
        // only links going to ports are consumptiion/production and logic gates related
        if(genericTarget instanceof ReactionNodeModel
                && (clazz.equals("production") || clazz.equals("consumption") || clazz.equals("logic arc"))) {
            String portNumber = "p1";
            if(linkM.isReversed()) {
                portNumber = "p2";
            }
            target = portMap.get(genericTarget.getId()+"_"+portNumber);
            // TODO for reversible reactions, some product link can start from the input port!
        }
        else {
            target = glyphMap.get(genericTarget.getId());
        }

        arc1.setSource(source);
        arc1.setTarget(target);

        arc1.setClazz(clazz);
        arc1.setId(linkM.getId());

        Point2D startPoint = link.getStart();
        Point2D endPoint = link.getEnd();

        // start end
        Arc.Start s1 = new Arc.Start();
        s1.setX((float) startPoint.getX());
        s1.setY((float) startPoint.getY());
        arc1.setStart(s1);

        for(int i=0; i<link.getEditPoints().size() ; i++) {
            Point2D p = link.getEditPoints().get(i);
            Arc.Next next = new Arc.Next();
            next.setX((float) p.getX());
            next.setY((float) p.getY());
            arc1.getNext().add(next);
        }

        Arc.End e1 = new Arc.End();
        e1.setX((float) endPoint.getX());
        e1.setY((float) endPoint.getY());
        arc1.setEnd(e1);

        return arc1;
    }

    public Element getAllStyles(List<StyleInfo> styleInfoList, Sbml sbmldoc) {
        // convert to DOM document
        Document baseDoc = null;
        try {
            baseDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            /*JAXBContext context = JAXBContext.newInstance(Sbml.class.getPackage().getName());
            Marshaller marshaller = context.createMarshaller();*/
        } catch (ParserConfigurationException  e) {
            e.printStackTrace();
        }

        //Document baseDoc = sbmldoc.getSbml().getDomNode().getOwnerDocument();

        java.util.Map<String, String> colorMap = StyleInfo.getMapOfColorDefinitions(styleInfoList);
        Element renderInformation = baseDoc.createElement("renderInformation");
        renderInformation.setAttribute("xmlns", "http://www.sbml.org/sbml/level3/version1/render/version1");
        renderInformation.setAttribute("id", "renderInformation");
        renderInformation.setAttribute("programName", "cd2sbgnml");
        renderInformation.setAttribute("programVersion", "0.1"); // TODO fetch this properly, through maven
        renderInformation.setAttribute("backgroundColor", "#ffffff");

        Element listofcolors = baseDoc.createElement("listOfColorDefinitions");
        renderInformation.appendChild(listofcolors);

        for(String color: colorMap.keySet()){
            Element colorDef = baseDoc.createElement("colorDefinition");
            colorDef.setAttribute("id", colorMap.get(color));

            // switch from argb to rgba
            String alpha = color.substring(0,2);
            color = "#"+color.substring(2) + alpha;

            colorDef.setAttribute("value", color);

            listofcolors.appendChild(colorDef);
        }

        Element listofstyles = baseDoc.createElement("listOfStyles");
        renderInformation.appendChild(listofstyles);

        // first pass to aggregate all styles
        HashMap<String, StyleInfo> styleMap = new HashMap<>();
        HashMap<String, List<String>> idListMap = new HashMap<>();
        for(StyleInfo sinfo: styleInfoList) {
            if(!styleMap.containsKey(sinfo.getId())) {
                styleMap.put(sinfo.getId(), sinfo);
                List<String> idList = new ArrayList<>();
                idList.add(sinfo.getRefId());
                idListMap.put(sinfo.getId(), idList);
            }
            else {
                idListMap.get(sinfo.getId()).add(sinfo.getRefId());
            }
        }

        for(String styleId: styleMap.keySet()) {
            StyleInfo sinfo = styleMap.get(styleId);
            Element styleE = baseDoc.createElement("style");
            styleE.setAttribute("id", styleId);
            styleE.setAttribute("idList", idListMap.get(styleId).stream ().collect (Collectors.joining (" ")));

            Element g = baseDoc.createElement("g");
            g.setAttribute("fontSize", String.valueOf(sinfo.getFontSize()));
            g.setAttribute("stroke", colorMap.get(sinfo.getLineColor()));
            g.setAttribute("strokeWidth", String.valueOf(sinfo.getLineWidth()));
            g.setAttribute("fill", colorMap.get(sinfo.getBgColor()));

            styleE.appendChild(g);
            listofstyles.appendChild(styleE);
        }

        return renderInformation;
    }

    /**
     * Convert html Element into an SBGN notes object
     * @param notes
     * @return
     */
    public SBGNBase.Notes getSBGNNotes(Element notes) {
        SBGNBase.Notes newNotes = new SBGNBase.Notes();
        if(notes != null ){
            newNotes.getAny().add(notes);
            return newNotes;
        }
        return null;
    }

    /**
     * Convert a rdf element to an SBGN extension, with an annotation element inside wrapping the rdf.
     * @param rdf
     * @return
     */
    public SBGNBase.Extension getSBGNAnnotation(Element rdf, String refId) {
        if(rdf == null) {
            return null;
        }

        // assume the first description is the one concerning our element, which should always be the case
        Element description = (Element) rdf.getElementsByTagName("rdf:Description").item(0);
        description.setAttribute("rdf:about", "#"+refId);

        SBGNBase.Extension newExt = new SBGNBase.Extension();
        Element annotationElement = rdf.getOwnerDocument().createElement("annotation");
        annotationElement.appendChild(rdf.cloneNode(true));
        newExt.getAny().add(annotationElement);
        return newExt;
    }

    public GeneralModel convert(GeneralModel generalModel) throws ConversionException, ReadModelException {
        CellDesignerSBFCModel cdModel = (CellDesignerSBFCModel) generalModel;
        return new SBGNSBFCModel(this.toSbgn(cdModel.getSbml()));
    }

    public String getResultExtension() {
        return null;
    }

    public String getName() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getHtmlDescription() {
        return null;
    }
}
