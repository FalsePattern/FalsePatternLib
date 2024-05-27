/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lib.turboasm;

import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TransformerUtil {
    private static final Logger LOG = LogManager.getLogger("ASM");
    public static boolean executeTransformers(String transformedName, ClassNodeHandle handle, List<TurboClassTransformer> transformers) {
        boolean didTransformation = false;
        for (val transformer: transformers) {
            try {
                if (transformer.shouldTransformClass(transformedName, handle)) {
                    if (!didTransformation) {
                        LOG.debug("Transforming {}!", transformedName);
                    }
                    didTransformation = true;
                    transformer.transformClass(transformedName, handle);
                    LOG.debug("Successfully transformed class {} with {}, owner: {}", transformedName, transformer.name(), transformer.owner());
                }
            } catch (Exception e) {
                LOG.error("Failed to transform class {} with {}, owner: {}", transformedName, transformer.name(), transformer.owner());
                LOG.error("Exception stacktrace:", e);
            }
        }
        return didTransformation;
    }
}
