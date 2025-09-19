package org.sopt.makers.internal.common;

import java.time.LocalDateTime;

public class Constant {

	public final static int CURRENT_GENERATION = 37;

	public final static Integer REPORT_FILTER_YEAR = 2024;
	public final static LocalDateTime START_DATE_OF_YEAR = LocalDateTime.of(REPORT_FILTER_YEAR, 1, 1, 0, 0);
	public final static LocalDateTime END_DATE_OF_YEAR = LocalDateTime.of(REPORT_FILTER_YEAR, 12, 31, 23, 59);

	// Regex
	public final static String PHONE_NUMBER_REGEX = "^(010|015)\\d{8}$";

}
