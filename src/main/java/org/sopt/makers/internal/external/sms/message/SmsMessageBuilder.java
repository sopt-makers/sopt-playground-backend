package org.sopt.makers.internal.external.sms.message;

public interface SmsMessageBuilder {

    String buildMessage();

    String getReceiverPhone();
}
