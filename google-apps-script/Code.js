/**
 * Google Apps Script Web App for Expense Tracker
 * Paste this in: Extensions > Apps Script in your expenses spreadsheet.
 */
function doPost(e) {
  try {
    const data = JSON.parse(e.postData.contents);
    const ss = SpreadsheetApp.getActiveSpreadsheet();
    
    // Auto-detect target sheet (e.g. 'Sept 2026') or fallback to active sheet
    const sheetName = data.sheetName || "Sept 2026";
    let sheet = ss.getSheetByName(sheetName);
    if (!sheet) {
      sheet = ss.getActiveSheet();
    }
    
    const now = new Date();
    // Format: 'd-MMM' (e.g. 11-Sep) and 'hh:mm a'
    const dateStr = Utilities.formatDate(now, "Asia/Kolkata", "d-MMM");
    const timeStr = Utilities.formatDate(now, "Asia/Kolkata", "hh:mm a");
    
    const category = data.category || "Food";
    const note = data.note || "";
    const amount = Number(data.amount) || 0;
    const onceAMonth = data.isMonthly ? "yes" : "";
    
    // Column order matching sheet: Date, Time, category, note, amount, once a month
    sheet.appendRow([dateStr, timeStr, category, note, amount, onceAMonth]);
    
    return ContentService.createTextOutput(JSON.stringify({
      status: "success",
      message: "Row added to " + sheet.getName(),
      row: [dateStr, timeStr, category, note, amount, onceAMonth]
    })).setMimeType(ContentService.MimeType.JSON);
    
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({
      status: "error",
      message: err.toString()
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

function doGet(e) {
  return ContentService.createTextOutput("Expense Tracker Webhook is Online!");
}
