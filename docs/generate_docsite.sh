#!/bin/bash

# install gitbook
npm install -g gitbook-cli

# install the plugins and build the static site
gitbook install && gitbook build

# copy the static site files into the root directory.
cp -R _book/* ..

# remove 'node_modules' and '_book' directory
git clean -fx node_modules
git clean -fx _book

# add all files
git add .

# commit
git commit -a -m "Updated docs"
