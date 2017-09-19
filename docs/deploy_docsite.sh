#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

SOURCE_BRANCH="master"
TARGET_BRANCH="gh-pages-shadow"

# Save some useful information
REPO=`git config remote.origin.url`
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
SHA=`git rev-parse --verify HEAD`

# clone the existing gh-pages
git clone $REPO haystack
cd haystack/docs

# build and generate documentation site
./generate_docsite.sh

# copy generated sources
cp -R _book/* ..


# If there are no changes to the compiled out then just bail.
rm -rf ../deployment/
if git diff --quiet; then
    echo "No changes to the output on this push; exiting."
    exit 0
fi

# Commit the "changes", i.e. the new version.
# The delta will show diffs between new and old versions.
git clean -fx node_modules
git clean -fx _book
git add -A ..
git commit -m "Deploy to GitHub Pages: ${SHA}"

# Now that we're all set up, we can push.
#git push $SSH_REPO $SOURCE_BRANCH:$TARGET_BRANCH