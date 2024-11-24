package assessments1;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;
import java.util.Scanner;

public class Assessment2 {
    private static Properties properties = new Properties();

    public static void main(String[] args) {
        String propertiesFilePath = "src/assessments1/validation.properties";

        // Load validation properties
        try (FileInputStream input = new FileInputStream(propertiesFilePath)) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Error loading properties file: " + e.getMessage());
            return;
        }

        Employee e1 = new Employee();
        Scanner scanner = new Scanner(System.in);

        // Collect Employee Data
        String name = getValidatedString(scanner, "Enter Your name: ");
        e1.setName(name);

        String email = getValidatedEmail(scanner, "Enter Your email: ");
        e1.setEmail(email);

        String phone = getValidatedPhone(scanner, "Enter Your phone number: ");
        e1.setPhone(phone);

        String dateOfBirth = getValidatedDateOfBirth(scanner, "Enter Your Date of Birth as yyyy-MM-dd ");
        e1.setDob(dateOfBirth);

        double salary = getValidatedDouble(scanner, "Enter Your salary: ");
        e1.setSalary(salary);

        int houseNo = getValidatedInt(scanner, "Enter Your house no.: ");
        e1.getAddress().setHouseNo(houseNo);

        String area = getValidatedString(scanner, "Enter Your Area/Town/Society: ");
        e1.getAddress().setArea(area);

        String city = getValidatedString(scanner, "Enter Your City: ");
        e1.getAddress().setCity(city);

        String country = getValidatedString(scanner, "Enter Your Country: ");
        e1.getAddress().setCountry(country);

        // Show available departments and get the user's choice
        showDepartments();
        int departmentId = getValidatedDepartmentId(scanner, "Enter the department ID for the employee: ");

        // Save Employee, Address, and Department to Database
        saveEmployeeToDatabase(e1, departmentId);

        // Retrieve and Display All Employees
        System.out.println("\nFetching all employees from the database:");
        fetchEmployeesFromDatabase();
    }

    // Validation Methods

    public static int getValidatedInt(Scanner scanner, String prompt) {
        int result = 0;
        boolean valid = false;
        while (!valid) {
            System.out.println(prompt);
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again!");
                continue;
            }
            try {
                result = Integer.parseInt(input);
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid integer.");
            }
        }
        return result;
    }

    public static double getValidatedDouble(Scanner scanner, String prompt) {
        double result = 0;
        boolean valid = false;
        while (!valid) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again!");
                continue;
            }
            try {
                result = Double.parseDouble(input);
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
        return result;
    }

    public static String getValidatedString(Scanner scanner, String prompt) {
        String result = "";
        boolean valid = false;
        while (!valid) {
            System.out.println(prompt);
            result = scanner.nextLine().trim();
            if (result.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again!");
            } else {
                valid = true;
            }
        }
        return result;
    }

    public static String getValidatedEmail(Scanner scanner, String prompt) {
        String email = "";
        boolean valid = false;
        String emailRegex = properties.getProperty("email.regex");
        while (!valid) {
            System.out.println(prompt);
            email = scanner.nextLine().trim();
            if (email.matches(emailRegex)) {
                valid = true;
            } else {
                System.out.println("Invalid email! Please ensure it matches the format specified.");
            }
        }
        return email;
    }

    public static String getValidatedPhone(Scanner scanner, String prompt) {
        String phone = "";
        boolean valid = false;
        String phoneRegex = properties.getProperty("phone.regex");
        while (!valid) {
            System.out.println(prompt);
            phone = scanner.nextLine().trim();
            if (phone.matches(phoneRegex)) {
                valid = true;
            } else {
                System.out.println("Invalid phone number! Make sure it matches the format specified.");
            }
        }
        return phone;
    }

    public static String getValidatedDateOfBirth(Scanner scanner, String prompt) {
        String dateOfBirth = "";
        boolean valid = false;

        String dobRegex = properties.getProperty("dob.regex"); // Ensure this regex is ^\\d{4}-\\d{2}-\\d{2}$
        int minYear = Integer.parseInt(properties.getProperty("min.year"));
        int maxYear = Integer.parseInt(properties.getProperty("max.year"));

        while (!valid) {
            System.out.println(prompt + " (format: yyyy-MM-dd)");
            dateOfBirth = scanner.nextLine().trim();

            if (dateOfBirth.matches(dobRegex)) {
                try {
                    // Parse the date to check its validity
                    LocalDate parsedDate = LocalDate.parse(dateOfBirth);
                    int year = parsedDate.getYear();

                    // Check if the year is within the specified range
                    if (year < minYear || year > maxYear) {
                        System.out.println("Year must be between " + minYear + " and " + maxYear + ". Please try again!");
                    } else {
                        valid = true; // Date is valid and within the year range
                    }
                } catch (Exception e) {
                    System.out.println("Invalid date! Please ensure it's a valid calendar date.");
                }
            } else {
                System.out.println("Invalid date format! Please use yyyy-MM-dd.");
            }
        }
        return dateOfBirth;
    }



    public static int getValidatedDepartmentId(Scanner scanner, String prompt) {
        // Fetch departments into simple arrays
        int[] departmentIds = new int[10];
        String[] departmentNames = new String[10];
        int index = 0;

        String query = "SELECT id, name FROM Department ORDER BY id ASC";
        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            System.out.println("Available Departments:");
            while (resultSet.next()) {
                departmentIds[index] = resultSet.getInt("id");
                departmentNames[index] = resultSet.getString("name");
                System.out.println(departmentIds[index] + ". " + departmentNames[index]);
                index++;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching department IDs: " + e.getMessage());
            return -1; // Exit if there was an error
        }

        // Validate user input
        int departmentId = 0;
        boolean valid = false;
        while (!valid) {
            System.out.println(prompt);
            try {
                departmentId = Integer.parseInt(scanner.nextLine().trim());
                // Check if the entered ID is in the array
                for (int i = 0; i < index; i++) {
                    if (departmentIds[i] == departmentId) {
                        valid = true;
                        System.out.println("Selected Department: " + departmentNames[i]);
                        break;
                    }
                }
                if (!valid) {
                    System.out.println("Invalid department ID. Please choose a valid ID from the list.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }

        return departmentId;
    }



    public static void showDepartments() {
        String query = "SELECT id, name FROM Department ORDER BY id ASC";

        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            System.out.println("Available Departments:");
            while (resultSet.next()) {
                int departmentId = resultSet.getInt("id");
                String departmentName = resultSet.getString("name");
                System.out.println(departmentId + ". " + departmentName);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching departments: " + e.getMessage());
        }
    }

    public static void saveEmployeeToDatabase(Employee employee, int departmentId) {
        String insertEmployeeQuery = "INSERT INTO Employee (name, email, phone, dob, salary) VALUES (?, ?, ?, ?, ?)";
        String insertAddressQuery = "INSERT INTO Address (employee_id, house_no, area, city, country) VALUES (?, ?, ?, ?, ?)";
        String insertEmployeeDepartmentQuery = "INSERT INTO Employee_Department (employee_id, department_id) VALUES (?, ?)";

        try (Connection connection = DatabaseUtil.getConnection()) {
            try (PreparedStatement employeeStmt = connection.prepareStatement(insertEmployeeQuery, Statement.RETURN_GENERATED_KEYS)) {
                employeeStmt.setString(1, employee.getName());
                employeeStmt.setString(2, employee.getEmail());
                employeeStmt.setString(3, employee.getPhone());
                employeeStmt.setDate(4, java.sql.Date.valueOf(employee.getDob())); // Directly saving as SQL date
                employeeStmt.setDouble(5, employee.getSalary());
                employeeStmt.executeUpdate();

                ResultSet generatedKeys = employeeStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int employeeId = generatedKeys.getInt(1);

                    try (PreparedStatement addressStmt = connection.prepareStatement(insertAddressQuery)) {
                        addressStmt.setInt(1, employeeId);
                        addressStmt.setInt(2, employee.getAddress().getHouseNo());
                        addressStmt.setString(3, employee.getAddress().getArea());
                        addressStmt.setString(4, employee.getAddress().getCity());
                        addressStmt.setString(5, employee.getAddress().getCountry());
                        addressStmt.executeUpdate();
                    }

                    try (PreparedStatement employeeDeptStmt = connection.prepareStatement(insertEmployeeDepartmentQuery)) {
                        employeeDeptStmt.setInt(1, employeeId);
                        employeeDeptStmt.setInt(2, departmentId);
                        employeeDeptStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error saving employee to database: " + e.getMessage());
        }
    }




    public static void fetchEmployeesFromDatabase() {
        String query = """
        SELECT e.id, e.name, e.email, e.phone, e.dob, e.salary,
               a.house_no, a.area, a.city, a.country, d.name AS department
        FROM Employee e
        JOIN Address a ON e.id = a.employee_id
        JOIN Employee_Department ed ON e.id = ed.employee_id
        JOIN Department d ON ed.department_id = d.id
        ORDER BY e.id ASC; -- Ensure consistent order
        """;

        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            System.out.println("Fetching all employees from the database:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");
                String dob = resultSet.getString("dob");
                double salary = resultSet.getDouble("salary");
                int houseNo = resultSet.getInt("house_no");
                String area = resultSet.getString("area");
                String city = resultSet.getString("city");
                String country = resultSet.getString("country");
                String department = resultSet.getString("department");

                System.out.printf("""
                Employee ID: %d
                Name: %s
                Email: %s
                Phone: %s
                DOB: %s
                Salary: %.2f
                Address: House no %d, %s, %s, %s
                Department: %s
                -----------------------------------
                """, id, name, email, phone, dob, salary, houseNo, area, city, country, department);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching employees from the database: " + e.getMessage());
        }
    }

}










class Employee {
    private String name;
    private String dob;
    private double salary;
    private String email;
    private String phone; // Added phone attribute

    private Address address; // composition

    public Employee() {
        this.address = new Address();   // created and initialized address object in employee constructor
    }



    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public double getSalary() {
        return salary;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}




class Address {
    private int houseNo;
    private String area;
    private String city;
    private String country;

    public int getHouseNo() {
        return houseNo;
    }

    public String getArea() {
        return area;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public void setHouseNo(int houseNo) {
        this.houseNo = houseNo;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}






//----DATABASE TEST

class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/EmployeeDB";
    private static final String USER = "root";
    private static final String PASSWORD = "Dsaq@123"; // Replace with your MySQL root password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
