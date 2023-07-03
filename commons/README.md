[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-commons.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-commons)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)


# haystack-commons
Module with common code that is used by various haystack components

## Building

Since this repo contains haystack-idl as the submodule, so use the following to clone the repo

```git clone --recursive git@github.com:ExpediaDotCom/haystack-commons.git```

#### Prerequisite: 

* Make sure you have Java 1.8
* Make sure you have maven 3.3.9 or higher


#### Build

For a full build including unit tests, one can run -

```
mvn clean package
```

#### Updating haystack-idl

* Run:

```git submodule update --recursive --remote```

* Update maven version

* Raise a PR

#### Releasing haystack-commons
* https://github.com/ExpediaDotCom/haystack-commons/blob/master/Release.md

