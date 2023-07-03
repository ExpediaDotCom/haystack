# Releasing

Currently we publish the repo to docker hub and nexus central repository.

## How to release and publish

* Git tagging: 

```
git tag -a <tag name> -m "Release description..."
git push origin <tag name>
```

`<tag name>` must follow semantic versioning scheme.

Or one can also tag using UI: https://github.com/ExpediaDotCom/haystack-traces/releases

It is preferred to create an annotated tag using `git tag -a` and then use the release UI to add release notes for the tag.

* After the release is completed, please update the `pom.xml` files to next `-SNAPSHOT` version to match the next release
