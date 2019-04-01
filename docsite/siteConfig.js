const siteConfig = {
    title: 'Haystack',
    tagline: 'A resilient, scalable tracing and analysis system',
    url: 'https://expediadotcom.github.io',
    baseUrl: '/haystack/',
    projectName: 'haystack',
    headerLinks: [
        {doc: 'about/introduction', label: 'Docs'},
        {page: 'help', label: 'Help'},
    ],
    headerIcon: 'img/logo/logo.png',
    footerIcon: 'img/logo/logo.png',
    favicon: 'img/favicon/favicon.png',
    /* colors for website */
    colors: {
        primaryColor: '#2d3750',
        secondaryColor: '#3f4d71',
        postHeaderColor: '#2d3750',
    },
    fonts: {
        myFont: [
            "Titillium Web",
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
    gaTrackingId: 'UA-109460835-4',
    scrollToTop: true,
    scrollToTopOptions: {
        zIndex: 100,
    },
    algolia: {
        apiKey: 'd35e5b975bb77d817c19162f8d0e9ec6',
        indexName: 'haystack'
    },
};

module.exports = siteConfig;
