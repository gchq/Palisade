<!---
Copyright 2020 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->

# Building the documentation
Palisade's documentation is built using [GitBook](https://www.gitbook.com). 

## Prerequisites
### NPM
You need NPM to install the GitBook command line toolchain.
To get NPM install [node](https://nodejs.org/en/).

### GitBook command line tools

```bash
npm install -g gitbook-cli
```

## Build
Just run:
```bash
./scripts/buildGitbook.sh
```

## Serve
To serve the gitbook locally at localhost:4000 just run:
```bash
./scripts/serveGitbook.sh
```
