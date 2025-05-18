FalsePatternLib JSON dependency file example
Version 1

An example JSON file that can be used to download dependencies using FalsePatternLib. This replaces the legacy
DependencyLoader api. This file needs to reside inside the META-INF directory.

```json
{
  "identifier": "falsepatternlib_dependencies",
  "repositories": [
    "https://example.com/"
  ],
  "dependencies": {
    "always": {
      "common": [],
      "client": [],
      "server": []
    },
    "obf": {
      "common": [
        "com.example:examplejar:1.0.0"
      ],
      "client": [],
      "server": []
    },
    "dev": {
      "common": [],
      "client": [],
      "server": []
    }
  }
}
```

Explanation:

- `identifier`: The identifier of the json file. This must always be `falsepatternlib_dependencies` for the library
  downloader
  to recognize it.
- `repositories`: A list of maven repositories to use when downloading dependencies. These are used in addition to the
  default maven repositories. This is just a list of strings, each string being a https repository url.
  Additionally, every jar with a dependencies json file inside of it is treated as a "jar in jar" maven repository.
  This maven repository exists under `META-INF/falsepatternlib_repo/`
- `dependencies`: A categorized list of dependencies.
    - The `always` category gets downloaded both inside and outside the dev environment. Usually not needed, as gradle
      will automatically download dependencies.
    - The `obf` category gets downloaded only in the obfuscated environment. Any third party libraries that are not
      available by default from minecraft or forge, AND are also not shaded/shadowed into the jar, should be placed
      here.
    - The `dev` category gets downloaded only in the dev environment. Usually not needed, as gradle will automatically
      download dependencies.

Each of these categories also have 3 more subcategories:

- `common`: Dependencies that are required on both the client and the server.
- `client`: Dependencies that are required on the client only.
- `server`: Dependencies that are required on the server only.

Each of these subcategories is a list of strings, each string being a maven dependency string. These are the same as
the ones used in the gradle dependencies block. Note that this DOES NOT support the version range syntax, so you must
specify a specific version. For example, `com.example:examplejar:1.0.0` is valid,
but `com.example:examplejar:[1.0.0,2.0.0]` is not.
