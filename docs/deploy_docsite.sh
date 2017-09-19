#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

# build and generate documentation site
# this would create a _book folder containing all the doc resources
cd docs
./generate_docsite.sh

# copy generated sources to root of out
mkdir ../out
cp -R _book/* ../out
