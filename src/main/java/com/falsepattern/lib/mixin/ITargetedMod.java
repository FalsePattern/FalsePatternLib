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
package com.falsepattern.lib.mixin;

import com.google.common.io.Files;

import java.nio.file.Path;
import java.util.function.Predicate;

public interface ITargetedMod {
    String getModName();

    boolean isLoadInDevelopment();

    @SuppressWarnings("UnstableApiUsage")
    default boolean isMatchingJar(Path path) {
        String pathString = path.toString();
        String nameLowerCase = Files.getNameWithoutExtension(pathString).toLowerCase();
        String fileExtension = Files.getFileExtension(pathString);

        return "jar".equals(fileExtension) && getCondition().test(nameLowerCase);
    }

    Predicate<String> getCondition();

    final class PredicateHelpers {
        public static Predicate<String> startsWith(String subString) {
            return (name) -> name.startsWith(subString.toLowerCase());
        }

        public static Predicate<String> contains(String subString) {
            return (name) -> name.contains(subString.toLowerCase());
        }

        public static Predicate<String> matches(String regex) {
            return (name) -> name.matches(regex);
        }
    }
}
