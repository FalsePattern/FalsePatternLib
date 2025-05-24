/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

import com.falsepattern.lib.internal.Tags;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TransformerUtil {
    private static final boolean DEBUG_VERBOSE_TRANSFORMERS = Boolean.parseBoolean(System.getProperty(Tags.MODID + ".debug.verboseTransformers", "false"));
    private static final Logger LOG = LogManager.getLogger("ASM");
    public static boolean executeTransformers(String transformedName, ClassNodeHandle handle, List<TurboClassTransformer> transformers) {
        boolean modified = false;
        for (val transformer: transformers) {
            try {
                if (transformer.shouldTransformClass(transformedName, handle)) {
                    if (DEBUG_VERBOSE_TRANSFORMERS)
                        LOG.trace("Transforming {} with {}, owner: {}", transformedName, transformer.name(), transformer.owner());
                    if (transformer.transformClass(transformedName, handle)) {
                        if (DEBUG_VERBOSE_TRANSFORMERS)
                            LOG.trace("Transformed.");
                        modified = true;
                    } else {
                        if (DEBUG_VERBOSE_TRANSFORMERS)
                            LOG.trace("No change.");
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed to transform class {} with {}, owner: {}", transformedName, transformer.name(), transformer.owner());
                LOG.error("Exception stacktrace:", e);
            }
        }
        if (DEBUG_VERBOSE_TRANSFORMERS && modified) {
            LOG.trace("Transformed class {}", transformedName);
        }
        return modified;
    }
}
