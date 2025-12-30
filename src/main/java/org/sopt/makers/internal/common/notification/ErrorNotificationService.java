package org.sopt.makers.internal.common.notification;

import jakarta.servlet.http.HttpServletRequest;

public interface ErrorNotificationService {
    void notifyError(Exception exception, HttpServletRequest request, String profile, String errorLocation);
    boolean shouldNotify(Exception exception, String profile);
}
