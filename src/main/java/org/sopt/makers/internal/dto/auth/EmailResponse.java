package org.sopt.makers.internal.dto.auth;

public record EmailResponse(boolean success, String code, String message)
{}
