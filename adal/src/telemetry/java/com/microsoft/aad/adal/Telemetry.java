// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.microsoft.aad.adal;

import android.annotation.SuppressLint;
import android.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Telemetry {
    private DefaultDispatcher mDispatcher = null;
    private final Map<Pair<String, String>, String> mEventTracking = new ConcurrentHashMap<Pair<String, String>, String>();
    private static final Telemetry INSTANCE = new Telemetry();

    /**
     * Method to get the singleton instance of the Telemetry object.
     * @return Telemetry object
     */
    public static synchronized Telemetry getInstance() {
        return INSTANCE;
    }

    /**
     *
     * @param dispatcher the IDispatcher interface to be registered
     * @param aggregationRequired true if client wants a single event per call to AcquireToken, false otherwise
     */
    public void registerDispatcher(final IDispatcher dispatcher, final boolean aggregationRequired) {
        if (aggregationRequired) {
            mDispatcher = new AggregatedDispatcher(dispatcher);
        } else {
            mDispatcher = new DefaultDispatcher(dispatcher);
        }
    }

    static String registerNewRequest() {
        return UUID.randomUUID().toString();
    }

    @SuppressLint("SimpleDateFormat")
    private String getCurrentTimeAsString() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ssZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    @SuppressLint("SimpleDateFormat")
    private String diffTime(final String startTime, final String stopTime) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ssZ");

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return Long.toString(dateFormat.parse(stopTime).getTime() - dateFormat.parse(startTime).getTime());
    }

    void startEvent(final String requestId, final String eventName) {
        mEventTracking.put(new Pair<>(requestId, eventName), getCurrentTimeAsString());
    }

    void stopEvent(final String requestId, final IEvents events, final String eventName) {
        final String startTime = mEventTracking.get(new Pair<>(requestId, eventName));
        final String stopTime = getCurrentTimeAsString();

        events.setEvent(EventStrings.START_TIME, startTime);
        events.setEvent(EventStrings.STOP_TIME, stopTime);

        try {
            events.setEvent(EventStrings.RESPONSE_TIME, diffTime(startTime, stopTime));
        } catch (ParseException e) {
            events.setEvent(EventStrings.RESPONSE_TIME, "0");
        }

        if (mDispatcher != null) {
            mDispatcher.receive(requestId, events);
        }
    }
}
