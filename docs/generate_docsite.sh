#!/bin/bash

# this script will use /docs folder as source of md files
# will generate gitbook styled html files from md files
# and get them ready for commit and push to gh-pages file

# install gitbook
npm install -g gitbook-cli

# install the plugins and build the static site
gitbook install && gitbook build
