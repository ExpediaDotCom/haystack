/*
 * Copyright 2018 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */

import React from 'react';
import Modal from 'react-modal';
import PropTypes from 'prop-types';

Modal.setAppElement('#root');
const ConnectionDetails = ({requestRate, errorPercent, onClose}) => {
    const modalStyles = {
        overlay: {
            zIndex: 10,
            backgroundColor: 'rgba(0, 0, 0, 0.5)'
        },
        content: {
            width: '45%',
            maxWidth: '1240px',
            top: '30%',
            bottom: 'auto',
            left: '0',
            right: '0',
            marginLeft: 'auto',
            marginRight: 'auto'
        }
    };

    return (
        <Modal style={modalStyles} isOpen contentLabel={'Modal'} onRequestClose={onClose}>
            <header className="clearfix">
                <h4 className="text-center">Request Rate: {requestRate}</h4>
                <h5 className="text-center">Error Rate: {errorPercent}</h5>
                <button onClick={onClose}>Close</button>
            </header>
        </Modal>
    );
};

ConnectionDetails.propTypes = {
    requestRate: PropTypes.string.isRequired,
    errorPercent: PropTypes.string.isRequired,
    onClose: PropTypes.func.isRequired
};
export default ConnectionDetails;
