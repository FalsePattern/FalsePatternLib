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
package com.falsepattern.lib.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import cpw.mods.fml.common.registry.LanguageRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.8.0
 */
@UtilityClass
public final class LangUtil {
    public static final String DEFAULT_LOCALE = "en_US";
    private static final ThreadLocal<HashMap<String, String>> tempMap = ThreadLocal.withInitial(HashMap::new);

    public static void defaultLocalization(@NonNull Map<String, String> localeMap) {
        localeMap.forEach(LangUtil::defaultLocalization);
    }

    public static void defaultLocalization(@NonNull String key, @NonNull String value) {
        if (!LanguageRegistry.instance().getStringLocalization(key, DEFAULT_LOCALE).isEmpty()) {
            return;
        }
        val map = tempMap.get();
        map.put(key, value);
        LanguageRegistry.instance().injectLanguage(DEFAULT_LOCALE, map);
        map.clear();
    }
}

