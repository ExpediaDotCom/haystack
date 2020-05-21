/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');
const Container = CompLibrary.Container;

const siteConfig = require(process.cwd() + '/siteConfig.js');

const adopters = siteConfig.users;

function imgUrl(img) {
  return siteConfig.baseUrl + 'img/users/' + img;
}

class Users extends React.Component {
  render() {
    if (adopters.length === 0) {
      return null;
    }

    const UserLink = ({infoLink, image, caption}) => (
      <a className="link" href={infoLink} key={infoLink}>
        <img src={imgUrl(image)} alt={caption} title={caption} />
        <span className="caption">{caption}</span>
      </a>
    );

    const Showcase = ({users}) => (
      <div className="showcase">
        {users.map((user) => (
          <UserLink key={user.infoLink} {...user} />
        ))}
      </div>
    );

    return (
      <div className="mainContainer">
        <Container padding={['bottom']}>
          <div className="showcaseSection">
            <div className="prose">
              <h1>Haystack Adopters</h1>
            </div>
            <div className="logos">
              <Showcase users={adopters} />
            </div>
            <p>Are you using this project?</p>
            <a
              href="https://github.com/ExpediaDotCom/haystack/edit/master/docsite/adopters.js"
              className="button">
              Add your company to the list here!
            </a>
          </div>
        </Container>
      </div>
    );
  }
}

module.exports = Users;
