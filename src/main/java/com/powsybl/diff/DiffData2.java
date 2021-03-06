/**
 * Copyright (c) 2020-2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class DiffData2 {
    final List<String> switchesDiff;
    final List<String> branchesDiff;
    final List<LineDiffData> linesDiffData;
    final Map<String, VlDiffData> vlDiffData;

    private DiffData2(List<String> switchesDiff, List<String> branchesDiff, List<LineDiffData> linesDiffData, Map<String, VlDiffData> vlDiffData) {
        this.switchesDiff = switchesDiff;
        this.branchesDiff = branchesDiff;
        this.linesDiffData = linesDiffData;
        this.vlDiffData = vlDiffData;
    }

    public List<String> getSwitchesIds() {
        return switchesDiff;
    }

    public List<String> getBranchesIds() {
        return branchesDiff;
    }

    public List<LineDiffData> getLinesDiffData() {
        return linesDiffData;
    }

    public Map<String, VlDiffData> getVlDiffData() {
        return vlDiffData;
    }

    public static DiffData2 parseData(String jsonDiff) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(jsonDiff, new TypeReference<Map<String, Object>>() {
        });
        List<String> switchesDiff = (List<String>) ((List) jsonMap.get("diff.VoltageLevels")).stream()
                .map(t -> ((Map) t).get("vl.switchesStatus-delta"))
                .flatMap(t -> ((List<String>) t).stream())
                .collect(Collectors.toList());
        List<String> branchesDiff = (List<String>) ((List) jsonMap.get("diff.Branches")).stream()
                .map(t -> ((Map) t).get("branch.terminalStatus-delta"))
                .flatMap(t -> ((List<String>) t).stream())
                .collect(Collectors.toList());
        List<LineDiffData> linesDiffData = (List<LineDiffData>) ((List) jsonMap.get("diff.Branches")).stream()
                .map(t -> {
                    Map<String, Object> branchMap = (Map) t;
                    return new LineDiffData(
                            Utils.formatNum(branchMap.get("branch.branchId1").toString()),
                            Utils.formatNum(branchMap.get("branch.terminal1.p-delta").toString()),
                            Utils.formatNum(branchMap.get("branch.terminal1.q-delta").toString()),
                            Utils.formatNum(branchMap.get("branch.terminal1.i-delta").toString()),
                            Utils.formatNum(branchMap.get("branch.terminal2.p-delta").toString()),
                            Utils.formatNum(branchMap.get("branch.terminal2.q-delta").toString()),
                            Utils.formatNum(branchMap.get("branch.terminal2.i-delta").toString()),
                            Utils.formatPerc(branchMap.get("branch.terminal1.p-delta-percent").toString()),
                            Utils.formatPerc(branchMap.get("branch.terminal1.q-delta-percent").toString()),
                            Utils.formatPerc(branchMap.get("branch.terminal1.i-delta-percent").toString()),
                            Utils.formatPerc(branchMap.get("branch.terminal2.p-delta-percent").toString()),
                            Utils.formatPerc(branchMap.get("branch.terminal2.q-delta-percent").toString()),
                            Utils.formatPerc(branchMap.get("branch.terminal2.i-delta-percent").toString())
                    );
                })
                .collect(Collectors.toList());

        Map<String, VlDiffData> vlDiffData = (Map<String, VlDiffData>) ((List) jsonMap.get("diff.VoltageLevels")).stream()
                .map(t -> {
                    Map<String, Object> vlMap = (Map) t;
                    return new VlDiffData(
                            Utils.formatNum(vlMap.get("vl.vlId1").toString()),
                            Utils.formatNum(vlMap.get("vl.minV-delta").toString()),
                            Utils.formatNum(vlMap.get("vl.maxV-delta").toString()),
                            Utils.formatPerc(vlMap.get("vl.minV-delta-percent").toString()),
                            Utils.formatPerc(vlMap.get("vl.maxV-delta-percent").toString()));
                }).collect(Collectors.toMap(VlDiffData::getVlId, vd -> vd));
        return new DiffData2(switchesDiff, branchesDiff, linesDiffData, vlDiffData);
    }
}
