package assessments1;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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

        Employee employee = new Employee();
        Scanner scanner = new Scanner(System.in);

        // Get employee details (same as before)
        employee.setName(getValidatedString(scanner, "Enter your name: "));
        employee.setEmail(getValidatedEmail(scanner, "Enter your email: "));
        employee.setPhone(getValidatedPhone(scanner, "Enter your phone number: "));
        employee.setDob(getValidatedDateOfBirth(scanner, "Enter your Date of Birth (yyyy-MM-dd): "));
        employee.setSalary(getValidatedDouble(scanner, "Enter your salary: "));

        // Get multiple addresses (same as before)
        List<Address> addresses = new ArrayList<>();
        System.out.println("\nEnter addresses for the employee (minimum one address required):");
        boolean addMoreAddresses = true;
        while (addMoreAddresses) {
            Address address = new Address();
            address.setHouseNo(getValidatedInt(scanner, "Enter house number: "));
            address.setArea(getValidatedString(scanner, "Enter area/town/society: "));
            address.setCity(getValidatedString(scanner, "Enter city: "));
            address.setCountry(getValidatedString(scanner, "Enter country: "));
            addresses.add(address);

            // Handle "yes" or "no" input validation
            boolean validChoice = false;
            while (!validChoice) {
                System.out.print("Do you want to add another address? (yes/no): ");
                String choice = scanner.nextLine().trim().toLowerCase();
                if (choice.equals("yes")) {
                    addMoreAddresses = true;
                    validChoice = true;
                } else if (choice.equals("no")) {
                    addMoreAddresses = false;
                    validChoice = true;
                } else {
                    System.out.println("Invalid option! Please enter 'yes' or 'no'.");
                }
            }
        }
        employee.setAddresses(addresses);

        // Get departments and role/position for employee
        List<Department> departments = new ArrayList<>();
        boolean addMoreDepartments = true;

        // Fetch available department IDs
        List<Department> validDepartments = showDepartments();

        while (addMoreDepartments) {
            // Ask user to select a valid department
            Department selectedDepartment = getValidatedDepartment(scanner, "Enter the department ID for the employee: ", validDepartments);
            String role = getValidatedString(scanner, "Enter your role in this department: ");
            String head = getValidatedString(scanner, "Enter the head of this department: ");

            selectedDepartment.setRole(role);
            selectedDepartment.setHead(head);

            departments.add(selectedDepartment);

            // Ask if the user wants to add another department
            boolean validChoice = false;
            while (!validChoice) {
                System.out.print("Do you work in another department? (yes/no): ");
                String choice = scanner.nextLine().trim().toLowerCase();
                if (choice.equals("yes")) {
                    validChoice = true;
                } else if (choice.equals("no")) {
                    addMoreDepartments = false;
                    validChoice = true;
                } else {
                    System.out.println("Invalid option! Please enter 'yes' or 'no'.");
                }
            }
        }

        // Set the employee's departments
        employee.setDepartments(departments);

        // Save employee with department assignments, including role and head
        saveEmployeeToDatabase(employee, departments, addresses);

        // Fetch and display all employees with their addresses (same as before)
        System.out.println("\nFetching all employees from the database:");
        fetchEmployeesFromDatabase();
    }




    // Show departments to the user
    public static List<Department> showDepartments() {
        List<Department> departments = new ArrayList<>();
        String query = "SELECT id, name FROM Department ORDER BY id ASC";
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\nAvailable Departments:");
            int count = 1;  // To display department number as 1, 2, 3, etc.
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Department department = new Department();
                department.setId(id);
                department.setName(name);
                departments.add(department);  // Add department object to the list
                System.out.println(count + ". " + name);  // Display department number followed by name
                count++;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching departments: " + e.getMessage());
        }
        return departments;  // Return the list of department objects
    }


    // Get validated department ID from the user
    public static Department getValidatedDepartment(Scanner scanner, String prompt, List<Department> validDepartments) {
        int departmentId = -1;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            try {
                departmentId = Integer.parseInt(scanner.nextLine().trim());

                // Find the department object with the given ID
                for (Department dept : validDepartments) {
                    if (dept.getId() == departmentId) {
                        valid = true;
                        return dept;  // Return the department object
                    }
                }
                System.out.println("Invalid department ID. Please enter a valid department ID.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid department ID.");
            }
        }
        return null;  // This line should never be reached
    }



    public static int getValidatedInt(Scanner scanner, String prompt) {
        int value = -1;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            try {
                value = Integer.parseInt(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.println("Invalid input. Please enter a positive integer.");
                } else {
                    valid = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
        return value;
    }

    // Utility method to get a validated string input from the user
    public static String getValidatedString(Scanner scanner, String prompt) {
        String input = "";
        while (input.trim().isEmpty()) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
        }
        return input;
    }

    // Utility method to get validated email input from the user
    public static String getValidatedEmail(Scanner scanner, String prompt) {
        String email = "";
        String emailRegex = properties.getProperty("email.regex");
        while (!email.matches(emailRegex)) {
            System.out.print(prompt);
            email = scanner.nextLine().trim();
            if (!email.matches(emailRegex)) {
                System.out.println("Invalid email format. Please try again.");
            }
        }
        return email;
    }

    // Utility method to get validated phone number input
    public static String getValidatedPhone(Scanner scanner, String prompt) {
        String phone = "";
        String phoneRegex = properties.getProperty("phone.regex");
        while (!phone.matches(phoneRegex)) {
            System.out.print(prompt);
            phone = scanner.nextLine().trim();
            if (!phone.matches(phoneRegex)) {
                System.out.println("Invalid phone number. Please try again.");
            }
        }
        return phone;
    }

    // Utility method to get validated date of birth input


    public static String getValidatedDateOfBirth(Scanner scanner, String prompt) {
        String dob = "";
        String dobRegex = properties.getProperty("dob.regex");  // This regex can still be used for format validation
        int minYear = Integer.parseInt(properties.getProperty("min.year"));
        int maxYear = Integer.parseInt(properties.getProperty("max.year"));

        while (true) {
            System.out.print(prompt);
            dob = scanner.nextLine().trim();

            // Check if the format matches the required regex
            if (!dob.matches(dobRegex)) {
                System.out.println("Invalid Date of Birth format. Please try again.");
                continue;
            }

            try {
                // Parse the date string to a LocalDate object
                LocalDate dateOfBirth = LocalDate.parse(dob);

                // Check if the year is within the valid range
                if (dateOfBirth.getYear() < minYear || dateOfBirth.getYear() > maxYear) {
                    System.out.println("Year must be between " + minYear + " and " + maxYear + ". Please try again.");
                } else {
                    // If everything is valid, break out of the loop
                    return dob;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid Date of Birth. Please enter a valid date (yyyy-MM-dd).");
            }
        }
    }


    // Utility method to get validated double input (e.g., salary)
    public static double getValidatedDouble(Scanner scanner, String prompt) {
        double salary = 0;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            try {
                salary = Double.parseDouble(scanner.nextLine().trim());
                if (salary <= 0) {
                    System.out.println("Invalid salary. Please enter a valid amount.");
                } else {
                    valid = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number for salary.");
            }
        }
        return salary;
    }

    // Save employee to the database (including role and head in EmployeeDepartment)
    public static void saveEmployeeToDatabase(Employee employee, List<Department> departments, List<Address> addresses) {
        String insertEmployeeQuery = "INSERT INTO Employee (name, email, phone, dob, salary) VALUES (?, ?, ?, ?, ?)";
        String insertEmployeeDepartmentQuery = "INSERT INTO EmployeeDepartment (employee_id, department_id, role, head) VALUES (?, ?, ?, ?)";
        String insertAddressQuery = "INSERT INTO Address (employee_id, house_no, area, city, country) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement employeeStmt = connection.prepareStatement(insertEmployeeQuery, Statement.RETURN_GENERATED_KEYS)) {

            // Insert employee data
            employeeStmt.setString(1, employee.getName());
            employeeStmt.setString(2, employee.getEmail());
            employeeStmt.setString(3, employee.getPhone());
            employeeStmt.setString(4, employee.getDob());
            employeeStmt.setDouble(5, employee.getSalary());

            int affectedRows = employeeStmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = employeeStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int employeeId = generatedKeys.getInt(1);

                        // Insert department and role/position data
                        try (PreparedStatement deptStmt = connection.prepareStatement(insertEmployeeDepartmentQuery)) {
                            for (Department dept : departments) {
                                deptStmt.setInt(1, employeeId);
                                deptStmt.setInt(2, dept.getId());
                                deptStmt.setString(3, dept.getRole());
                                deptStmt.setString(4, dept.getHead());
                                deptStmt.addBatch();
                            }
                            deptStmt.executeBatch();
                        }

                        // Insert address data for the employee
                        try (PreparedStatement addressStmt = connection.prepareStatement(insertAddressQuery)) {
                            for (Address address : addresses) {
                                addressStmt.setInt(1, employeeId);  // Link the address to the employee
                                addressStmt.setInt(2, address.getHouseNo());
                                addressStmt.setString(3, address.getArea());
                                addressStmt.setString(4, address.getCity());
                                addressStmt.setString(5, address.getCountry());
                                addressStmt.addBatch();
                            }
                            addressStmt.executeBatch();  // Insert all addresses in batch
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error saving employee data: " + e.getMessage());
        }
    }


    // Fetch employees from the database and display their information
    public static void fetchEmployeesFromDatabase() {
        String query = "SELECT * FROM Employee";
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int employeeId = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String dob = rs.getString("dob");
                double salary = rs.getDouble("salary");

                // Display employee details in separate lines
                System.out.println("Employee ID: " + employeeId);
                System.out.println("Name: " + name);
                System.out.println("Email: " + email);
                System.out.println("Phone: " + phone);
                System.out.println("Date of Birth: " + dob);
                System.out.println("Salary: " + salary);

                // Fetch and display employee addresses
                String addressQuery = "SELECT house_no, area, city, country FROM Address WHERE employee_id = ?";
                try (PreparedStatement addressStmt = connection.prepareStatement(addressQuery)) {
                    addressStmt.setInt(1, employeeId);
                    try (ResultSet addressRs = addressStmt.executeQuery()) {
                        while (addressRs.next()) {
                            int houseNo = addressRs.getInt("house_no");
                            String area = addressRs.getString("area");
                            String city = addressRs.getString("city");
                            String country = addressRs.getString("country");

                            System.out.println("Address:");
                            System.out.println("  House No: " + houseNo);
                            System.out.println("  Area: " + area);
                            System.out.println("  City: " + city);
                            System.out.println("  Country: " + country);
                            System.out.println();  // Add a blank line after each address
                        }
                    }
                }

                // Display department information in a separate block
                String deptQuery = "SELECT d.name, ed.role, ed.head FROM Department d JOIN EmployeeDepartment ed ON d.id = ed.department_id WHERE ed.employee_id = ?";
                try (PreparedStatement deptStmt = connection.prepareStatement(deptQuery)) {
                    deptStmt.setInt(1, employeeId);
                    try (ResultSet deptRs = deptStmt.executeQuery()) {
                        while (deptRs.next()) {
                            String deptName = deptRs.getString("name");
                            String role = deptRs.getString("role");
                            String head = deptRs.getString("head");

                            System.out.println("Department: " + deptName);
                            System.out.println("Role: " + role);
                            System.out.println("Head: " + head);
                            System.out.println();  // Add a blank line between departments
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching employee data: " + e.getMessage());
        }
    }

}