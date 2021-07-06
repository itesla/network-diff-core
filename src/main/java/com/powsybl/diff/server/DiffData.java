/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class DiffData {

    final List<String> switchesDiff;
    final List<String> branchesDiff;

    DiffData(String jsonDiff) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(jsonDiff, new TypeReference<Map<String, Object>>() { });
        switchesDiff = (List<String>) ((List) jsonMap.get("diff.VoltageLevels")).stream()
                .map(t -> ((Map) t).get("vl.switchesStatus-delta"))
                .flatMap(t -> ((List<String>) t).stream())
                .collect(Collectors.toList());
        branchesDiff = (List<String>) ((List) jsonMap.get("diff.Branches")).stream()
                .map(t -> ((Map) t).get("branch.terminalStatus-delta"))
                .flatMap(t -> ((List<String>) t).stream())
                .collect(Collectors.toList());
    }

    public List<String> getSwitchesIds() {
        return switchesDiff;
    }

    public List<String> getBranchesIds() {
        return branchesDiff;
    }
}
