#!/bin/bash

# this script will use /docs folder as source of md files
# will generate styled html files from md files

cd ./docsite
# install
npm install && npm run publish-gh-pages

