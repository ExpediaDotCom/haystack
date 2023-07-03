/*
 * Copyright 2019 Expedia Group
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

import React, {Component} from 'react';
import PropTypes from 'prop-types';

export default class Lines extends Component {
    static propTypes = {
        edges: PropTypes.array.isRequired,
        onHover: PropTypes.func.isRequired,
        onLeave: PropTypes.func.isRequired,
        tracesConsidered: PropTypes.number.isRequired
    };

    // Get the midpoint between 2 points
    midpoint = (p1, p2) => ({
        x: (p2.x - p1.x) / 2 + p1.x,
        y: (p2.y - p1.y) / 2 + p1.y
    });

    // Get a point 1/3 of the way along a line defined by 2 points
    oneThird = (p1, p2) => ({
        x: (p2.x - p1.x) / 3 + p1.x,
        y: (p2.y - p1.y) / 3 + p1.y
    });

    // Get a point 2/3 of the way along a line defined by 2 points
    twoThirds = (p1, p2) => ({
        x: (2 * (p2.x - p1.x)) / 3 + p1.x,
        y: (2 * (p2.y - p1.y)) / 3 + p1.y
    });

    // We want a b-spline (basis spline) to be calculated for the given points.
    // SVGs only have operations for bezier splines. Below we are generating a
    // bezier spline that approximates a b-spline. This is based on ideas from here:
    //   http://www.designcoding.net/how-to-draw-a-basis-spline-with-cubic-bezier-spans/
    // We are doing a couple of things differently:
    //   1) We are NOT skipping the first and last line segment when generating tangents
    //   2) We are generating a tanget between the 2/3 mark on the first segment and
    //      the 1/3 mark on the second segment in all cases.
    buildCurve = (points) => {
        if (points && points.length > 2) {
            let path = `M ${points[0].x} ${points[0].y} `;

            for (let i = 0; i < points.length - 1; i++) {
                const pt1 = points[i];
                const pt2 = points[i + 1];
                const pt3 = points[i + 2];
                const tangent1 = this.twoThirds(pt1, pt2);
                const tangent2 = pt3 ? this.oneThird(pt2, pt3) : pt2;
                const knot = pt3 ? this.midpoint(tangent1, tangent2) : pt2;
                path += `S ${tangent1.x} ${tangent1.y} ${knot.x} ${knot.y} `;
            }

            return path;
        }
        return '';
    };

    // Helper function for calculating line opacity based on count of traces for the link
    computeOpacity = (link, tracesCount) => {
        const adjustedCount = link.count || 0;

        return Math.max(adjustedCount / tracesCount, 0.2);
    };

    // Capture mouse x,y values and call the onHover prop
    handleHover(data) {
        return (event) => {
            this.props.onHover(event, event.clientX, event.clientY, data);
        };
    }

    render() {
        const {edges, onLeave, tracesConsidered} = this.props;

        return (
            <g className="lines">
                {edges.map((edge) => {
                    const pathData = this.buildCurve(edge.points);

                    const isBackward = edge.points && edge.points[0].x > edge.points[1].x;
                    const violationClass = edge.link.invalidCycleDetected && isBackward ? 'violation' : '';

                    return (
                        <path
                            key={pathData.toString().replace(/\s/g, '')}
                            className={`line ${violationClass}`}
                            d={pathData}
                            strokeOpacity={this.computeOpacity(edge.link, tracesConsidered)}
                            onMouseOver={this.handleHover(edge.link)}
                            onMouseOut={onLeave}
                            onFocus={this.handleHover(edge.link)}
                            onBlur={onLeave}
                        />
                    );
                })}
            </g>
        );
    }
}
