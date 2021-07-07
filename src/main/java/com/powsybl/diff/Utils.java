/**
 * Copyright (c) 2020-2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */

public final class Utils {

    private Utils() {
    }

    public static String formatNum(String aNum) {
        return formatNum(aNum, 3, "");
    }

    public static String formatPerc(String aNum) {
        return formatNum(aNum, 2, "");
    }

    public static String formatNum(String aNum, int prec, String suffix) {
        return (NumberUtils.isCreatable(aNum)) ? new BigDecimal(aNum).setScale(prec, RoundingMode.HALF_UP).toString() + suffix : aNum;
    }
}
