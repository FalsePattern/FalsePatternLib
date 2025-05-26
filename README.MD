A library for 1.7.10 with lots of useful stuff, licensed under the LGPLv3 license.

| Package                                                         | Contents / Purpose                                                                                                                      |
|-----------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| [asm](src/main/java/com/falsepattern/lib/asm)                   | ASM helper code                                                                                                                         |
| [compat](src/main/java/com/falsepattern/lib/compat)             | Code backported from vanilla 1.12.2                                                                                                     |
| [config](src/main/java/com/falsepattern/lib/config)             | 1.12.2-style annotation-based config library                                                                                            |
| [dependencies](src/main/java/com/falsepattern/lib/dependencies) | Runtime dependency loader which uses Maven (DEPRECATED, see [The new data-driven dependency format](src/main/resources/DEPENDENCIES.md) |
| [mapping](src/main/java/com/falsepattern/lib/mapping)           | Notch-SRG-MCP name mapping helper code                                                                                                  |
| [mixin](src/main/java/com/falsepattern/lib/mixin)               | Mixin loader plugin boilerplate code                                                                                                    |
| [optifine](src/main/java/com/falsepattern/lib/optifine)         | Tools for messing with OptiFine                                                                                                         |
| [text](src/main/java/com/falsepattern/lib/text)                 | Better Chat and GUI text processing                                                                                                     |
| [toasts](src/main/java/com/falsepattern/lib/toasts)             | The toast system from newer versions, with some extras                                                                                  |
| [turboasm](src/main/java/com/falsepattern/lib/turboasm)         | A forge-style class transformer port of RFB's transformer API.                                                                          |
| [util](src/main/java/com/falsepattern/lib/util)                 | Additional utilities that do not fit the above categories, see below for more information                                               |

The contents of the [util](src/main/java/com/falsepattern/lib/util) package so far:

| Class                                                                        | Purpose                                                                                                                           |
|------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| [FileUtil](src/main/java/com/falsepattern/lib/util/FileUtil.java)            | Directory and file utilities.                                                                                                     |
| [LangUtil](src/main/java/com/falsepattern/lib/util/LangUtil.java)            | Utility for having default language values defined inside code instead of lang files                                              |
| [MathUtil](src/main/java/com/falsepattern/lib/util/MathUtil.java)            | Miscellaneous math helper code                                                                                                    |
| [ReflectionUtl](src/main/java/com/falsepattern/lib/util/ReflectionUtil.java) | Reflection helper code                                                                                                            |
| [RenderUtil](src/main/java/com/falsepattern/lib/util/RenderUtil.java)        | Rendering tools. Currently, the only thing implemented is a way to grab the current partial tick time from the minecraft instance |
| [ResourceUtil](src/main/java/com/falsepattern/lib/util/ResourceUtil.java)    | Methods for efficiently retrieving resources from SPECIFIC jar files instead of the entire classpath                              |

The update checker module and the library downloader module can be toggled with the `ENABLE_UPDATE_CHECKER` and 
`ENABLE_LIBRARY_DOWNLOADS` booleans in the `falsepatternlib.cfg` config file respectively.
