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
import PropTypes from 'prop-types';
import Modal from 'react-modal';
import colorMapper from '../../utils/serviceColorMapper';
import './modal.less';

const ServiceNameTitle = ({serviceName}) => (
    <span className={`service-spans label ${colorMapper.toBackgroundClass(serviceName)}`}>{serviceName}</span>
);

ServiceNameTitle.propTypes = {
    serviceName: PropTypes.string.isRequired
};

const serviceTitle = (serviceName, clientServiceName) => {
    if (serviceName && clientServiceName) {
        return (
            <div>
                <ServiceNameTitle serviceName={clientServiceName} />
                <ServiceNameTitle serviceName={serviceName} />
            </div>
        );
    } else if (serviceName) {
        return (
            <div>
                <ServiceNameTitle serviceName={serviceName} />
            </div>
        );
    }
    return null;
};

const ModalView = ({serviceName, title, isOpen, closeModal, children, clientServiceName, height, width}) => {
    const modalStyles = {
        overlay: {
            zIndex: 10,
            backgroundColor: 'rgba(0, 0, 0, 0.5)'
        },
        content: {
            width,
            maxWidth: '1240px',
            top: '10%',
            height,
            left: '0',
            right: '0',
            marginLeft: 'auto',
            marginRight: 'auto'
        }
    };

    return (
        <Modal isOpen={isOpen} onRequestClose={closeModal} style={modalStyles} closeTimeoutMS={200} contentLabel={'Modal'} ariaHideApp={false}>
            <header className="clearfix">
                <div className="pull-left">
                    {serviceTitle(serviceName, clientServiceName)}
                    <h4>{title}</h4>
                </div>
                <button className="close pull-right" onClick={closeModal}>
                    &times;
                </button>
            </header>
            <section>{children}</section>
        </Modal>
    );
};

ModalView.defaultProps = {
    serviceName: '',
    clientServiceName: null,
    height: '85%',
    width: '95%'
};

ModalView.propTypes = {
    serviceName: PropTypes.string,
    title: PropTypes.string.isRequired,
    isOpen: PropTypes.bool.isRequired,
    closeModal: PropTypes.func.isRequired,
    children: PropTypes.oneOfType([PropTypes.array, PropTypes.object]).isRequired,
    clientServiceName: PropTypes.string,
    height: PropTypes.string,
    width: PropTypes.string
};

export default ModalView;
