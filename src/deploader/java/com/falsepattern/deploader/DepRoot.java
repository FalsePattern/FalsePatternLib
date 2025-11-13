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

package com.falsepattern.deploader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

@Data
@Accessors(fluent = true)
public class DepRoot {
    private String source;
    @Expose
    private Integer minJava;
    @Expose
    private Integer maxJava;
    @Expose
    private List<Dependency> bundledArtifacts;
    @Expose
    private List<String> repositories;
    @Expose
    private Dependencies dependencies;
    @Expose
    private Dependencies modDependencies;

    @Data
    @Accessors(fluent = true)
    public static class Dependencies {
        @Expose
        private SidedDependencies always;
        @Expose
        private SidedDependencies obf;
        @Expose
        private SidedDependencies dev;
    }

    @Data
    @Accessors(fluent = true)
    public static class SidedDependencies {
        @Expose
        private List<Dependency> common;
        @Expose
        private List<Dependency> client;
        @Expose
        private List<Dependency> server;
    }

    @Data
    @Accessors(fluent = true)
    public static class Dependency {
        @Expose
        private @Nullable String modid;
        @Expose
        private String artifact;

        @RequiredArgsConstructor
        public static class Adapter extends TypeAdapter<Dependency> {
            private final TypeAdapter<JsonElement> jsonElementTypeAdapter;

            @Override
            public void write(JsonWriter out, Dependency value) throws IOException {
                if (value.modid() == null) {
                    out.value(value.artifact());
                    return;
                }
                out.beginObject();
                out.name("modid");
                out.value(value.modid());
                out.name("artifact");
                out.value(value.artifact());
                out.endObject();
            }

            @Override
            public Dependency read(JsonReader in) throws IOException {
                val dep = new Dependency();
                switch (in.peek()) {
                    case STRING: {
                        val artifact = in.nextString();
                        dep.artifact(artifact);
                        break;
                    }
                    case BEGIN_OBJECT: {
                        val object = jsonElementTypeAdapter.read(in).getAsJsonObject();
                        if (!object.has("artifact")) {
                            throw new IllegalArgumentException("Missing artifact in dependency object!");
                        }
                        dep.artifact(object.get("artifact").getAsString());
                        if (object.has("modid")) {
                            dep.modid(object.get("modid").getAsString());
                        }
                        break;
                    }
                }
                return dep;
            }

            public static class Factory implements TypeAdapterFactory {
                @SuppressWarnings("unchecked")
                @Override
                public <T> @Nullable TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                    if (!Dependency.class.isAssignableFrom(type.getRawType())) {
                        return null;
                    }
                    val jsonElementAdapter = gson.getAdapter(JsonElement.class);

                    return (TypeAdapter<T>) new Adapter(jsonElementAdapter);
                }
            }
        }
    }
}
