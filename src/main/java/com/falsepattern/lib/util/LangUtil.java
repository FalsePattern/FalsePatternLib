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
package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import cpw.mods.fml.common.registry.LanguageRegistry;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
@StableAPI(since = "0.8.0")
public final class LangUtil {
    @StableAPI.Expose
    public static final String DEFAULT_LOCALE = "en_US";
    private static final ThreadLocal<HashMap<String, String>> tempMap = ThreadLocal.withInitial(HashMap::new);

    @StableAPI.Expose
    public static void defaultLocalization(@NonNull Map<String, String> localeMap) {
        localeMap.forEach(LangUtil::defaultLocalization);
    }

    @StableAPI.Expose
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

