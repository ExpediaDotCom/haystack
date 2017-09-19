#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

SOURCE_BRANCH="master"
TARGET_BRANCH="gh-pages-shadow"

# Save some useful information
REPO=`git config remote.origin.url`
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
SHA=`git rev-parse --verify HEAD`

# build and generate documentation site
# this would create a _book folder containing all the doc resources
cd docs
./generate_docsite.sh
cd ..

# Clone the existing gh-pages for this repo into out folder
git clone $REPO out
cd out
git checkout $TARGET_BRANCH

# copy generated sources to root of out
cp -R ../docs/_book/* .

# if there are no changes to the generated out then just bail
if git diff --quiet; then
    echo "No changes to the output on this push; exiting."
    exit 0
fi

# Commit the new version of doc page
git add -A .
git commit -m "Deploy to GitHub Pages: ${SHA}"

# now that we're all set up, push the changes to target branch
git push $SSH_REPO $TARGET_BRANCH