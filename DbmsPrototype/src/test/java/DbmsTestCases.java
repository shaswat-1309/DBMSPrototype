import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DbmsTestCases {
        @Test // Change the name of table in query after running test case once
        public void testCreateQuery() throws IOException {
                File folder = new File(System.getProperty("user.dir") + "/" + "Shas");
                if (!folder.exists())
                        folder.mkdir();

                String query = "create table user(id);";
                boolean expectedResult = true;
                boolean actualResult = Dbms.executeCreateQuery(query, "Shas");
                assertEquals(expectedResult, actualResult);
        }

        @Test // Change the name of table in query after running test case once
        public void test2() throws IOException {
                File folder = new File(System.getProperty("user.dir") + "/" + "Shas");
                if (!folder.exists())
                        folder.mkdir();

                String query = "create table user(id);";
                boolean expectedResult = false;
                boolean actualResult = Dbms.executeCreateQuery(query, "Shas");
                assertEquals(expectedResult, actualResult);
        }

}
