# ![Haystack](../images/logo_small.png)

# [Haystack UI](https://github.com/ExpediaDotCom/haystack-ui)

## About

Haystack-UI is the user interface for the Haystack package. It is written in [Node.js](https://nodejs.org/en/) and has a [React](https://github.com/facebook/react)-based front-end with [Bootstrap](http://getbootstrap.com/) for styling.

Haystack-UI utilizes Airbnb's [ESLint](http://eslint.org/) for code and style guidelines.

#### Pre-requisites

Ensure you have `node` and `npm` installed.

#### Build and Run

This application uses [webpack](https://webpack.github.io/) as the UI module bundler. To build + bundle all the required UI assets (CSS/JS) and run expressjs server, use:

```
$ npm install
$ npm run start:dev
```

Once start is successful you can visit [http://localhost:8080/](http://localhost:8080/)

To continuously re-build the assets while you are developing, use this command in a separate terminal:

```
$ npm run watch
```
