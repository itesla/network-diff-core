/**
 * Copyright (c) 2020-2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff.server;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.diff.*;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.SubstationDiagram;
import com.powsybl.sld.VoltageLevelDiagram;
import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SmartVoltageLevelLayoutFactory;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.svg.DiagramLabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class NetworkDiffUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkDiffUtil.class);

    //voltage levels
    public String diffVoltageLevel(Network network1, Network network2, String vlId, double epsilon, double voltageEpsilon) {
        List<String> voltageLevels = Collections.singletonList(vlId);
        List<String> branches = network1.getVoltageLevel(vlId).getConnectableStream(Branch.class).map(Branch::getId).collect(Collectors.toList());
        return diffNetworks(network1, network2, voltageLevels, branches, epsilon, voltageEpsilon);
    }

    public String diffNetworks(Network network1, Network network2, List<String> voltageLevels, List<String> branches, double epsilon, double voltageEpsilon) {
        DiffEquipment diffEquipment = new DiffEquipment();
        diffEquipment.setVoltageLevels(voltageLevels);
        List<DiffEquipmentType> equipmentTypes = new ArrayList<DiffEquipmentType>();
        equipmentTypes.add(DiffEquipmentType.VOLTAGE_LEVELS);
        if (!branches.isEmpty()) {
            equipmentTypes.add(DiffEquipmentType.BRANCHES);
            diffEquipment.setBranches(branches);
        }
        diffEquipment.setEquipmentTypes(equipmentTypes);
        NetworkDiff ndiff = new NetworkDiff(new DiffConfig(epsilon, voltageEpsilon, DiffConfig.FILTER_DIFF_DEFAULT));
        NetworkDiffResults diffVl = ndiff.diff(network1, network2, diffEquipment);
        String jsonDiff = NetworkDiff.writeJson(diffVl);
        //NaN is not part of the JSON standard and frontend would fail when parsing it
        //it should be handled at the source, though
        jsonDiff = jsonDiff.replace(": NaN,", ": \"Nan\",");
        jsonDiff = jsonDiff.replace(": Infinity,", ": \"Infinity\",");
        jsonDiff = jsonDiff.replace(": -Infinity,", ": \"-Infinity\",");
        return jsonDiff;
    }

    public String getVoltageLevelSvgDiff(Network network1, Network network2, String vlId, double epsilon, double voltageEpsilon, LevelsData levelsData) {
        try {
            String jsonDiff = diffVoltageLevel(network1, network2, vlId, epsilon, voltageEpsilon);
//            DiffData diffData = new DiffData(jsonDiff);
//            return writeVoltageLevelSvg(network1, vlId, new DiffStyleProvider(diffData));
            ColorsLevelsDiffData diffData = new ColorsLevelsDiffData(jsonDiff);
//            return writeVoltageLevelSvg(network1, vlId, new ColorsLevelsDiffStyleProvider(diffData, new ColorsLevelsDiffConfig(0, 10, true)));
            return writeVoltageLevelSvg(network1, vlId, new MultipleColorsLevelsDiffStyleProvider(diffData, levelsData, true));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new PowsyblException(e.getMessage(), e);
        }
    }

    private String writeVoltageLevelSvg(Network network, String vlId, ExtendedDiagramStyleProvider styleProvider) {
        String svgData;
        String metadataData;
        String jsonData;
        try (StringWriter svgWriter = new StringWriter();
             StringWriter metadataWriter = new StringWriter();
             StringWriter jsonWriter = new StringWriter()) {
            LayoutParameters layoutParameters = new LayoutParameters();
            layoutParameters.setCssInternal(true);
            ComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");
            DiagramLabelProvider initProvider = new DiffDiagramLabelProvider(network, componentLibrary, layoutParameters);
            GraphBuilder graphBuilder = new NetworkGraphBuilder(network);
            VoltageLevelDiagram diagram = VoltageLevelDiagram.build(graphBuilder, vlId, new SmartVoltageLevelLayoutFactory(network), false);
            diagram.writeSvg("",
                    new DiffSVGWriter(componentLibrary, layoutParameters, styleProvider),
                    initProvider,
                    styleProvider,
                    svgWriter,
                    metadataWriter);
            diagram.getGraph().writeJson(jsonWriter);
            svgWriter.flush();
            metadataWriter.flush();
            svgData = svgWriter.toString();
            metadataData = metadataWriter.toString();
            jsonData = jsonWriter.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return svgData;
    }

    //substations
    public String getSubstationSvgDiff(Network network1, Network network2, String substationId, double epsilon, double voltageEpsilon, LevelsData levelsData) {
        try {
            String jsonDiff = diffSubstation(network1, network2, substationId, epsilon, voltageEpsilon);
//            DiffData diffData = new DiffData(jsonDiff);
//            return writeSubstationSvg(network1, substationId, new DiffStyleProvider(diffData));
            ColorsLevelsDiffData diffData = new ColorsLevelsDiffData(jsonDiff);
//            return writeSubstationSvg(network1, substationId, new ColorsLevelsDiffStyleProvider(diffData, new ColorsLevelsDiffConfig(0, 10, true)));
            return writeSubstationSvg(network1, substationId, new MultipleColorsLevelsDiffStyleProvider(diffData, levelsData, true));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new PowsyblException(e.getMessage(), e);
        }
    }

    private String writeSubstationSvg(Network network, String substationId, ExtendedDiagramStyleProvider styleProvider) {
        String svgData;
        String metadataData;
        String jsonData;
        try (StringWriter svgWriter = new StringWriter();
             StringWriter metadataWriter = new StringWriter();
             StringWriter jsonWriter = new StringWriter()) {
            LayoutParameters layoutParameters = new LayoutParameters();
            layoutParameters.setCssInternal(true);
            ComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");
            DiagramLabelProvider initProvider = new DiffDiagramLabelProvider(network, componentLibrary, layoutParameters);
            GraphBuilder graphBuilder = new NetworkGraphBuilder(network);
            SubstationDiagram diagram = SubstationDiagram.build(graphBuilder, substationId, new HorizontalSubstationLayoutFactory(),
                    new SmartVoltageLevelLayoutFactory(network), false);
            diagram.writeSvg("",
                    new DiffSVGWriter(componentLibrary, layoutParameters, styleProvider),
                    initProvider,
                    styleProvider,
                    svgWriter,
                    metadataWriter);
            diagram.getSubGraph().writeJson(jsonWriter);
            svgWriter.flush();
            metadataWriter.flush();
            svgData = svgWriter.toString();
            metadataData = metadataWriter.toString();
            jsonData = jsonWriter.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return svgData;
    }

    public String diffSubstation(Network network1, Network network2, String substationId, double epsilon, double voltageEpsilon) {
        Substation substation1 = network1.getSubstation(substationId);
        List<String> voltageLevels = substation1.getVoltageLevelStream().map(VoltageLevel::getId)
                .collect(Collectors.toList());
        List<String> branches = substation1.getVoltageLevelStream().flatMap(vl -> vl.getConnectableStream(Line.class))
                .map(Line::getId).collect(Collectors.toList());
        List<String> twts = substation1.getTwoWindingsTransformerStream().map(TwoWindingsTransformer::getId)
                .collect(Collectors.toList());
        branches.addAll(twts);
        String jsonDiff = diffNetworks(network1, network2, voltageLevels, branches, epsilon, voltageEpsilon);
        return jsonDiff;
    }
}
