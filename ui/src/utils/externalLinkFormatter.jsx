/*
 * Copyright 2020 Expedia Group
 *
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */

import React from 'react';
import validUrl from './validUrl';

const externalLinks = (window.haystackUiConfig && window.haystackUiConfig.externalLinking);

export function constructExternalLinksList(linkType, linkValue) {
    return externalLinks
        .filter(link => link.key === linkType)
        .map(link => {
            const url = link.url.replace('#{key}', linkType).replace('#{value}', linkValue);

            return (<li>
                <a href={url} target="_blank">
                    <span className="ti-new-window"/> <span>{link.label || 'No Label Provided'} <code>{linkType}: {linkValue}</code></span>
                </a>
            </li>);
        });
}

/**
 * The following transform functions take the tag object (from the span data) and
 * transform configuration (from haystackUiConfig) as input and return jsx as output.
 */

/**
 * Default Transform:
 *
 * Returns either a hyperlink of the tag value or just a normal tag value in a <span>,
 * depending on whether the value is a valid url.
 */

function defaultTransform(tag) {
    const value = tag.value;
    return validUrl.isUrl(value) ?
        (<a href={value} target="_blank">
            <span className="ti-new-window"/> <span>{value}</span>
        </a>) :
        (<span>{`${value}`}</span>);
}

/**
 * Returns either a hyperlink where the url is the pattern with '!{temp}'
 * is replaced with the tag value.
 */
function linkTransform(tag, link) {
    const value = tag.value;
    const key = tag.key;
    const url = link.url.replace('#{value}', value).replace('#{key}', key);
    const label = link.label ? link.label.replace('#{tagKey}', tag.key) : value;

    return (<span> {value}
        <a className="tag-link" href={url} target="_blank">
            <span className="ti-new-window"/> {label}
        </a>
    </span>);
}

const tagLinks = externalLinks ? externalLinks.filter(link => link.key === 'tag') : [];

export function tagTransformer(tag) {
    const tagLink = tagLinks.find(link => link.tagKey === tag.key);
    if (tagLink) {
        return linkTransform(tag, tagLink);
    }
    return defaultTransform(tag);
}
