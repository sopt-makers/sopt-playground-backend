package org.sopt.makers.internal.external;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    @Value("${google.spread-sheet.name}")
    private String applicationName;

    @Value("${google.spread-sheet.id}")
    private String spreadsheetId;

    @Value("${google.spread-sheet.credentials}")
    private String googleCredentialsJson;

    public Sheets getSheetsService() throws IOException {
        ByteArrayInputStream credentialsStream = new ByteArrayInputStream(googleCredentialsJson.getBytes(StandardCharsets.UTF_8));

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(applicationName).build();
    }

    public void writeSheetData(List<List<Object>> values) throws IOException {
        Sheets sheetsService = getSheetsService();
        String range = applicationName + "!A:A";

        ValueRange body = new ValueRange()
                .setValues(values);

        sheetsService.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }
}