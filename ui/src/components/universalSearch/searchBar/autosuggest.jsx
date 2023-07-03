/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
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

import React, {useEffect, useRef, useState} from 'react';
import PropTypes from 'prop-types';
import {observer} from 'mobx-react';

import TimeWindowPicker from './timeWindowPicker';
import Chips from './chips';
import QueryBank from './queryBank';
import Guide from './guide';
import Suggestions from './suggestions';
import SearchSubmit from './searchSubmit';

import './autosuggest.less';

const subsystems = (window.haystackUiConfig && window.haystackUiConfig.subsystems) || [];
const tracesEnabled = subsystems.includes('traces');

const BACKSPACE = 8;
const SPACE = 32;
const TAB = 9;
const ENTER = 13;
const UP = 38;
const DOWN = 40;
const ESC = 27;

function focusInput(event) {
    const children = event.target.children;

    if (children.length) children[children.length - 1].focus();
}

function checkForWhitespacedValue(value) {
    return value.indexOf(' ') < 0 ? value : `"${value}"`;
}

function completeInputString(inputString) {
    if (inputString.includes('"')) {
        return (inputString.match(/"/g) || []).length === 2;
    }
    return /^([a-zA-Z0-9\s-]+)([=><])([a-zA-Z0-9,\s-:/_".#$+!%@^&?<>]+)$/g.test(inputString);
}

function findIndexOfOperator(inputString) {
    const match = inputString.match(/([=><])/);
    return match && match.index;
}

function removeWhiteSpaceAroundInput(inputString) {
    const operatorIndex = findIndexOfOperator(inputString);

    if (operatorIndex) {
        const operator = inputString[operatorIndex];
        return inputString
            .split(operator)
            .map((piece) => piece.trim())
            .join(operator);
    }
    return inputString.trim();
}

const Autosuggest = observer(({uiState, history, options, search, services, operationStore}) => {
    const [suggestionStrings, setSuggestionStrings] = useState([]);
    const [suggestionIndex, setSuggestionIndex] = useState(null);
    const [existingKeys, setExistingKeys] = useState([]);
    const [inputError, setInputError] = useState(false);
    const [suggestedOnValue, setSuggestedOnValue] = useState(null);
    const [suggestedOnType, setSuggestedOnType] = useState(null);
    const [activeServiceName, setActiveServiceName] = useState(uiState.serviceName || null);
    const fillInput = useRef(false);
    const wrapperRef = useRef(null);
    const [dropdownIsActive, setDropdownIsActive] = useState(false);
    const inputRef = useRef(null);
    const [filled, setFilled] = useState(false);

    useEffect(() => {
        const pageClickEvent = (e) => {
            // If clicked out of the dropdown, close the dropdown
            if (wrapperRef.current !== null && !wrapperRef.current.contains(e.target)) {
                setDropdownIsActive(!dropdownIsActive);
            }
        };

        // Enable listener if dropdown is opened
        if (dropdownIsActive) {
            window.addEventListener('mousedown', pageClickEvent);
        }

        // Cleanup when outside click occurs
        return () => {
            if (dropdownIsActive) {
                setSuggestionStrings([]);
            }
            window.removeEventListener('mousedown', pageClickEvent);
        };
    }, [dropdownIsActive]);

    // setting defaults for time
    if (!uiState.timeWindow) {
        uiState.setTimeWindow({
            endTime: undefined,
            startTime: undefined,
            timePreset: '1h'
        });
    }

    // Set default first-class citizens for searching
    useEffect(() => {
        options.serviceName = {isRangeQuery: false, values: services};
        options.operationName = {isRangeQuery: false, values: []};
        if (tracesEnabled) {
            options.traceId = {isRangeQuery: false, values: []};
        }
    }, [services]);

    // Checks for operations when a user supplies a new serviceName
    useEffect(() => {
        if (activeServiceName && tracesEnabled) {
            operationStore.fetchOperations(activeServiceName, () => {
                options.operationName.values = operationStore.operations;
            });
        }
    }, [activeServiceName]);

    const inputMatchesRangeKey = (input) => {
        let match = false;
        Object.keys(options).forEach((option) => {
            if (input === option && options[option].isRangeQuery === true) match = true;
        });
        return match;
    };

    // Hides suggestion list by emptying stateful array
    const handleBlur = () => {
        setSuggestionStrings([]);
        setDropdownIsActive(false);
    };

    // Test for correct formatting on K/V pairs
    const testForValidInputString = (kvPair) => {
        if (/^(.+)([=><])(.+)$/g.test(kvPair)) {
            // Ensure format is a=b
            const indexOfOperator = findIndexOfOperator(kvPair);
            const valueKey = kvPair.substring(0, indexOfOperator).trim();
            if (Object.keys(options).includes(valueKey)) {
                // Ensure key is searchable

                setExistingKeys((prevState) => [...prevState, valueKey]);
                return true;
            }
            setInputError('Indicated key is not whitelisted. Please submit a valid key');
            return false;
        }
        setInputError('Invalid K/V Pair, please use format "abc=xyz"');
        return false;
    };

    // Adds inputted text chip to client side store
    const addChipToPendingQueries = () => {
        handleBlur();
        const inputValue = removeWhiteSpaceAroundInput(inputRef.current.value);
        if (!inputValue) return;
        if (testForValidInputString(inputValue)) {
            // Valid input tests
            const kvPair = inputValue;
            const operatorIndex = findIndexOfOperator(kvPair);
            const chipKey = kvPair.substring(0, operatorIndex).trim();
            const chipValue = kvPair
                .substring(operatorIndex + 1, kvPair.length)
                .trim()
                .replace(/"/g, '');
            const operator = kvPair[operatorIndex];
            uiState.pendingQuery.push({query: uiState.query, key: chipKey, value: chipValue, operator}); // todo: not sure about query
            if (chipKey.includes('serviceName') && tracesEnabled) {
                uiState.serviceName = chipValue;
                options.operationName.values = [];
                operationStore.fetchOperations(chipValue, () => {
                    options.operationName.values = operationStore.operations;
                });
            }
            setInputError(false);
            inputRef.current.value = '';
        }
    };

    // Trigger from pressing the search button or enter on an empty input field
    const handleSearch = () => {
        if (inputRef.current.value) {
            addChipToPendingQueries();
        }
        search();
    };

    // Remove pending chip from client side store
    const deleteChip = (chipIndex) => {
        const targetChip = uiState.pendingQuery[chipIndex];
        if (targetChip !== undefined) {
            const updatedExistingKeys = existingKeys;
            if (targetChip.key === 'serviceName') {
                setActiveServiceName(null);
                options.operationName.values = [];
            }
            const itemIndex = updatedExistingKeys.indexOf(targetChip.key);
            updatedExistingKeys.splice(itemIndex, 1);

            setExistingKeys(updatedExistingKeys);
            uiState.pendingQuery.splice(chipIndex, 1);
        }
    };

    // Remove query from client side store
    const deleteQuery = (queryIndex, searchAfterDelete) => {
        const targetQuery = uiState.queries[queryIndex];
        if (targetQuery !== undefined) {
            const updatedExistingKeys = existingKeys;
            if (targetQuery.some((kv) => kv.key === 'serviceName')) {
                setActiveServiceName(null);
                options.operationName.values = [];
            }
            const itemIndex = updatedExistingKeys.indexOf(targetQuery.key);
            updatedExistingKeys.splice(itemIndex, 1);

            setExistingKeys(updatedExistingKeys);
            uiState.queries.splice(queryIndex, 1);
        }
        if (searchAfterDelete) {
            handleSearch();
        }
    };

    // Logic for when a user presses backspace to edit a chip
    const modifyChip = () => {
        const chip = uiState.pendingQuery[uiState.pendingQuery.length - 1];
        const value = checkForWhitespacedValue(chip.value);
        inputRef.current.value = `${chip.key}${chip.operator}${value}`;
        deleteChip(uiState.pendingQuery.length - 1);
    };

    // Logic for clicking on an existing query object below the search bar
    const modifyQuery = (index) => {
        uiState.pendingQuery = uiState.queries[index];
        inputRef.current.value = '';
        deleteQuery(index, false);
        inputRef.current.focus();
    };

    // Clears all pending and active queries
    const resetSearch = () => {
        inputRef.current.value = '';
        uiState.pendingQuery = [];
        uiState.queries = [];
        setInputError(false);
        history.push('/');
    };

    // Takes current input value and displays suggestion options.
    const setSuggestions = (input) => {
        if (services && services.length > 0) {
            options.serviceName.values = services;
        }
        let suggestionArray = [];
        const formattedInput = removeWhiteSpaceAroundInput(input);
        const operatorIndex = findIndexOfOperator(formattedInput);

        let value;
        let type;
        if (operatorIndex) {
            type = 'Values';
            const key = formattedInput.substring(0, operatorIndex).trim();
            value = formattedInput
                .substring(operatorIndex + 1, formattedInput.length)
                .trim()
                .replace('"', '');
            if (options[key] && options[key].values) {
                options[key].values.forEach((option) => {
                    if (option.toLowerCase().includes(value.toLowerCase()) && option !== value) {
                        suggestionArray.push({value: option});
                    }
                });
            }
        } else if (inputMatchesRangeKey(formattedInput)) {
            type = 'Operator';
            suggestionArray = [{value: '>'}, {value: '='}, {value: '<'}];
            value = '';
        } else {
            type = 'Keys';
            value = formattedInput.toLowerCase();
            Object.keys(options).forEach((option) => {
                if (option.toLowerCase().includes(value) && option !== value) {
                    suggestionArray.push({value: option, description: options[option].description});
                }
            });
        }
        setSuggestedOnValue(value);
        setSuggestionStrings(suggestionArray);
        setSuggestedOnType(type);
        setSuggestionIndex(null);
        setDropdownIsActive(true); // triggers useEffect with event listener
    };

    // hack
    useEffect(() => {
        if (filled === true) {
            setFilled(false);
            setSuggestions(inputRef.current.value);
        }
    }, [filled]);

    // Updates input field and uiState props value
    const updateFieldKv = (event) => {
        uiState.setFieldsUsingKvString(event.target.value);
        setSuggestions(event.target.value);
        if (suggestionIndex) {
            setSuggestionIndex(0);
        }
    };

    // Triggered upon clicking input box
    const handleFocus = (e) => {
        if (!suggestionStrings.length) {
            setSuggestions(e.target.value);
        }
    };

    // Changes active suggestion which will cause a re-render for hovered object
    const handleHover = (index) => {
        setSuggestionIndex(index);
    };

    // Selection choice fill when navigating with arrow keys
    const fillInputFromDropdownSelection = () => {
        const formattedInput = removeWhiteSpaceAroundInput(inputRef.current.value);
        const operatorIndex = findIndexOfOperator(formattedInput);
        let fillValue = '';
        let value = suggestionStrings[suggestionIndex || 0].value;
        value = checkForWhitespacedValue(value);
        if (suggestedOnType === 'operator') {
            const slicedInput = formattedInput.replace(/[(>=<)]/, '');
            fillValue = `${slicedInput}${value}`;
        } else if (operatorIndex) {
            const operator = formattedInput[operatorIndex];
            const key = formattedInput.substring(0, operatorIndex);
            fillValue = `${key}${operator}${value}`;
        } else {
            fillValue = value;
        }
        inputRef.current.value = fillValue;
    };

    useEffect(() => {
        // todo: potentially better solution than this
        if (fillInput.current === true) {
            fillInput.current = false;
            fillInputFromDropdownSelection();
        }
    }, [suggestionIndex]);

    // Selection chose when clicking or pressing space/tab/enter
    const handleDropdownSelection = () => {
        fillInputFromDropdownSelection();
        handleBlur();
        inputRef.current.focus();
        if (!findIndexOfOperator(inputRef.current.value)) {
            if (!inputMatchesRangeKey(inputRef.current.value)) inputRef.current.value = `${inputRef.current.value}=`;
            setSuggestionStrings([]);
            setFilled(true);
        }
        if (suggestedOnType === 'Values') addChipToPendingQueries();
    };

    // Moves highlighted suggestion down one option (toward bottom of screen)
    const lowerSuggestion = () => {
        if (suggestionStrings.length) {
            let stateUpdate = null;
            if (suggestionIndex !== null && suggestionIndex < suggestionStrings.length - 1) {
                stateUpdate = suggestionIndex + 1;
            } else {
                stateUpdate = 0;
            }
            fillInput.current = true;
            setSuggestionIndex(stateUpdate);
        }
    };

    // Moves highlighted suggestion up one option (toward top of screen)
    const higherSuggestion = () => {
        if (suggestionStrings.length) {
            let stateUpdate = null;
            if (suggestionIndex !== null && suggestionIndex > 0) {
                stateUpdate = suggestionIndex - 1;
            } else {
                stateUpdate = suggestionStrings.length - 1;
            }
            fillInput.current = true;
            setSuggestionIndex(stateUpdate);
        }
    };

    // Logic for navigation and selection with keyboard presses
    const handleKeyPress = (e) => {
        const keyPressed = e.keyCode || e.which;
        if ((keyPressed === ENTER || keyPressed === TAB) && suggestionStrings.length && inputRef.current.value.trim().length) {
            e.preventDefault();
            if (suggestionIndex !== null || suggestionStrings.length === 1) {
                handleDropdownSelection();
                handleBlur();
            }
        } else if (keyPressed === ENTER) {
            e.preventDefault();
            if (inputRef.current.value.trim().length) {
                addChipToPendingQueries();
            } else if (uiState.pendingQuery.length) {
                handleBlur();
                search();
            }
        } else if (keyPressed === TAB && inputRef.current.value) {
            e.preventDefault();
            addChipToPendingQueries();
        } else if (keyPressed === SPACE) {
            if (completeInputString(inputRef.current.value.trim())) {
                e.preventDefault();
                addChipToPendingQueries();
            }
        } else if (keyPressed === UP) {
            e.preventDefault();
            higherSuggestion();
        } else if (keyPressed === DOWN) {
            e.preventDefault();
            lowerSuggestion();
        } else if (keyPressed === ESC) {
            e.preventDefault();
            handleBlur();
        } else if (keyPressed === BACKSPACE) {
            const chips = uiState.pendingQuery;
            if (!inputRef.current.value && chips.length) {
                e.preventDefault();
                modifyChip();
                updateFieldKv(e);
            }
        }
    };

    // Logic for when the user pastes into search bar
    const handlePaste = (e) => {
        e.preventDefault();
        const splitPastedText = e.clipboardData.getData('Text').split('');
        splitPastedText.forEach((char) => {
            inputRef.current.value += char;
            if (char === ' ') handleKeyPress({keyCode: SPACE, preventDefault: () => {}});
        });
        if (completeInputString(inputRef.current.value)) addChipToPendingQueries();
    };

    const ErrorMessaging = () => (inputError ? <div className="usb-search__error-message">{inputError}</div> : null);

    return (
        <article className="usb-wrapper">
            <div className="usb-search" role="form" onClick={focusInput}>
                <Chips deleteChip={deleteChip} uiState={uiState} />
                <div className="usb-searchbar">
                    <input
                        type="text"
                        className="usb-searchbar__input"
                        onPaste={handlePaste}
                        onKeyDown={handleKeyPress}
                        onChange={updateFieldKv}
                        ref={inputRef}
                        onFocus={handleFocus}
                        placeholder={uiState.pendingQuery.length ? '' : 'Search tags and services...'}
                    />
                </div>
                {(uiState.queries.length > 0 || uiState.pendingQuery.length > 0) && (
                    <div className="usb-reset-button" role="button" tabIndex="-1" onClick={resetSearch}>
                        Reset Search
                    </div>
                )}
                <TimeWindowPicker uiState={uiState} />
                <SearchSubmit handleSearch={handleSearch} />
            </div>
            <div className="usb-suggestions">
                <div ref={wrapperRef} className={suggestionStrings.length ? 'usb-suggestions__tray clearfix' : 'hidden'}>
                    <Suggestions
                        handleHover={handleHover}
                        handleSelection={handleDropdownSelection}
                        suggestionIndex={suggestionIndex}
                        suggestionStrings={suggestionStrings}
                        suggestedOnType={suggestedOnType}
                        suggestedOnValue={suggestedOnValue}
                    />
                    <Guide searchHistory={uiState.searchHistory} />
                </div>
            </div>
            <QueryBank uiState={uiState} modifyQuery={modifyQuery} deleteQuery={deleteQuery} />
            <ErrorMessaging inputError={inputError} />
        </article>
    );
});

Autosuggest.propTypes = {
    uiState: PropTypes.object.isRequired,
    history: PropTypes.object.isRequired,
    options: PropTypes.object,
    search: PropTypes.func.isRequired,
    services: PropTypes.object.isRequired,
    operationStore: PropTypes.object.isRequired
};

Autosuggest.defaultProps = {
    options: {}
};

export default Autosuggest;
