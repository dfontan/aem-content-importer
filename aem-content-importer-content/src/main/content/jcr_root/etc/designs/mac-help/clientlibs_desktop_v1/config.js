/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2012 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

//define mobile slider config
if( !window.CQMobileSlider ) window.CQMobileSlider = {};

window.CQMobileSlider["geometrixx-outdoors"] = {
    DESKTOP_CSS: [
        "/etc/designs/${app}/clientlibs_desktop_v1.css"
    ],
    MOBILE_CSS: [
        "/etc/designs/${app}/clientlibs_mobile_v1.css"
    ],
    DESKTOP_MAIN_ID: "main",
    MOBILE_MAIN_ID: "main",
    DESKTOP_BODY_CLASS: [
        "page"
    ],
    MOBILE_BODY_CLASS: [
        "page-mobile"
    ]
};

