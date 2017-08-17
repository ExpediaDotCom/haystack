#!/bin/bash

# this script will use /docs folder as source of md files
# will generate gitbook styled html files from md files
# and get them ready for commit and push to gh-pages file
# TODO will automate this process using a build job

# install gitbook
npm install -g gitbook-cli

# install the plugins and build the static site
gitbook install && gitbook build

# copy the static site files into the root directory.
cp -R _book/* ..

# remove 'node_modules' and '_book' directory
git clean -fx node_modules
git clean -fx _book

# add rest of the files
git add ..
