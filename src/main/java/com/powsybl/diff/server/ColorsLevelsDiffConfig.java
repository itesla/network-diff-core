/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff.server;

/**
 * @author Massimo Ferraro <massimo.ferraro@soft.it>
 */
public class ColorsLevelsDiffConfig {

    final double thresholdLevel1;
    final double thresholdLevel2;
    final boolean usePercentage;

    public ColorsLevelsDiffConfig(double thresholdLevel1,
                                  double thresholdLevel2,
                                  boolean usePercentage) {
        this.thresholdLevel1 = thresholdLevel1;
        this.thresholdLevel2 = thresholdLevel2;
        this.usePercentage = usePercentage;
    }

    public double getThresholdLevel1() {
        return thresholdLevel1;
    }

    public double getThresholdLevel2() {
        return thresholdLevel2;
    }

    public boolean isUsePercentage() {
        return usePercentage;
    }
}
