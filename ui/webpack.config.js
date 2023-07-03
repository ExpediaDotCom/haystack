const path = require('path');
const webpack = require('webpack');
const Assets = require('assets-webpack-plugin');

const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const LodashModuleReplacementPlugin = require('lodash-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

// paths --------------------------------------------------------------------------------------------------------
const appPaths = {
    public: path.join(__dirname, 'public/'),
    bundles: path.join(__dirname, 'public/bundles'),

    source: path.join(__dirname, 'src'),
    sourceAppJsx: path.join(__dirname, 'src/app.jsx'),
    sourceStyle: path.join(__dirname, 'src/'),

    views: path.join(__dirname, 'views'),
    indexView: path.join(__dirname, 'views/index.pug')
};

// progress plugin -------------------------------------------------------------------------------------------------
const ProgressBarPlugin = require('progress-bar-webpack-plugin');

const chalk = require('chalk'); // Provided through ProgressBarPlugin

const progressBarOptions = {
    format: `    ${chalk.blue.bold('build')} [:bar] ${chalk.green.bold(':percent')} (:elapsed s) :msg`,
    clear: false,
    complete: chalk.yellow(' \u2708'),
    incomplete: '  ',
    width: 20
};

// environment specific configs -------------------------------------------------------------------------------------
let devtool = 'source-map';
let jsBundleFilename = 'bundles/js/[name].[chunkhash].js';
let cssBundleFilename = 'bundles/style/[name].[chunkhash].css';

if (process.env.NODE_ENV === 'development') {
    devtool = 'eval-cheap-module-source-map';
    jsBundleFilename = 'bundles/js/[name].js';
    cssBundleFilename = 'bundles/style/[name].css';
}

// main config export -----------------------------------------------------------------------------------------------
module.exports = {
    entry: {
        app: appPaths.sourceAppJsx
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                loader: 'babel-loader',
                options: {
                    babelrc: false,
                    plugins: [
                        'lodash',
                        [
                            '@babel/plugin-proposal-decorators',
                            {
                                legacy: true
                            }
                        ],
                        [
                            '@babel/plugin-proposal-class-properties',
                            {
                                loose: true
                            }
                        ],
                    ],
                    presets: ['@babel/preset-env', '@babel/preset-react']
                }
            },
            {
                test: /\.less$/,
                exclude: /node_modules/,
                use: [
                    MiniCssExtractPlugin.loader,
                    {
                        loader: 'css-loader',
                        options: {
                            url: false
                        }
                    },
                    'less-loader'
                ]
            },
            {
                test: /\.svg$/,
                loader: 'svg-url-loader?encoding=base64'
            }
        ]
    },
    plugins: [
        new MiniCssExtractPlugin({filename: cssBundleFilename}),
        new LodashModuleReplacementPlugin({
            shorthands: true,
            collections: true
        }),
        new ProgressBarPlugin(Object.assign({}, progressBarOptions)),
        new BundleAnalyzerPlugin({
            analyzerMode: 'static',
            generateStatsFile: false,
            reportFilename: 'bundles/report.html',
            openAnalyzer: false
        }),
        new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
        new Assets({filename: 'public/assets.json'})
    ],
    output: {
        path: appPaths.public,
        publicPath: '/',
        filename: jsBundleFilename,
        sourceMapFilename: `${jsBundleFilename}.map`,
        chunkFilename: jsBundleFilename
    },
    resolve: {
        extensions: ['.json', '.js', '.jsx']
    },
    optimization: {
        splitChunks: {
            cacheGroups: {
                commons: {
                    chunks: 'initial',
                    test: path.resolve(__dirname, 'node_modules'),
                    name: 'commons',
                    enforce: true
                }
            }
        }
    },
    devtool
};
