/*
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.mixin;

import com.falsepattern.lib.StableAPI;
import com.google.common.io.Files;

import java.nio.file.Path;
import java.util.function.Predicate;

@StableAPI(since = "0.8.0")
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
