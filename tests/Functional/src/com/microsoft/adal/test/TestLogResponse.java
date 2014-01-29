package com.microsoft.adal.test;

import java.util.concurrent.CountDownLatch;

import com.microsoft.adal.ADALError;
import com.microsoft.adal.Logger;
import com.microsoft.adal.Logger.ILogger;
import com.microsoft.adal.Logger.LogLevel;

public class TestLogResponse {
    String tag;

    String message;

    String additionalMessage;

    LogLevel level;

    ADALError errorCode;
    
    public void reset() {
        this.tag = null;
        this.message = null;
        this.additionalMessage = null;
        this.errorCode = null;
    }
    
    public void listenForLogMessage(final String msg, final CountDownLatch signal) {
        final TestLogResponse response = this;
        
        Logger.getInstance().setExternalLogger(new ILogger() {

            @Override
            public void Log(String tag, String message, String additionalMessage, LogLevel level,
                    ADALError errorCode) {

                if (message.equals(msg)) {
                    response.tag = tag;
                    response.message = message;
                    response.additionalMessage = additionalMessage;
                    response.level = level;
                    response.errorCode = errorCode;
                    if(signal != null){
                        signal.countDown();
                    }
                }
            }
        });
    }
}