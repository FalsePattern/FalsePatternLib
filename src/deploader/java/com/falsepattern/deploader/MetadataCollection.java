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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

class MetadataCollection
{
    @SuppressWarnings("unused")
    private String modListVersion;
    ModMetadata[] modList;
    private Map<String, ModMetadata> metadatas = Maps.newHashMap();

    public static MetadataCollection from(InputStream inputStream, String sourceName)
    {
        if (inputStream == null)
        {
            return new MetadataCollection();
        }

        InputStreamReader reader = new InputStreamReader(inputStream);
        try
        {
            MetadataCollection collection;
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            JsonElement rootElement = parser.parse(reader);
            if (rootElement.isJsonArray())
            {
                collection = new MetadataCollection();
                JsonArray jsonList = rootElement.getAsJsonArray();
                collection.modList = new ModMetadata[jsonList.size()];
                int i = 0;
                for (JsonElement mod : jsonList)
                {
                    collection.modList[i++]=gson.fromJson(mod, ModMetadata.class);
                }
            }
            else
            {
                collection = gson.fromJson(rootElement, MetadataCollection.class);
            }
            collection.parseModMetadataList();
            return collection;
        }
        catch (JsonParseException e)
        {
            DependencyLoaderImpl.LOG.error(e);
            DependencyLoaderImpl.LOG.error("The mcmod.info file in {} cannot be parsed as valid JSON. It will be ignored", sourceName);
            return new MetadataCollection();
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }


    private void parseModMetadataList()
    {
        for (ModMetadata modMetadata : modList)
        {
            metadatas.put(modMetadata.modId, modMetadata);
        }
    }
}