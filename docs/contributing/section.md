# ![Haystack](../images/logo_small.png)

## Contributing to Haystack

### Bugs

We use [Github Issues](https://github.com/ExpediaDotCom/haystack-ui/issues) for our bug reporting. Please make sure the bug isn't already listed before opening a new issue.

 ### Development

 All work on Haystack happens directly on Github. Core Haystack team members will review opened pull requests.

 ### Requests

 If you see a feature that you would like to be added, please open an issue in the respective repository or in the general [Haystack](https://github.com/ExpediaDotCom/haystack/issues) repo.

 ### Adding to Documentation

 The documentation uses [Gitbook](https://www.gitbook.com/) as an organizational framework. The documentation source can be found on the gh-pages branch in the [base haystack repository](https://github.com/ExpediaDotCom/haystack/).

  The following code can be used to build gitbook properly.



 ```
 git checkout gh-pages
 gitbook build
 git pull origin gh-pages --rebase
 cp -R _book/* .
 git clean -fx node_modules
 git clean -fx _book
 git add .
 git commit -a -m [commit addition/subtraction]
 git push origin gh-pages
 ```

 ### License

 By contributing to Haystack, you agree that your contributions will be licensed under its [Apache License](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE).
