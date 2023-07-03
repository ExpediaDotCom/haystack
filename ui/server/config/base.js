/* istanbul ignore file */
module.exports = {
    // app port
    port: 8080,

    // use when https endpoint is needed
    // https: {
    //     keyFile: '', // path for private key file
    //     certFile: '' // path for ssh cert file
    // },

    // whether to start in cluster mode or not
    cluster: false,

    // default timeout in ms for all the downlevels from connector
    upstreamTimeout: 55000,

    graphite: {
        host: 'host',
        port: 2003
    },

    // Refresh interval for auto refreshing trends and alerts
    refreshInterval: 60000,

    // Google Analytics Tracking ID
    gaTrackingID: 'UA-XXXXXXXX-X',

    // Encoding for trends and alerts
    // base64 and periodreplacement are supported, default to noop if none provided
    encoder: 'periodreplacement',

    grpcOptions: {
        'grpc.max_receive_message_length': 10485760
    },

    // this list defines subsystems for which UI should be enabled
    // traces connector must be present in connectors config
    connectors: {
        traces: {
            // name of config connector module to use for fetching traces data from downstream
            // Options (connector) :
            //  - haystack - gets data from haystack query service
            //  - zipkin - bridge for using an existing zipkin api,
            //             zipkin connector expects a zipkin config field specifying zipkin api url,
            //             eg. zipkinUrl: 'http://<zipkin>/api/v2'}
            //  - stub - a stub used during development, will be removed in future
            //  - mock - similar to stub, but specifically for testing Service Insights
            connectorName: 'stub',
            // Override haystack connector host and port.
            // haystackHost: '127.0.0.1',
            // haystackPort: '8088',
            // interval in seconds to refresh the service and operation data from backend
            serviceRefreshIntervalInSecs: 60
        },
        trends: {
            // name of config connector module to use for fetching trends data from downstream
            // Options :
            //  - haystack - gets data from Haystack Metric Tank Setup
            //               haystack connector also expects config field specifying metricTankUrl
            //  - stub      - a stub used during development, will be removed in future
            connectorName: 'stub',
            // Feature switches
            enableServicePerformance: true,
            enableServiceLevelTrends: true
        },
        alerts: {
            // name of config connector module to use for fetching anomaly detection data from downstream
            // Options :
            //  - haystack - Gets data from Haystack adaptive alerting
            //               you must specify haystack host and port
            //  - stub - a stub used during development, will be removed in future
            connectorName: 'stub',
            //  haystackHost: 'https://<haystack>/alert-api',
            //  haystackPort: 8080,

            // frequency of alerts coming in the system
            alertFreqInSec: 300,

            // While merging the successive alerts, need a buffer time. We will accept the point if successive alert is
            // within this buffer
            alertMergeBufferTimeInSec: 60,

            subscriptions: {
                // name of config connector module to use for managing subscriptions
                // Options :
                //  - stub - a stub used during development, will be removed in future
                connectorName: 'stub',
                enabled: true
            }
        },
        serviceGraph: {
            // name of config connector module to use for fetching dependency graph data from downstream
            // options :
            // - stub - a stub used during development, will be removed in future
            // - haystack - gets data from haystack-service-graph
            //              you must specify serviceGraphUrl
            //              e.g. serviceGraphUrl: 'https://<haystack>/serviceGraph'
            connectorName: 'stub',
            windowSizeInSecs: 3600
        },
        serviceInsights: {
            // serviceInsights uses traces.connectorName
            // Service Insights is beta, so disabled by default
            enableServiceInsights: true,
            // max number of traces to retrieve
            traceLimit: 200,
            // functions to generate nodes from different types of spans
            // customize these to match tech stack, available span tags, and how you want nodes displayed
            spanTypes: {
                edge: {
                    isType: (span) => span.serviceName === 'edge',
                    nodeId: (span) => {
                        const route = span.tags.find((tag) => tag.key === 'edge.route');
                        return route ? route.value : span.serviceName;
                    },
                    nodeName: (span) => {
                        const route = span.tags.find((tag) => tag.key === 'edge.route');
                        return route ? route.value : span.serviceName;
                    }
                },
                gateway: {
                    isType: (span) => span.serviceName === 'gateway',
                    nodeId: (span) => {
                        const destination = span.tags.find((tag) => tag.key === 'gateway.destination');
                        return destination ? destination.value : span.serviceName;
                    },
                    nodeName: (span) => {
                        const datacenter = span.tags.find((tag) => tag.key === 'app.datacenter');
                        return datacenter ? datacenter.value : span.serviceName;
                    }
                },
                mesh: {
                    isType: (span) => span.serviceName === 'service-mesh',
                    nodeId: (span) => span.operationName,
                    nodeName: (span) => span.operationName
                },
                database: {
                    isType: (span) => span.tags.some((tag) => tag.key === 'db.type'),
                    nodeId: (span) => span.operationName,
                    nodeName: (span) => span.operationName,
                    databaseType: (span) => span.tags.find((tag) => tag.key === 'db.type').value
                },
                outbound: {
                    isType: (span) => {
                        const hasMergedTag = span.tags.some((tag) => tag.key === 'X-HAYSTACK-IS-MERGED-SPAN' && tag.value === true);
                        const hasClientTag = span.tags.some((tag) => tag.key === 'span.kind' && tag.value === 'client');
                        return hasMergedTag ? false : hasClientTag;
                    },
                    nodeId: (span) => span.operationName,
                    nodeName: (span) => span.operationName
                },
                service: {
                    // isType implicitly true when none of the above
                    nodeId: (span) => span.serviceName,
                    nodeName: (span) => span.serviceName
                }
            }
        },
        blobs: {
            // to enable/disable blobs decorator
            // Blobs Service endpoint (optional) can be passed with blobsUrl key to redirect blobs request
            // e.g. blobsUrl : 'https://haystack-blob-example-server:9090'
            enableBlobs: false
        }
    },
    timeWindowPresetOptions: [
        {
            shortName: '5m',
            longName: '5 minutes',
            value: 5 * 60 * 1000
        },
        {
            shortName: '15m',
            longName: '15 minutes',
            value: 15 * 60 * 1000
        },
        {
            shortName: '1h',
            longName: '1 hour',
            value: 60 * 60 * 1000
        },
        {
            shortName: '6h',
            longName: '6 hours',
            value: 6 * 60 * 60 * 1000
        },
        {
            shortName: '12h',
            longName: '12 hours',
            value: 12 * 60 * 60 * 1000
        },
        {
            shortName: '24h',
            longName: '24 hours',
            value: 24 * 60 * 60 * 1000
        },
        {
            shortName: '3d',
            longName: '3 days',
            value: 3 * 24 * 60 * 60 * 1000
        },
        {
            shortName: '7d',
            longName: '7 days',
            value: 7 * 24 * 60 * 60 * 1000
        },
        {
            shortName: '30d',
            longName: '30 days',
            value: 30 * 24 * 60 * 60 * 1000
        }
    ],

    relatedTracesOptions: [
        {
            fieldTag: 'url2',
            propertyToMatch: 'url2',
            fieldDescription: 'test trait'
        }
    ]

    // externalLinking: [
    //     {
    //         key: 'serviceName', // Searchable key to add to external link list
    //         url: 'https://my-splunk-url.com/app/search/search?q=#{key}=#{value}',
    //         label: 'Splunk-Instance-1'
    //     },
    //     {
    //         key: 'tag',
    //         tagKey: 'external-link-key', // Tag to create a link from in the span tag list
    //         url: 'https://my-other-splunk-url.com/app/search/search?q=#{key}=#{value}',
    //         label: 'Splunk-Instance-2'
    //     },
    //     {
    //         key: 'traceId', // Include traceId to add external link in trace context view
    //         url: 'https://my-splunk-url.com/app/search/search?q=traceId=#{value}',
    //         label: 'Splunk-Instance-1'
    //     }
    // ]

    // use if you need SAML back SSO auth
    //
    // enableSSO: true, // flag for enabling sso
    // saml: {
    //     entry_point: '', // SAML entrypoint
    //     issuer: '' // SAML issuer
    // },
    // sessionTimeout: 60 * 60 * 1000, // timeout for session
    // sessionSecret: 'XXXXXXXXXXXXX' // secret key for session
};
