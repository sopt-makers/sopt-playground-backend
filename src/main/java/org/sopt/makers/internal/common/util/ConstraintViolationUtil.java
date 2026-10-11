package org.sopt.makers.internal.common.util;

import org.hibernate.exception.ConstraintViolationException;

public final class ConstraintViolationUtil {

    private ConstraintViolationUtil() {
    }

    // cause 체인에서 Hibernate 제약 위반을 찾아, 위반된 제약 이름이 일치하는지 확인한다
    public static boolean isViolated(Throwable exception, String constraintName) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException violation) {
                return constraintName.equalsIgnoreCase(violation.getConstraintName());
            }
            current = current.getCause();
        }
        return false;
    }
}
