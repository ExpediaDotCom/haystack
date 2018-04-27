/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

class Footer extends React.Component {
    docUrl(doc, language) {
        const baseUrl = this.props.config.baseUrl;
        return baseUrl + 'docs/' + doc;
    }

    pageUrl(doc, language) {
        const baseUrl = this.props.config.baseUrl;
        return baseUrl + doc;
    }

    render() {
        const currentYear = new Date().getFullYear();
        return (
            <footer className="nav-footer" id="footer">
                <section className="sitemap">
                    <a href={this.props.config.baseUrl} className="nav-home">
                        {this.props.config.footerIcon && (
                            <img
                                src={this.props.config.baseUrl + this.props.config.footerIcon}
                                alt={this.props.config.title}
                                width="66"
                                height="58"
                            />
                        )}
                    </a>
                    <div>
                        <h5>Docs</h5>
                        <a href={this.docUrl('introduction.html'}>
                            Introduction
                        </a>
                        <a href={this.docUrl('getting_started.html'}>
                            Getting Started
                        </a>
                        <a href={this.docUrl('architecture.html'}>
                            Architecture
                        </a>
                    </div>
                    <div>
                        <h5>Related</h5>
                        <a href="https://gitter.im/expedia-haystack/Lobby">Chat on Gitter</a>
                        <a href={this.docUrl('contributing.html'}>Contributing to Haystack</a>
                    </div>
                    <div>
                        <h5>More</h5>
                        <a href="https://github.com/ExpediaDotCom/haystack">GitHub</a>
                        <a
                            className="github-button"
                            href={this.props.config.repoUrl}
                            data-icon="octicon-star"
                            data-count-href="/facebook/docusaurus/stargazers"
                            data-show-count={true}
                            data-count-aria-label="# stargazers on GitHub"
                            aria-label="Star this project on GitHub">
                            Star
                        </a>
                    </div>
                </section>

                <a
                    href="https://www.expedia.com"
                    target="_blank"
                    className="footer__logo">
                    <img
                        src={this.props.config.baseUrl + 'img/logo/expedia_logo_inverted.png'}
                        alt="Expedia"
                        width="170"
                        height="45"
                    />
                </a>
                <section className="copyright">
                    Copyright &copy; {currentYear} Expedia
                </section>
            </footer>
        );
    }
}

module.exports = Footer;
