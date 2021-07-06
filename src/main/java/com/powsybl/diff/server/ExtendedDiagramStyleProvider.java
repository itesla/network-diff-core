/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diff.server;

import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.svg.DiagramStyleProvider;

/**
 * @author Massimo Ferraro <massimo.ferraro@soft.it>
 */
public interface ExtendedDiagramStyleProvider extends DiagramStyleProvider {

    String getArrowsActiveStyle(FeederNode feederNode, ComponentLibrary componentLibrary);

    String getArrowsReactiveStyle(FeederNode feederNode, ComponentLibrary componentLibrary);

    default String getCss() {
        return null;
    }
}
