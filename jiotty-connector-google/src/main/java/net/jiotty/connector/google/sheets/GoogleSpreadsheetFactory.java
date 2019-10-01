package net.jiotty.connector.google.sheets;

import com.google.api.services.sheets.v4.model.Spreadsheet;

interface GoogleSpreadsheetFactory {
    GoogleSpreadsheet create(Spreadsheet spreadsheet);
}
