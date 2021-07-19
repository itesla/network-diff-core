/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff;

import org.apache.commons.math3.util.Precision;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.Feeder2WTLegNode;
import com.powsybl.sld.model.Feeder3WTLegNode;
import com.powsybl.sld.model.FeederBranchNode;
import com.powsybl.sld.svg.InitialValue;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@soft.it>
 */
public class MergedDiffDiagramLabelProvider extends DiffDiagramLabelProvider {

    final ColorsLevelsDiffData diffData;
    final boolean usePercentage;

    public MergedDiffDiagramLabelProvider(Network net, ComponentLibrary componentLibrary, LayoutParameters layoutParameters,
                                          ColorsLevelsDiffData diffData, boolean usePercentage) {
        super(net, componentLibrary, layoutParameters);
        this.diffData = diffData;
        this.usePercentage = usePercentage;
    }

    protected InitialValue buildInitialValue(Terminal terminal, String terminalLabel) {
        double p = terminal.getP();
        double q = terminal.getQ();
        double deltaP = 0;
        double deltaQ = 0;
        if (diffData.getBranchesSideDiffs().containsKey(terminalLabel)) {
            deltaP = usePercentage
                     ? diffData.getBranchesSideDiffs().get(terminalLabel).getpDeltaP()
                     : diffData.getBranchesSideDiffs().get(terminalLabel).getpDelta();
            deltaQ = usePercentage
                     ? diffData.getBranchesSideDiffs().get(terminalLabel).getqDeltaP()
                     : diffData.getBranchesSideDiffs().get(terminalLabel).getqDelta();
        }
        String label1 = String.valueOf(Precision.round(deltaP, 2));
        String label2 = String.valueOf(Precision.round(deltaQ, 2));
        if (usePercentage) {
            label1 = "NaN".equals(label1) ? label1 : label1 + "%";
            label2 = "NaN".equals(label2) ? label2 : label2 + "%";
        }
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;
        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }

    protected InitialValue getBranchInitialValue(FeederBranchNode node) {
        Branch branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            return buildInitialValue(branch.getTerminal(side), node.getEquipmentId() + "_" + node.getSide().name());
        }
        return new InitialValue(null, null, null, null, null, null);
    }

    protected InitialValue get2WTInitialValue(Feeder2WTLegNode node) {
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            return buildInitialValue(transformer.getTerminal(side), node.getEquipmentId() + "_" + node.getSide().name());
        }
        return new InitialValue(null, null, null, null, null, null);
    }

    protected InitialValue get3WTInitialValue(Feeder3WTLegNode node) {
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.valueOf(node.getSide().name());
            return buildInitialValue(transformer.getTerminal(side), node.getEquipmentId() + "_" + node.getSide().name());
        }
        return new InitialValue(null, null, null, null, null, null);
    }
}
