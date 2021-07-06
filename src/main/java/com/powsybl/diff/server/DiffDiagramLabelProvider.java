/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff.server;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.InitialValue;
import org.apache.commons.math3.util.Precision;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@acotel-group.com>
 */
public class DiffDiagramLabelProvider extends DefaultDiagramLabelProvider {

    private final Network network;

    public DiffDiagramLabelProvider(Network net, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        super(net, componentLibrary, layoutParameters);
        this.network = Objects.requireNonNull(net);
    }

    @Override
    public InitialValue getInitialValue(Node node) {
        Objects.requireNonNull(node);
        switch (node.getType()) {
            case FEEDER:
                switch (((FeederNode) node).getFeederType()) {
                    case INJECTION:
                        return getInjectionInitialValue((FeederInjectionNode) node);
                    case BRANCH:
                        return getBranchInitialValue((FeederBranchNode) node);
                    case TWO_WINDINGS_TRANSFORMER_LEG:
                        return get2WTInitialValue((Feeder2WTLegNode) node);
                    case THREE_WINDINGS_TRANSFORMER_LEG:
                        return get3WTInitialValue((Feeder3WTLegNode) node);
                    default:
                        return new InitialValue(null, null, null, null, null, null);
                }
            default:
                return super.getInitialValue(node);
        }
    }

    protected InitialValue getInjectionInitialValue(FeederInjectionNode node) {
        Injection injection = (Injection) network.getIdentifiable(node.getEquipmentId());
        if (injection != null) {
            return buildInitialValue(injection.getTerminal());
        }
        return new InitialValue(null, null, null, null, null, null);
    }

    protected InitialValue buildInitialValue(Terminal terminal) {
        double p = terminal.getP();
        double q = terminal.getQ();
        String label1 = String.valueOf(Precision.round(p, 2));
        String label2 = String.valueOf(Precision.round(q, 2));
        Direction direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        Direction direction2 = q > 0 ? Direction.UP : Direction.DOWN;
        return new InitialValue(direction1, direction2, label1, label2, null, null);
    }

    protected InitialValue getBranchInitialValue(FeederBranchNode node) {
        Branch branch = network.getBranch(node.getEquipmentId());
        if (branch != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            return buildInitialValue(branch.getTerminal(side));
        }
        return new InitialValue(null, null, null, null, null, null);
    }

    protected InitialValue get2WTInitialValue(Feeder2WTLegNode node) {
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            Branch.Side side = Branch.Side.valueOf(node.getSide().name());
            return buildInitialValue(transformer.getTerminal(side));
        }
        return new InitialValue(null, null, null, null, null, null);
    }

    protected InitialValue get3WTInitialValue(Feeder3WTLegNode node) {
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer(node.getEquipmentId());
        if (transformer != null) {
            ThreeWindingsTransformer.Side side = ThreeWindingsTransformer.Side.valueOf(node.getSide().name());
            return buildInitialValue(transformer.getTerminal(side));
        }
        return new InitialValue(null, null, null, null, null, null);
    }
}
