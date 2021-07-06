/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.Branch.Side;

/**
 * @author Massimo Ferraro <massimo.ferraro@soft.it>
 */
public class ColorsLevelsDiffData {

    final List<String> switchesDiff;
    final Map<String, BranchSideDiff> branchesSideDiffs;

    class BranchSideDiff {
        final double pDelta;
        final double qDelta;
        final double iDelta;
        final double pDeltaP;
        final double qDeltaP;
        final double iDeltaP;

        BranchSideDiff(double pDelta, double qDelta, double iDelta, double pDeltaP, double qDeltaP, double iDeltaP) {
            this.pDelta = pDelta;
            this.qDelta = qDelta;
            this.iDelta = iDelta;
            this.pDeltaP = pDeltaP;
            this.qDeltaP = qDeltaP;
            this.iDeltaP = iDeltaP;
        }

        public double getpDelta() {
            return pDelta;
        }

        public double getqDelta() {
            return qDelta;
        }

        public double getiDelta() {
            return iDelta;
        }

        public double getpDeltaP() {
            return pDeltaP;
        }

        public double getqDeltaP() {
            return qDeltaP;
        }

        public double getiDeltaP() {
            return iDeltaP;
        }
    }

    ColorsLevelsDiffData(String jsonDiff) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(jsonDiff, new TypeReference<Map<String, Object>>() { });
        switchesDiff = (List<String>) ((List) jsonMap.get("diff.VoltageLevels")).stream()
                                                                                .map(t -> ((Map) t).get("vl.switchesStatus-delta"))
                                                                                .flatMap(t -> ((List<String>) t).stream())
                                                                                .collect(Collectors.toList());
        branchesSideDiffs = new HashMap<String, ColorsLevelsDiffData.BranchSideDiff>();
        ((List) jsonMap.get("diff.Branches")).stream().forEach(branch -> {
            branchesSideDiffs.put(((Map) branch).get("branch.branchId1") + "_" + Side.ONE,
                                  new BranchSideDiff((((Map) branch).get("branch.terminal1.p-delta") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal1.p-delta"),
                                                     (((Map) branch).get("branch.terminal1.q-delta") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal1.q-delta"),
                                                     (((Map) branch).get("branch.terminal1.i-delta") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal1.i-delta"),
                                                     (((Map) branch).get("branch.terminal1.p-delta-percent") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal1.p-delta-percent"),
                                                     (((Map) branch).get("branch.terminal1.q-delta-percent") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal1.q-delta-percent"),
                                                     (((Map) branch).get("branch.terminal1.i-delta-percent") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal1.i-delta-percent")));
            branchesSideDiffs.put(((Map) branch).get("branch.branchId1") + "_" + Side.TWO,
                                  new BranchSideDiff((((Map) branch).get("branch.terminal2.p-delta") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal2.p-delta"),
                                                     (((Map) branch).get("branch.terminal2.q-delta") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal2.q-delta"),
                                                     (((Map) branch).get("branch.terminal2.i-delta") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal2.i-delta"),
                                                     (((Map) branch).get("branch.terminal2.p-delta-percent") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal2.p-delta-percent"),
                                                     (((Map) branch).get("branch.terminal2.q-delta-percent") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal2.q-delta-percent"),
                                                     (((Map) branch).get("branch.terminal2.i-delta-percent") instanceof String) ? Double.NaN : (Double) ((Map) branch).get("branch.terminal2.i-delta-percent")));
        });
    }

    public List<String> getSwitchesDiff() {
        return switchesDiff;
    }

    public Map<String, BranchSideDiff> getBranchesSideDiffs() {
        return branchesSideDiffs;
    }
}
