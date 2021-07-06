/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff.server;

import static com.powsybl.sld.svg.DiagramStyles.ARROW_ACTIVE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.ARROW_REACTIVE_CLASS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.svg.GraphMetadata;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class DiffSVGWriter extends DefaultSVGWriter {

    private final ExtendedDiagramStyleProvider styleProvider;

    public DiffSVGWriter(ComponentLibrary componentLibrary, LayoutParameters layoutParameters, ExtendedDiagramStyleProvider styleProvider) {
        super(componentLibrary, layoutParameters);
        this.styleProvider = Objects.requireNonNull(styleProvider);
    }

    @Override
    protected void insertArrowsAndLabels(String prefixId, String wireId, List<Double> points, Element root, FeederNode feederNode, GraphMetadata metadata, DiagramLabelProvider initProvider, boolean feederArrowSymmetry) {
        super.insertArrowsAndLabels(prefixId, wireId, points, root, feederNode, metadata, initProvider, feederArrowSymmetry);
        //replace arrows css classes with the ones returned by the style provider
        for (int i = 0; i < root.getElementsByTagName(GROUP).getLength(); i++) {
            Node gNode = root.getElementsByTagName(GROUP).item(i);
            if (gNode instanceof Element) {
                List<String> classesList = Arrays.asList(((Element) gNode).getAttribute(CLASS).split(" "));
                Collections.replaceAll(classesList, ARROW_ACTIVE_CLASS, styleProvider.getArrowsActiveStyle(feederNode, componentLibrary));
                Collections.replaceAll(classesList, ARROW_REACTIVE_CLASS, styleProvider.getArrowsReactiveStyle(feederNode, componentLibrary));
                ((Element) gNode).setAttribute(CLASS, String.join(" ", classesList));
            }
        }
    }

    @Override
    protected void addStyle(Document document, DiagramStyleProvider styleProvider, DiagramLabelProvider labelProvider,
                            List<VoltageLevelGraph> graphs, Set<String> listUsedComponentSVG) {
        super.addStyle(document, styleProvider, labelProvider, graphs, listUsedComponentSVG);
        String css = this.styleProvider.getCss();
        if (css == null || css.isEmpty()) {
            return;
        }
        if (layoutParameters.isCssInternal()) {
            Node styleNode = document.getElementsByTagName(STYLE).item(0);
            Node oldCDataNode = styleNode.getFirstChild();
            if (oldCDataNode.getNodeType() == Node.CDATA_SECTION_NODE) {
                String oldCDataText = oldCDataNode.getNodeValue();
                String newCDataText = oldCDataText + "\n" + css + "\n";
                Node newCDataNode = document.createCDATASection(newCDataText);
                styleNode.replaceChild(newCDataNode, oldCDataNode);
            }
        }
    }
}
