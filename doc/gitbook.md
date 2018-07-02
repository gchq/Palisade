# Building the documentation
Gaffer's documentation is built using [GitBook](https://www.gitbook.com). 

## Prerequisites
### NPM
You need NPM to install the GitBook command line toolchain. To get NPM install [node](https://nodejs.org/en/).

### GitBook command line tools

```bash
npm install -g gitbook-cli
```

## Build
Just run:
```bash
./doc/scripts/buildGitbook.sh
```

## Serve
To serve the gitbook locally at localhost:4000 just run:
```bash
./doc/scripts/serveGitbook.sh
```
