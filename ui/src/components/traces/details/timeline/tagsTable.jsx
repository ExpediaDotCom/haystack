/*
 * Copyright 2018 Expedia Group
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
import PropTypes from 'prop-types';
import _ from 'lodash';
import blobUtil from '../../../../utils/blobUtil';
import {tagTransformer} from '../../../../utils/externalLinkFormatter';

const TagsTable = ({tags}) => {
    const sortedTags = _.sortBy(tags, [(tag) => tag.key.toLowerCase()]);
    const blobsUrl = window.haystackUiConfig && window.haystackUiConfig.blobsUrl;

    if (sortedTags.length) {
        return (
            <table className="table table-striped">
                <thead>
                    <tr>
                        <th width={30}>Key</th>
                        <th width={70}>Value</th>
                    </tr>
                </thead>
                <tbody>
                    {sortedTags.map((tag) => (
                        <tr className="non-highlight-row" key={Math.random()}>
                            <td>{tag.key}</td>
                            <td>
                                {blobUtil.isBlobUrlTag(tag.key) && (window.haystackUiConfig && window.haystackUiConfig.enableBlobs) ? (
                                    <a href={blobUtil.formatBlobTagValue(tag.value, blobsUrl)} target="_blank">
                                        <span className="ti-new-window" /> <span>{blobUtil.formatBlobTagValue(tag.value, blobsUrl)}</span>
                                    </a>
                                ) : (
                                    <span>{tagTransformer(tag)}</span>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        );
    }

    return <h6>No tags associated with span</h6>;
};

TagsTable.propTypes = {
    tags: PropTypes.array.isRequired
};

export default TagsTable;
