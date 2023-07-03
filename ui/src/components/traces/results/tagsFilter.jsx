/*
 * Copyright 2017 Expedia, Inc.
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

import React, {useState} from 'react';
import PropTypes from 'prop-types';

function hasTagsWithFilters(tags, parsedFilters) {
    return parsedFilters.every(filter =>
        tags.some(
            tag => tag && `${tag.key}=${tag.value}`.toLowerCase().includes(filter.toLowerCase())));
}

const TagsFilter = ({filterHandler}) => {
    const [value, setValue] = useState(null);

    const handleChange = (event) => {
        setValue(event.target.value);
        const parsedFilters = event.target.value.split(/[\s,;]+/);

        filterHandler({ callback: (tags) => {
                if (event.target) {
                    if (/^\s*$/.test(value)) return true;
                    return hasTagsWithFilters(tags, parsedFilters);
                }

                if (/^\s*$/.test(value)) return true;
                return hasTagsWithFilters(tags, parsedFilters);
            }});
    };

    return (
        <div>
            <input
                className="filter text-filter form-control"
                type="text"
                placeholder="Filter tags..."
                value={value}
                onChange={handleChange}
            />
        </div>
    );
};

TagsFilter.propTypes = {
    filterHandler: PropTypes.func.isRequired
};

export default TagsFilter;
