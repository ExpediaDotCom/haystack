const siteConfig = {
    title: 'Haystack',
    tagline: 'A resilient, scalable tracing and analysis system',
    url: 'https://expediadotcom.github.io',
    baseUrl: '/haystack/',
    projectName: 'haystack',
    headerLinks: [
        {doc: 'introduction', label: 'Docs'},
        {page: 'help', label: 'Help'},
    ],
    headerIcon: 'img/logo/logo.png',
    footerIcon: 'img/logo/logo.png',
    favicon: 'img/favicon/favicon.png',
    /* colors for website */
    colors: {
        primaryColor: '#2d3750',
        secondaryColor: '#3f4d71',
        postHeaderColor: '#e23474',
    },
    fonts: {
        myFont: [
            "sansserif"
        ],
        myOtherFont: [
            "-apple-system",
            "system-ui"
        ]
    },
    copyright: 'Copyright © ' +
    new Date().getFullYear() +
    'Expedia',
    organizationName: 'ExpediaDotCom',
    highlight: {
        theme: 'solarized-dark',
    },
    editUrl: 'https://github.com/ExpediaDotCom/haystack/blob/master/docs/',
    scripts: ['https://buttons.github.io/buttons.js'],
    repoUrl: 'https://github.com/ExpediaDotCom/haystack',
    onPageNav: 'separate',
};

module.exports = siteConfig;
