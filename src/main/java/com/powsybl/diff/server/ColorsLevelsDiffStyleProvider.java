/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
 * @author Massimo Ferraro <massimo.ferraro@soft.it>
 */
public class ColorsLevelsDiffStyleProvider extends DefaultDiagramStyleProvider implements ExtendedDiagramStyleProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColorsLevelsDiffStyleProvider.class);

    public static final String LEVEL_0_SUFFIX = "-level0";
    public static final String LEVEL_1_SUFFIX = "-level1";
    public static final String LEVEL_2_SUFFIX = "-level2";

    final ColorsLevelsDiffData diffData;
    final ColorsLevelsDiffConfig colorsLevelsDiffConfig;

    public ColorsLevelsDiffStyleProvider(ColorsLevelsDiffData diffData, ColorsLevelsDiffConfig colorsLevelsDiffConfig) {
        this.diffData = Objects.requireNonNull(diffData);
        this.colorsLevelsDiffConfig = Objects.requireNonNull(colorsLevelsDiffConfig);
    }

    @Override
    public List<String> getSvgNodeStyles(Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {
        List<String> nodeStyles = super.getSvgNodeStyles(node, componentLibrary, showInternalNodes);
        //LOGGER.debug("node before: id {} node_type {} componenttype {}, styles {}", node.getId(), node.getType(), node.getComponentType(), nodeStyles);
        String diffSuffix = LEVEL_0_SUFFIX;
        if (Node.NodeType.SWITCH.equals(node.getType()) && diffData.getSwitchesDiff().contains(node.getId())) {
            diffSuffix = LEVEL_2_SUFFIX;
        } else if (ComponentTypeName.TWO_WINDINGS_TRANSFORMER.equals(node.getComponentType())) {
            List<String> edgesNodesIds = node.getAdjacentEdges()
                                             .stream()
                                             .map(e -> e.getNodes()
                                                        .stream()
                                                        .map(n -> n.getId())
                                                        .filter(nId -> !nId.equals(node.getId()))
                                                        .collect(Collectors.toList()))
                                             .flatMap(List::stream)
                                             .collect(Collectors.toList());
            if (diffData.getBranchesSideDiffs().keySet().containsAll(edgesNodesIds) || diffData.getBranchesSideDiffs().containsKey(node.getId())) {
                double iDelta = 0;
                if (diffData.getBranchesSideDiffs().containsKey(node.getId())) {
                    iDelta = colorsLevelsDiffConfig.isUsePercentage()
                             ? diffData.getBranchesSideDiffs().get(node.getId()).getiDeltaP()
                             : diffData.getBranchesSideDiffs().get(node.getId()).getiDelta();
                } else {
                    iDelta = edgesNodesIds.stream()
                                          .mapToDouble(nodeId -> {
                                              return colorsLevelsDiffConfig.isUsePercentage()
                                                     ? diffData.getBranchesSideDiffs().get(nodeId).getiDeltaP()
                                                     : diffData.getBranchesSideDiffs().get(nodeId).getiDelta();
                                          })
                                          .max()
                                          .orElse(0);
                }
                diffSuffix = getLevel(iDelta);
            }
        }
        Collections.replaceAll(nodeStyles, CONSTANT_COLOR_CLASS, CONSTANT_COLOR_CLASS + diffSuffix);
        //LOGGER.debug("node after: id {} node_type {} componenttype {}, styles {}", node.getId(), node.getType(), node.getComponentType(), nodeStyles);
        return nodeStyles;
    }

    @Override
    public List<String> getSvgWireStyles(Edge edge, boolean highlightLineState) {
        List<String> style = super.getSvgWireStyles(edge, highlightLineState);
        //LOGGER.debug("edge before: Id1='{}', id2='{}', styles= '{}'", edge.getNode1().getId(), edge.getNode2().getId(), style);
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        String diffSuffix = LEVEL_0_SUFFIX;
        if (diffData.getBranchesSideDiffs().containsKey(node1.getId()) || diffData.getBranchesSideDiffs().containsKey(node2.getId())) {
            double iDelta = 0;
            if (diffData.getBranchesSideDiffs().containsKey(node1.getId())) {
                iDelta = colorsLevelsDiffConfig.isUsePercentage()
                         ? diffData.getBranchesSideDiffs().get(node1.getId()).getiDeltaP()
                         : diffData.getBranchesSideDiffs().get(node1.getId()).getiDelta();
            } else if (diffData.getBranchesSideDiffs().containsKey(node2.getId())) {
                iDelta = colorsLevelsDiffConfig.isUsePercentage()
                         ? diffData.getBranchesSideDiffs().get(node2.getId()).getiDeltaP()
                         : diffData.getBranchesSideDiffs().get(node2.getId()).getiDelta();
            }
            diffSuffix = getLevel(iDelta);
        }
        Collections.replaceAll(style, CONSTANT_COLOR_CLASS, CONSTANT_COLOR_CLASS + diffSuffix);
        //LOGGER.debug("edge after:  Id1='{}', id2='{}', styles= '{}'", edge.getNode1().getId(), edge.getNode2().getId(), style);
        return style;
    }

    @Override
    public List<String> getCssFilenames() {
        return Stream.concat(super.getCssFilenames().stream(), Stream.of("colors-levels-diffs.css")).collect(Collectors.toList());
    }

    public String getArrowsActiveStyle(FeederNode feederNode, ComponentLibrary componentLibrary) {
        String diffSuffix = LEVEL_0_SUFFIX;
        if (diffData.getBranchesSideDiffs().containsKey(feederNode.getId())) {
            double pDelta = colorsLevelsDiffConfig.isUsePercentage()
                            ? diffData.getBranchesSideDiffs().get(feederNode.getId()).getpDeltaP()
                            : diffData.getBranchesSideDiffs().get(feederNode.getId()).getpDelta();
            diffSuffix = getLevel(pDelta);
        }
        return ARROW_ACTIVE_CLASS + diffSuffix;
    }

    public String getArrowsReactiveStyle(FeederNode feederNode, ComponentLibrary componentLibrary) {
        String diffSuffix = LEVEL_0_SUFFIX;
        if (diffData.getBranchesSideDiffs().containsKey(feederNode.getId())) {
            double qDelta = colorsLevelsDiffConfig.isUsePercentage()
                            ? diffData.getBranchesSideDiffs().get(feederNode.getId()).getqDeltaP()
                            : diffData.getBranchesSideDiffs().get(feederNode.getId()).getqDelta();
            diffSuffix = getLevel(qDelta);
        }
        return ARROW_REACTIVE_CLASS + diffSuffix;
    }

    private String getLevel(double delta) {
        if (delta > colorsLevelsDiffConfig.getThresholdLevel2()) {
            return LEVEL_2_SUFFIX;
        }
        if (delta > colorsLevelsDiffConfig.getThresholdLevel1()) {
            return LEVEL_1_SUFFIX;
        }
        return LEVEL_0_SUFFIX;
    }
}
