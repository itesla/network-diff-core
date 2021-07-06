/**
 * Copyright (c) 2020-2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff.server;

import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.svg.DiagramStyles.ARROW_ACTIVE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.ARROW_REACTIVE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.CONSTANT_COLOR_CLASS;

/**
 * @author Giovanni Ferrari <giovanni.ferrari@techrain.eu>
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class DiffStyleProvider extends DefaultDiagramStyleProvider implements ExtendedDiagramStyleProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffStyleProvider.class);

    public static final String UNCHANGED_SUFFIX = "-diff1";
    public static final String CHANGED_SUFFIX = "-diff2";

    final DiffData diffData;

    public DiffStyleProvider(DiffData diffData) {
        this.diffData = Objects.requireNonNull(diffData);
    }

    @Override
    public List<String> getSvgNodeStyles(Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        List<String> nodeStyles = super.getSvgNodeStyles(node, componentLibrary, showInternalNodes);
        //LOGGER.debug("node before: id {} node_type {} componenttype {}, styles {}", node.getId(), node.getType(), node.getComponentType(), nodeStyles);
        Collections.replaceAll(nodeStyles, CONSTANT_COLOR_CLASS, CONSTANT_COLOR_CLASS + UNCHANGED_SUFFIX);

        if (Node.NodeType.SWITCH.equals(node.getType()) && diffData.getSwitchesIds().contains(node.getId())) {
            Collections.replaceAll(nodeStyles, CONSTANT_COLOR_CLASS + UNCHANGED_SUFFIX, CONSTANT_COLOR_CLASS + CHANGED_SUFFIX);
        } else if (ComponentTypeName.TWO_WINDINGS_TRANSFORMER.equals(node.getComponentType())) {
            List<String> edgesNodesIds = node.getAdjacentEdges().stream()
                    .map(e -> e.getNodes().stream().map(n -> n.getId())
                            .filter(nId -> !nId.equals(node.getId()))
                            .collect(Collectors.toList()))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            if (diffData.getBranchesIds().containsAll(edgesNodesIds) || diffData.getBranchesIds().contains(node.getId())) {
                Collections.replaceAll(nodeStyles, CONSTANT_COLOR_CLASS + UNCHANGED_SUFFIX, CONSTANT_COLOR_CLASS + CHANGED_SUFFIX);
            }
        }

        //LOGGER.debug("node after: id {} node_type {} componenttype {}, styles {}", node.getId(), node.getType(), node.getComponentType(), nodeStyles);
        return nodeStyles;
    }

    @Override
    public List<String> getSvgWireStyles(Edge edge, boolean highlightLineState) {
        List<String> style = super.getSvgWireStyles(edge, highlightLineState);
        //LOGGER.debug("edge before: Id1='{}', id2='{}', styles= '{}'", edge.getNode1().getId(), edge.getNode2().getId(), style);
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if (diffData.getBranchesIds().contains(node1.getId()) || diffData.getBranchesIds().contains(node2.getId())) {
            Collections.replaceAll(style, CONSTANT_COLOR_CLASS, CONSTANT_COLOR_CLASS + CHANGED_SUFFIX);
        } else {
            Collections.replaceAll(style, CONSTANT_COLOR_CLASS, CONSTANT_COLOR_CLASS + UNCHANGED_SUFFIX);
        }
        //LOGGER.debug("edge after:  Id1='{}', id2='{}', styles= '{}'", edge.getNode1().getId(), edge.getNode2().getId(), style);
        return style;
    }

    @Override
    public List<String> getCssFilenames() {
        return Stream.concat(super.getCssFilenames().stream(), Stream.of("diffs.css")).collect(Collectors.toList());
    }

    public String getArrowsActiveStyle(FeederNode feederNode, ComponentLibrary componentLibrary) {
        String diffSuffix = diffData.getBranchesIds().contains(feederNode.getId()) ? CHANGED_SUFFIX : UNCHANGED_SUFFIX;
        return ARROW_ACTIVE_CLASS + diffSuffix;
    }

    public String getArrowsReactiveStyle(FeederNode feederNode, ComponentLibrary componentLibrary) {
        String diffSuffix = diffData.getBranchesIds().contains(feederNode.getId()) ? CHANGED_SUFFIX : UNCHANGED_SUFFIX;
        return ARROW_REACTIVE_CLASS + diffSuffix;
    }

}
