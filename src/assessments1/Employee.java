package assessments1;

public class Employee {
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
