import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Dbms {

    static String username = null;

    public static void main(String[] args) throws IOException {

        // Create a Scanner object to read user input
        Scanner input = new Scanner(System.in);

        // Prompt the user to enter whether they are a new or existing user
        String choice;
        do {
            System.out.print("1. New user or 2. Existing user? 3. Exit: ");
            choice = input.nextLine();

            switch (choice) {
                case "1":
                    // If the user is new, prompt them to enter a username, password, and security code
                    System.out.print("Enter username: ");
                    username = input.nextLine();

                    // Check if a folder exists for the user's name, and create one if it doesn't
                    File folder = new File(System.getProperty("user.dir") + "/" + username);
                    if (folder.exists()) {
                        System.out.println("User already exists.");
                        break;
                    } else {
                        folder.mkdir();
                    }

                    // Prompt the user to enter a password and security code
                    System.out.print("Enter password: ");
                    String password = input.nextLine();
                    System.out.print("Enter security code: ");
                    String securityCode = input.nextLine();

                    // Hash the password and security code using MD5 and add them to the map
                    String hashedPassword = getMD5Hash(password);
                    String hashedSecurityCode = getMD5Hash(securityCode);
                    HashMap<String, String[]> userSecurityInfo = readUserSecurityInfo();
                    userSecurityInfo.put(username, new String[]{hashedPassword, hashedSecurityCode});
                    writeUserSecurityInfo(userSecurityInfo);

                    System.out.println("User registered successfully.");

                    while (true) {
                        System.out.print("Enter query: ");
                        String userQuery = input.nextLine();
                        userQuery = userQuery.toLowerCase();
                        if (userQuery.startsWith("exit")) {
                            System.out.println("Goodbye!");
                            break;
                        } else if (userQuery.startsWith("create")) {
                            executeCreateQuery(userQuery,username);
                        } else if (userQuery.startsWith("insert")) {
                            executeInsertQuery(userQuery);
                        } else if (userQuery.startsWith("select")) {
                            executeSelectQuery(userQuery);
                        } else if (userQuery.startsWith("update")) {
                            executeUpdateQuery(userQuery);
                        } else if (userQuery.startsWith("delete")) {
                            executeDeleteQuery(userQuery);
                        } else {
                            System.out.println("Invalid query.");
                        }
                    }
                    //  break;
                case "2":
                    // If the user is existing, prompt them to enter their username
                    System.out.print("Enter username: ");
                    username = input.nextLine();
                    // Check if a folder exists for the user's name, and create one if it doesn't
                    File folder2 = new File(username);
                    if (!folder2.exists()) {
                        System.out.println("User does not exist.");
                        return;
                    }

                    // Check if the user is already registered and get their password and security code
                    HashMap<String, String[]> userSecurityInfo2 = readUserSecurityInfo();
                    String[] securityInfo;
                    if (userSecurityInfo2.containsKey(username)) {
                        securityInfo = userSecurityInfo2.get(username);
                    } else {
                        System.out.println("User does not exist.");
                        return;
                    }

                    // Prompt the user to enter their password and security code for authentication
                    System.out.print("Enter password: ");
                    String enteredPassword = input.nextLine();
                    System.out.print("Enter security code: ");
                    String enteredSecurityCode = input.nextLine();

                    // Validate the password and security code
                    String hashedEnteredPassword = getMD5Hash(enteredPassword);
                    String hashedEnteredSecurityCode = getMD5Hash(enteredSecurityCode);
                    if (hashedEnteredPassword.equals(securityInfo[0]) && hashedEnteredSecurityCode.equals(securityInfo[1])) {
                        System.out.println("Authentication successful.");
                    } else {
                        System.out.println("Authentication failed.");
                        break;
                    }
                    while (true) {
                        System.out.print("Enter query: ");
                        String userQuery = input.nextLine();
                        userQuery = userQuery.toLowerCase();
                        if (userQuery.equalsIgnoreCase("exit")) {
                            System.out.println("Goodbye!");
                            break;
                        } else if (userQuery.startsWith("create")) {
                            executeCreateQuery(userQuery,username);
                        } else if (userQuery.startsWith("insert")) {
                            executeInsertQuery(userQuery);
                        } else if (userQuery.startsWith("select")) {
                            executeSelectQuery(userQuery);
                        } else if (userQuery.startsWith("update")) {
                            executeUpdateQuery(userQuery);
                        } else if (userQuery.startsWith("delete")) {
                            executeDeleteQuery(userQuery);
                        } else {
                            System.out.println("Invalid query.");
                        }
                    }
                case "3":
                    return;

                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        } while (true);
    }

    public static HashMap<String, String[]> readUserSecurityInfo() {
        HashMap<String, String[]> userSecurityInfo = new HashMap<String, String[]>();
        try {
            File file = new File("user_security_info.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] tokens = scanner.nextLine().split(",");
                String[] securityInfo = new String[]{tokens[1], tokens[2]};
                userSecurityInfo.put(tokens[0], securityInfo);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("User security info file not found.");
        }
        return userSecurityInfo;
    }

    // Write user security information to the "user_security_info.txt" file
    public static void writeUserSecurityInfo(HashMap<String, String[]> userSecurityInfo) {
        try {
            FileWriter writer = new FileWriter("user_security_info.txt");
            for (String username : userSecurityInfo.keySet()) {
                String[] securityInfo = userSecurityInfo.get(username);
                writer.write(username + "," + securityInfo[0] + "," + securityInfo[1] + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing to user security info file.");
        }
    }

    public static String getMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xff & messageDigest[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean executeCreateQuery(String query,String username) throws IOException {
        String[] queryParts = query.split("\\W");
        String tableName = queryParts[2];
        List<String> columns = new ArrayList<>();

        if (queryParts.length < 4) {
            System.out.println("Invalid query.");
            return false;
        }
        int startIndex = query.indexOf("(") + 1;
        int endIndex = query.indexOf(")", startIndex);
        String[] columnDefs = query.substring(startIndex, endIndex).split(",");

        for (String columnDef : columnDefs) {
            String columnName = columnDef.trim().split("\\s+")[0];
            columns.add(columnName);
        }

        try {
            File tableFile = new File(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");

            if (tableFile.exists()) {
                System.out.println("Table already exists!");
                return false;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile));
            writer.write(String.join(",", columns));
            writer.newLine();
            writer.close();
            System.out.println("Table created successfully!");
            return true;
        } catch (IOException e) {
            System.out.println("Error creating table: " + e.getMessage());
            return false;
        }

    }

    static boolean executeInsertQuery(String query) throws IOException {
        Pattern insertPattern = Pattern.compile("insert\\s+into\\s+(\\w+)\\s*\\(([\\w\\s,]+)\\)\\s*values\\s*\\(([\\w\\s,]+)\\);");
        Matcher matcher = insertPattern.matcher(query);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            String[] columnNames = matcher.group(2).split("\\s*,\\s*");
            String[] values = matcher.group(3).split("\\s*,\\s*");

            File tableFile = new File(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");
            if (!tableFile.exists()) {
                System.out.println("Table " + tableName + " does not exist");
                return false;
            }
            if (columnNames.length != values.length) {
                System.out.println("Column count does not match value count");
                return false;
            }
            List<String> newRecord = new ArrayList<>();
            for (int i = 0; i < columnNames.length; i++) {
                newRecord.add(values[i]);
            }

            insertWriteTable(tableName, Collections.singletonList(newRecord));
            System.out.println("Record inserted successfully");
        } else {
            System.out.println("Invalid query");
        }
        return false;
    }

    private static void insertWriteTable(String tableName, List<List<String>> tableData) throws IOException {
        File tableFile = new File(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");
        FileWriter writer = new FileWriter(tableFile, tableFile.exists());

        for (List<String> row : tableData) {
            writer.write(String.join(",", row));
            writer.write(System.lineSeparator());
        }

        writer.close();
    }

    public static void executeUpdateQuery(String query) throws IOException {
        int rows = 0;
        Pattern updatePattern = Pattern.compile("^update\\s+(\\w+)\\s+set\\s+(.+?)\\s+where\\s+(.+);", Pattern.CASE_INSENSITIVE);
        Matcher updateMatcher = updatePattern.matcher(query.trim());
        if (updateMatcher.find()) {
            String tableName = updateMatcher.group(1);
            File tableFile = new File(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");
            if (!tableFile.exists()) {

                System.out.println("Table " + tableName + " does not exist.");
                return;
            }
            String setClause = updateMatcher.group(2);
            String whereClause = updateMatcher.group(3);

            // evaluate where clause condition for each row and update the matching rows
            List<String[]> tableData = readTable(tableName);
            List<String[]> newTableData = new ArrayList<>();

            for (String[] row : tableData) {
                if (evaluateCondition(row, whereClause, tableName)) {
                    String[] setColumns = setClause.split(",");
                    for (String setColumn : setColumns) {
                        String[] setValues = setColumn.trim().split("=");
                        String columnName = setValues[0].trim();
                        int columnIndex = getColumnIndex(tableName, columnName);
                        String columnValue = setValues[1].trim();
                        row[columnIndex] = columnValue;
                    }
                    rows++;
                }

                newTableData.add(row);
            }
            if (rows == 0) {
                System.out.println("No rows updated.");
                return;
            }

            updateWriteTable(tableName, newTableData);
            System.out.println("Update query executed successfully.");
        } else {
            System.out.println("Invalid update query.");
        }
    }


    private static List<String[]> readTable(String tableName) throws IOException {
        File tableFile = new File(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");
        List<String[]> tableData = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(tableFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(",");
            tableData.add(row);
        }
        reader.close();
        return tableData;
    }

    public static void updateWriteTable(String tableName, List<String[]> newTableData) {
        try {
            FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat", false);

            List<String> lines = new ArrayList<>();
            for (String[] row : newTableData) {
                String line = String.join(",", row);
                lines.add(line);
            }

            writer.write(String.join(System.lineSeparator(), lines));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    private static boolean evaluateCondition(String[] row, String condition, String tableName) throws IOException {
        String[] conditionParts = condition.split("\\s+");
        String columnName = conditionParts[0];
        String operator = conditionParts[1];
        String value = conditionParts[2];
        String cellValue = row[getColumnIndex(tableName, columnName)];
        boolean conditionSatisfied = false;
        switch (operator) {
            case "=":
                conditionSatisfied = cellValue.equals(value);
                break;
            case ">":
                conditionSatisfied = Integer.parseInt(cellValue) > Integer.parseInt(value);
                break;
            case "<":
                conditionSatisfied = Integer.parseInt(cellValue) < Integer.parseInt(value);
                break;
            case ">=":
                conditionSatisfied = Integer.parseInt(cellValue) >= Integer.parseInt(value);
                break;
            case "<=":
                conditionSatisfied = Integer.parseInt(cellValue) <= Integer.parseInt(value);
                break;
            case "!=":
                conditionSatisfied = !cellValue.equals(value);
                break;
        }
        return conditionSatisfied;
    }

    public static int getColumnIndex(String tableName, String column) throws IOException {
        List<String[]> tableData = readTable(tableName);
        if (tableData.isEmpty() || tableData.get(0).length == 0) {
            return -1;
        }
        String[] columns = tableData.get(0);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(column)) {
                return i;
            }
        }
        return -1;
    }

//    private static String getColumnNames(String tableName) throws IOException {
//        File tableFile = new File(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");
//        BufferedReader reader = new BufferedReader(new FileReader(tableFile));
//        String columnNames = reader.readLine();
//        reader.close();
//        return columnNames;
//    }
    public static void executeDeleteQuery(String query) throws IOException {
        int rows = 0;
        Pattern pattern = Pattern.compile("delete\\s+from\\s+(\\w+)\\s+where\\s+(.+);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String tableName = matcher.group(1).trim();
            String condition = matcher.group(2).trim();
            File tableFile = new File(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");
            if (!tableFile.exists()) {

                System.out.println("Table " + tableName + " does not exist.");
                return;
            }
            List<String[]> tableData = readTable(tableName);
            List<String[]> newTableData = new ArrayList<String[]>();
            boolean found = false;
            for (String[] row : tableData) {
                if (evaluateCondition(row, condition, tableName)) {
                    found = true;
                    rows++;
                } else {
                    newTableData.add(row);
                }
            }
            if (found) {
                deleteWriteTable(tableName, newTableData);
                System.out.println(rows +" row deleted from table " + tableName);
            } else {
                System.out.println("No rows found in table " + tableName + " for condition " + condition);
            }
        } else {
            System.out.println("Invalid delete query!");
        }
    }
    private static void deleteWriteTable(String tableName, List<String[]> newTableData) {
        try {
            FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat", false);

            List<String> lines = new ArrayList<>();
            for (String[] row : newTableData) {
                String line = String.join(",", row);
                lines.add(line);
            }

            writer.write(String.join(System.lineSeparator(), lines));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
    public static String[][] executeSelectQuery(String query) throws IOException {
        String[] parts = query.split("\\s+");
        parts[parts.length-1] = parts[parts.length-1].replace(";", "");
        String tableName = parts[3];
        String[] columns = parts[1].equals("*") ? null : parts[1].split(",");
        // read table data from file
        File tableFile = new File(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");
        if (!tableFile.exists()) {

            System.out.println("Table " + tableName + " does not exist.");
            return null;
        }
        String[][] tableData = selectReadTable(tableName);

        // filter rows based on WHERE clause if present
        String[] whereParts = query.split("where");
        if (whereParts.length > 1) {
            String condition = whereParts[1].trim();
            ArrayList<String[]> newTableData = new ArrayList<>();
            for (int i = 0; i < tableData.length; i++) {
                String[] row = tableData[i];
                if (evaluateCondition(row, condition, tableName)) {
                    newTableData.add(row);
                }
            }
            tableData = newTableData.toArray(new String[newTableData.size()][]);
        }

        // filter columns
        if (columns != null) {
            int[] columnIndexes = new int[columns.length];
            for (int i = 0; i < columns.length; i++) {
                columnIndexes[i] = getColumnIndex(tableName, columns[i].trim());
            }
            String[][] newTableData = new String[tableData.length][columns.length];
            for (int i = 0; i < tableData.length; i++) {
                for (int j = 0; j < columns.length; j++) {
                    newTableData[i][j] = tableData[i][columnIndexes[j]];
                    System.out.print(newTableData[i][j]);
                    System.out.print(" ");
                }
                System.out.println("");
            }
            tableData = newTableData;
        } else { // no column filtering required
            for (int i = 0; i < tableData.length; i++) {
                for (int j = 0; j < tableData[i].length; j++) {
                    System.out.print(tableData[i][j]);
                    System.out.print(" ");
                }
                System.out.println("");
            }
        }

        return tableData;
    }

private static String[][] selectReadTable(String tableName) throws IOException {
    // open table file for reading
    FileReader fileReader = new FileReader(System.getProperty("user.dir") + "/" + username + "/" + tableName + ".shaswat");
    BufferedReader bufferedReader = new BufferedReader(fileReader);

    // read table header
    String headerLine = bufferedReader.readLine();
    String[] columnNames = headerLine.split(",");

    // read table data
    List<String[]> tableData = new ArrayList<>();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
        String[] rowData = line.split(",");
        tableData.add(rowData);
    }

    // close file
    bufferedReader.close();

    // convert list to 2D array
    String[][] result = new String[tableData.size() + 1][columnNames.length];
    result[0] = columnNames;
    for (int i = 0; i < tableData.size(); i++) {
        result[i+1] = tableData.get(i);
    }

    return result;
}

}