Modrinth: https://modrinth.com/mod/fplib

A library mod for 1.7.10 with lots of useful stuff. See the table below for more information.

| Package                                                         | Stuff it has                                                    |
|-----------------------------------------------------------------|-----------------------------------------------------------------|
| [compat](src/main/java/com/falsepattern/lib/compat)             | Code backported from vanilla 1.12.2.                            |
| [config](src/main/java/com/falsepattern/lib/config)             | 1.12.2-style annotation-based config library                    |
| [dependencies](src/main/java/com/falsepattern/lib/dependencies) | Runtime dependency loader. Uses Maven.                          |
| [mixin](src/main/java/com/falsepattern/lib/mixin)               | Mixin loader plugin boilerplate code                            |
| [text](src/main/java/com/falsepattern/lib/text)                 | Better Chat and GUI text processing                             |
| [util](src/main/java/com/falsepattern/lib/util)                 | Additional stuff that don't fit the above categories. See below |

The contents of the [util](src/main/java/com/falsepattern/lib/util) package so far:

| Class        | What it does                                                                                                   |
|--------------|----------------------------------------------------------------------------------------------------------------|
| ResourceUtil | Provides methods for efficiently retrieving resources from SPECIFIC jar files, instead of the entire classpath |


Anything annotated with [@StableAPI](src/main/java/com/falsepattern/lib/StableAPI.java) is guaranteed to not change in patch versions.

### This project WILL NOT be ported to any version beyond 1.7.10, don't even ask
