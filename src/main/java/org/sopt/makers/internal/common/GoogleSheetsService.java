package org.sopt.makers.internal.common;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    @Value("${google.spread-sheet.name}")
    private String applicationName;

    @Value("${google.spread-sheet.id}")
    private String spreadsheetId;

    public Sheets getSheetsService() throws IOException {
        InputStream serviceAccountStream = GoogleSheetsService.class
                .getClassLoader()
                .getResourceAsStream("service_account.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName(applicationName)
                .build();
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