/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');
/* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

const siteConfig = require(process.cwd() + '/siteConfig.js');

function imgUrl(img) {
    return siteConfig.baseUrl + 'img/' + img;
}

function docUrl(doc, language) {
    return siteConfig.baseUrl + 'docs/' + (language ? language + '/' : '') + doc;
}

class Button extends React.Component {
    render() {
        return (
            <div className="pluginWrapper buttonWrapper">
                <a className="button" href={this.props.href} target={this.props.target}>
                    {this.props.children}
                </a>
            </div>
        );
    }
}

Button.defaultProps = {
    target: '_self',
};

const SplashContainer = props => (
    <div className="homeContainer">
        <div className="homeSplashFade">
            <div className="wrapper homeWrapper">{props.children}</div>
        </div>
    </div>
);

const PromoSection = props => (
    <div className="section promoSection">
        <div className="promoRow">
            <div className="pluginRowBlock">{props.children}</div>
        </div>
    </div>
);

class HomeSplash extends React.Component {
    render() {
        let language = this.props.language || '';
        return (
            <SplashContainer>
                <div className="inner">
                    <img width="632" height="373" src={imgUrl('/logo/logo_with_title_transparent.png')} />
                    <h2 className="projectTitle">
                        <small>
                            A resilient, scalable tracing and analysis system
                        </small>
                    </h2>
                    <PromoSection>
                        <Button href={docUrl('getting_started.html', language)}>
                            Get Started
                        </Button>
                        <Button target="_blank" href="https://github.com/ExpediaDotCom/haystack">
                            Github
                        </Button>
                    </PromoSection>
                </div>
            </SplashContainer>
        );
    }
}

const Block = props => (
    <Container
        padding={['bottom', 'top']}
        id={props.id}
        background={props.background}>
        <GridBlock align={props.align} contents={props.children} layout={props.layout}/>
    </Container>
);

const Features = props => (
    <Block background="dark" layout="fourColumn" align="center">
        {[
            {
                content: 'Distributed tracing at scale using OpenTracing compliant tracing engine',
                image: imgUrl('align-left.svg'),
                imageAlign: 'top',
                title: 'Traces',
            },
            {
                content: 'Trends on vital service health parameters',
                image: imgUrl('stats-up.svg'),
                imageAlign: 'top',
                title: 'Trends',
            },
            {
                content: 'Identifying anomalies in services’ health and trigger alerts',
                image: imgUrl('bell.svg'),
                imageAlign: 'top',
                title: 'Anomaly Detection',
            },
            {
                content: 'Visualize dependencies and call flow across services',
                image: imgUrl('vector.svg'),
                imageAlign: 'top',
                title: 'Dependencies',
            },
        ]}
    </Block>
);

const Feature1 = props => (
    <Block background="light" layout="twoColumn" align="left">
        {[
            {
                content: 'Based on Google\'s dapper paper and full OpenTracing compliant, Haystack has distributed tracing at its core. Designed to ingest large volume of production traces with high resiliency and scalability.',
                image: imgUrl('opentracing.png'),
                imageAlign: 'left',
                title: 'Open Tracing'
            },
        ]}
    </Block>
);

const Feature2 = props => (
    <Block background="dark" layout="twoColumn" align="left">
        {[
            {
                content: 'Why just stop at distributed tracing when you can leverage the opentracing data to create operation trends, setup anomaly detection and build service dependency graphs.',
                image: imgUrl('demo.gif'),
                imageAlign: 'right',
                title: 'Why stop at Tracing?',
            },
        ]}
    </Block>
);

const Feature3 = props => (
    <Block background="light" layout="twoColumn" align="left">
        {[
            {
                content: 'Haystack is designed to be easily enhanceable. Built around a Kafka backbone, its easy to spin off a new streams app for building your own Haystack subsystem.',
                image: imgUrl('logo/logo.png'),
                imageAlign: 'left',
                title: 'Open for enhancements',
            },
        ]}
    </Block>
);

class Index extends React.Component {
    render() {
        let language = this.props.language || '';

        return (
            <div>
                <HomeSplash language={language}/>
                <div className="mainContainer">
                    <Features />
                    <Feature1 />
                    <Feature2 />
                    <Feature3 />
                </div>
            </div>
        );
    }
}

module.exports = Index;
