// David Gully
// CS 4721
// Bookstore Project
// Spring 2014

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

public class CarpeLibrum
{
    public static void main(String[] args) throws SQLException
    {
	Bookstore carpeLibrum = new Bookstore();
	carpeLibrum.mainMenu();
    }
}

class Bookstore
{
    Scanner input = new Scanner(System.in);
    //Class.forName("com.mysql.jdbc.Driver");
    Connection con;
    Statement stmt;
    ResultSet rs;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    java.util.Date date = new java.util.Date();
    String today = dateFormat.format(date);
    
    public Bookstore()
    {
	try
	{
	    this.con = DriverManager.getConnection(
		    "jdbc:mysql://localhost:3306","root","root");
	    stmt = con.createStatement();   
	    stmt.executeUpdate("create schema carpeLibrum;");
	    createDB();
	}
	catch(SQLException ex)
	{
	    //Logger.getLogger(Bookstore.class.getName()).log(Level.SEVERE, null, ex);
	    System.out.println("Connected to preexisting database.\n");
	}
    }
    
    public void mainMenu() throws SQLException
    {
	int choice;
	String menuMain = "\nMain Menu\n" +
			  "\n\t1) I want to sign up as a new employee" +
			  "\n\t2) I am a returning employee" +
			  "\n\t3) I want to sign up as a new customer" +
			  "\n\t4) I am a returning customer" +
			  "\n\t5) Exit" +
			  "\n\nEnter your choice: ";
	System.out.println("Welcome to Carpe Librum!");
	do
	{
	    System.out.print(menuMain);
	    try
	    {
		choice = input.nextInt();
	    }
	    catch(InputMismatchException ex)
	    {
		choice = 0;
		input.nextLine();
	    }
	    switch(choice)
	    {
		case 1:
		    newEmp();
		    break;
		case 2:
		    retEmp();
		    break;
		case 3:
		    newCust();
		    break;
		case 4:
		    retCust();
		    break;
		case 5:
		    System.out.println("\nThank you for visiting Carpe Librum!");
		    stmt.close();
		    con.close();
		    break;
		default:
		    System.out.println("ERROR! Please enter a number 1-5.");
		    break;
	    }
	}
	while(choice != 5);
    }

    private void newEmp()
    {
	boolean error;
	int id;
	String last, first;
	do
	{
	    System.out.print("\nNew employee sign-up" +
			     "\n\nPlease provide the following info to proceed:" +
			     "\n\nYour assigned employee ID: ");
	    try
	    {
		id = input.nextInt();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an integer value.");
		input.nextLine();
		continue;
	    }
	    System.out.print("Your last name: ");
	    last = input.nextLine();
	    System.out.print("Your first name: ");
	    first = input.nextLine();
	    try
	    {
		stmt.executeUpdate("insert into carpeLibrum.Employees values(" +
				    id + ",'" + last + "','" + first + "');");
	    }
	    catch (SQLException ex)
	    {
		error = true;
		System.out.println("ERROR! That ID alreasy exists.");
		continue;
	    }
	    error = false;
	    System.out.println("\nDone...  Thank you.");
	}
	while(error);
    }

    private void retEmp()
    {
	boolean error;
	int id;
	String last, first;
	do
	{
	    System.out.print("\nPlease provide the following info to proceed:" +
			     "\n\nEmployee ID: ");
	    try
	    {
		id = input.nextInt();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an integer value.");
		input.nextLine();
		continue;
	    }
	    System.out.print("Your last name: ");
	    last = input.nextLine();
	    System.out.print("Your first name: ");
	    first = input.nextLine();
	    try
	    {
		rs = stmt.executeQuery("select * from carpeLibrum.Employees " +
			"where EmployeeID=" + id + " and LastName='" + last +
			"' and FirstName='" + first + "';");
	    }
	    catch(SQLException ex)
	    {
		error = true;
		System.out.println("ERROR! SQL problems in query.");
		continue;
	    }
	    try
	    {
		if(!rs.next())
		{
		    error = true;
		    System.out.println("ERROR! Employee information incorrect.");
		    continue;
		}
		retEmpMenu(id);
	    }
	    catch (SQLException ex)
	    {
		error = true;
		System.out.println("ERROR! SQL problems in result set.");
		continue;
	    }
	    error = false;
	}
	while(error);
    }
    
    private void retEmpMenu(int id)
    {
	boolean error;
	boolean stay = true;
	int choice;
	do
	{
	    System.out.print("\nMenu" +
			     "\n\n\t1) See all the orders I have been responsible for" +
			     "\n\t2) Insert a new book entry into the inventory" +
			     "\n\t3) See all the book entries in the inventory" +
			     "\n\t4) Delete a book entry from the inventory" +
			     "\n\t5) Exit" +
			     "\n\nEnter your choice: ");
	    try
	    {
		choice = input.nextInt();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an integer value.");
		input.nextLine();
		continue;
	    }
	    switch(choice)
	    {
		case 1:
		    empOrders(id);
		    break;
		case 2:
		    addBook();
		    break;
		case 3:
		    listBooks();
		    break;
		case 4:
		    rmBook();
		    break;
		case 5:
		    stay = false;
		    break;
		default:
	    }
	    error = false;
	}
	while(error || stay);
    }
    
    private void empOrders(int id)
    {
	try
	{
	    rs = stmt.executeQuery("select O.OrderID, O.CustomerID, B.Title, OD.Quantity, O.OrderDate, O.ShippedDate, O.ShipperID " +
	    "from carpeLibrum.Orders O, carpeLibrum.OrderDetails OD, carpeLibrum.Books B " +
	    "where O.OrderID = OD.OrderID and OD.BookID = B.BookID and O.EmployeeID = " + id + ";");
	    
	    System.out.printf("\n\t%8s%14s%-33s%11s%15s%15s%13s","Order ID","Customer ID","   Title","Quantity","Ordered Date","Shipped Date","Shipper ID");
	    while(rs.next())	    
	    {
		int oID = rs.getInt(1);
		int custID = rs.getInt(2);
		String title = rs.getString(3);
		int quantity = rs.getInt(4);
		String orderDate = rs.getString(5);
		String shipDate = rs.getString(6);
		int shipper = rs.getInt(7);
		
		System.out.printf("\n\t%8d%14d%3s%-30s%11d%15s%15s%13d",oID,custID,"",title,quantity,orderDate,shipDate,shipper);
	    }
	}
	catch (SQLException ex)
	{
	    System.out.println(ex);
	}
    }
    
    private void addBook()
    {
	boolean error;
	String title, author;
	double price;
	int quantity, supplier, subject;
	do
	{
	    System.out.print("\nEnter the title: ");
	    title = input.nextLine();
	    System.out.print("Enter the price: ");
	    try
	    {
		price = input.nextDouble();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an numeric value.");
		input.nextLine();
		continue;
	    }
	    System.out.print("Enter the author's name: ");
	    author = input.nextLine();
	    System.out.print("Enter quantity in stock: ");
	    try
	    {
		quantity = input.nextInt();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an integer value.");
		input.nextLine();
		continue;
	    }
	    System.out.print("Enter a supplier ID: ");
	    try
	    {
		supplier = input.nextInt();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an integer value.");
		input.nextLine();
		continue;
	    }
	    System.out.print("Enter a subject ID: ");
	    try
	    {
		subject = input.nextInt();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an integer value.");
		input.nextLine();
		continue;
	    }
	    try
	    {
		stmt.executeUpdate("insert into carpeLibrum.Books (Title,Unit_Price,"
			+ "Author,Unit_in_Stock,SupplierID,SubjectID) values ('"
			+ title + "'," + price + ",'" + author + "'," + quantity
			+ "," + supplier + "," + subject + ");");
	    }
	    catch (SQLException ex)
	    {
		System.out.println("ERROR! SQL problems in update. May have "
			+ "violated foreign key constraints.");
	    }
	    error = false;
	}
	while(error);
    }

    private void listBooks()
    {
	try
	{
	    rs = stmt.executeQuery("select BookID,Title,Unit_Price,Author,Unit_in_Stock,CompanyName,CategoryName"
		    + " from carpeLibrum.Books B,carpeLibrum.Suppliers S1,carpeLibrum.Subjects S2"
		    + " where B.SupplierID = S1.SupplierID and B.SubjectID = S2.SubjectID"
		    + " order by BookID;");
	    System.out.println("\n\tID | Title                        | Price | Author   | Units | Supplier      | Category   ");
	    while(rs.next())
	    {
		int id = rs.getInt("BookID");
		String title = rs.getString("Title");
		double price = rs.getDouble("Unit_Price");
		String author = rs.getString("Author");
		int units = rs.getInt("Unit_in_Stock");
		String supplier = rs.getString("CompanyName");
		String category = rs.getString("CategoryName");
		System.out.printf("\t%-5d%-29s%7.2f%3s%-11s%5d%3s%-16s%-11s\n",
			id,title,price,"",author,units,"",supplier,category);
	    }
	}
	catch (SQLException ex)
	{
	    System.out.println("ERROR! SQL problems in query.");
	}
    }

    private void rmBook()
    {
	boolean error = false;
	int choice = -1;
	do
	{
	    System.out.print("Enter ID of book to be deleted: ");
	    try
	    {
		choice = input.nextInt();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an integer value.");
		input.nextLine();
	    }
	}
	while(error);
	try
	{
	    stmt.executeUpdate("update carpeLibrum.Books set Unit_in_Stock=0 where BookID=" + choice + ";");
	    System.out.println("Book ID " + choice + " has been removed from stock.");
	}
	catch (SQLException ex)
	{
	    System.out.println("ERROR! That book does not exist.");
	}
    }
    
    private void newCust()
    {
	boolean error;
	String last, first, phone, address;
	int id = 0;
	do
	{
	    input.nextLine();
	    System.out.print("\nNew customer sign-up" +
			     "\n\nPlease provide the following info to proceed:" +
			     "\n\nYour last name: ");
	    last = input.nextLine();
	    System.out.print("Your first name: ");
	    first = input.nextLine();
	    System.out.print("Your phone number: ");
	    phone = input.nextLine();
	    System.out.print("Your shipping address: ");
	    address = input.nextLine();
	    try
	    {
		stmt.executeUpdate("insert into carpeLibrum.Customers(LastName,FirstName,Phone,ShippingAddress)"
			+ " values ('"+last+"','"+first+"','"+phone+"','"+address+"');");
	    }
	    catch(SQLException ex)
	    {
		error = true;
		System.out.println("ERROR! SQL problems in update. Please try again");
	    }
	    error = false;
	}
	while(error);
	System.out.println("\nThank you for registering.");
	try
	{
	    rs = stmt.executeQuery("select CustomerID from carpeLibrum.Customers "
		    + "where LastName='"+last+"' and FirstName='"+first+"' and Phone='"+phone+
		    "' and ShippingAddress='"+address+"';");
	    if(rs.next())
		id = rs.getInt(1);
	}
	catch(SQLException ex)
	{
	    System.err.println(ex);
	}
	System.out.println("\nWe have decided that your customer ID is: "+id+
		"\n(Please remember your ID, because you need it to sign in.)");
    }

    private void retCust()
    {
	boolean error;
	int id;
	String last, first;
	do
	{
	    System.out.print("\nPlease provide the following info to proceed:" +
			     "\n\nCustomer ID: ");
	    try
	    {
		id = input.nextInt();
		input.nextLine();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! You must enter an integer value.");
		input.nextLine();
		continue;
	    }
	    System.out.print("Your last name: ");
	    last = input.nextLine();
	    System.out.print("Your first name: ");
	    first = input.nextLine();
	    try
	    {
		rs = stmt.executeQuery("select * from carpeLibrum.Customers " +
			"where CustomerID=" + id + " and LastName='" + last +
			"' and FirstName='" + first + "';");
		if(rs.next())
		    retCustMenu(id);
		else
		{
		    System.out.println("ERROR! Customer information incorrect.");
		    error = true;
		    continue;
		}
	    }
	    catch(SQLException ex)
	    {
		System.out.println(ex);
	    }
	    error = false;
	}
	while(error);
    }
    
    private void retCustMenu(int id)
    {
	int choice;
	String menuMain = "\nReturning Customer Menu\n" +
			  "\n\t1) Update my profile" +
			  "\n\t2) See my current profile" +
			  "\n\t3) Place a new order" +
			  "\n\t4) See all my orders" +
			  "\n\t5) Cancel an order" +
			  "\n\t6) See all books in the inventory" +
			  "\n\t7) Exit" +
			  "\n\nEnter your choice: ";
	do
	{
	    System.out.print(menuMain);
	    try
	    {
		choice = input.nextInt();
	    }
	    catch(InputMismatchException ex)
	    {
		choice = 0;
		input.nextLine();
	    }
	    switch(choice)
	    {
		case 1:
		    updateCust(id);
		    break;
		case 2:
		    profileCust(id);
		    break;
		case 3:
		    newOrder(id);
		    break;
		case 4:
		    showOrders(id);
		    break;
		case 5:
		    cancelOrder(id);
		    break;
		case 6:
		    listBooks();
		    break;
		case 7:
		    break;
		default:
		    System.out.println("ERROR! Please enter a number 1-7.");
		    break;
	    }
	}
	while(choice != 7);
    }
    
    private void updateCust(int id)
    {
	int update = 0;
	input.nextLine();
	System.out.print("\nEnter a new shipping address: ");
	String address = input.nextLine();
	System.out.print("Enter a new phone number: ");
	String phone = input.nextLine();
	try
	{
	    update = stmt.executeUpdate("update carpeLibrum.Customers set ShippingAddress='"
		    + address +"', Phone='"+phone+"' where CustomerID="+id+";");
	}
	catch(SQLException ex)
	{
	    System.err.print(ex);
	}
	if(update == 1)
	    System.out.println("Customer information updated successfully.");
	else
	    System.out.println("Customer information update was not successful.");
    }

    private void profileCust(int id)
    {
	String last = "",first = "",phone = "",address = "";
	try
	{
	    rs = stmt.executeQuery("select * from carpeLibrum.Customers where CustomerID="+id+";");
	    while(rs.next())
	    {
		last = rs.getString(2);
		first = rs.getString(3);
		phone = rs.getString(4);
		address = rs.getString(5);
	    }
	}
	catch(SQLException ex)
	{
	    System.err.print(ex);
	}
	System.out.println("\nCustomer Profile:" +
			 "\n\t" + first + " " + last +
			 "\n\t" + address +
			 "\n\t" + phone);
    }

    private void newOrder(int id)
    {
	int customer = id;
	int employee, book, quantity, order = 0;
	boolean error;
	do
	{
	    input.nextLine();
	    System.out.print("\nNew Order Menu" +
			     "\n\nPlease provide the following info to proceed:" +
			     "\n\nEmployee ID: ");
	    try
	    {
		employee = input.nextInt();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! Please enter an integer value.");
		continue;
	    }
	    System.out.print("Please enter the Book ID: ");
	    try
	    {
		book = input.nextInt();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! Please enter an integer value.");
		continue;
	    }
	    System.out.print("Please enter the quantity: ");
	    try
	    {
		quantity = input.nextInt();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! Please enter an integer value.");
		continue;
	    }
	    try
	    {
		stmt.executeUpdate("insert into carpeLibrum.Orders(CustomerID,EmployeeID,OrderDate)"
			+ " values (" + customer + "," + employee + ",'" + today + "');");
		rs = stmt.executeQuery("select OrderID from carpeLibrum.Orders where CustomerID=" +
			customer + " and EmployeeID=" + employee + " order by OrderDate asc;");
		while(rs.next())
		{
		    order = rs.getInt(1);
		}
		stmt.executeUpdate("insert into carpeLibrum.OrderDetails values (" + 
			book + ", " + order + ", " + quantity + ");");
	    }
	    catch(SQLException ex)
	    {
		error = true;
		System.out.println("ERROR! Please check Employee and Book IDs.");
		continue;
	    }
	    error = false;
	}
	while(error);
    }

    private void showOrders(int id)
    {
	String first = "", last = "", title, oDate;
	int order, quantity;
	double price, total;
	try
	{
	    rs = stmt.executeQuery("select * from carpeLibrum.Customers where CustomerID="+id+";");
	    while(rs.next())
	    {
		last = rs.getString(2);
		first = rs.getString(3);
	    }
	    System.out.println("\nOrders for customer: " + first + " " + last);
	    rs = stmt.executeQuery("select O.OrderID, Title, Quantity, Unit_Price, OrderDate " +
		    "from carpeLibrum.Orders O, carpeLibrum.OrderDetails OD, carpeLibrum.Books B " +
		    "where O.CustomerID="+id+" and OD.OrderID=O.OrderID and OD.BookID=B.BookID");
	    System.out.printf("%-5s%-25s%10s%10s%10s%15s\n","ID","Title","Quantity","Price","Total","Order Date");
	    while(rs.next())
	    {
		order = rs.getInt(1);
		title = rs.getString(2);
		quantity = rs.getInt(3);
		price = rs.getDouble(4);
		total = quantity * price;
		oDate = rs.getString(5);
		System.out.printf("%-5d%-25s%10d%10.2f%10.2f%15s\n",order,title,quantity,price,total,oDate);
	    }
	}
	catch(SQLException ex)
	{
	    System.err.print(ex);
	}
    }

    private void cancelOrder(int id)
    {
	int order;
	boolean error;
	do
	{
	    input.nextLine();
	    System.out.print("\nEnter the order number you wish to cancel: ");
	    try
	    {
		order = input.nextInt();
	    }
	    catch(InputMismatchException ex)
	    {
		error = true;
		System.out.println("ERROR! Enter an integer value.");
		continue;
	    }
	    try
	    {
		rs = stmt.executeQuery("select * from carpeLibrum.Orders where OrderID="+
			order+" and CustomerID="+id+";");
		if(rs.next())
		{
		    stmt.executeUpdate("delete from carpeLibrum.OrderDetails where OrderID="+order+";");
		    stmt.executeUpdate("delete from carpeLibrum.Orders where OrderID="+order+";");
		    System.out.println("Order cancelled successfully.");
		}
		else
		{
		    System.out.println("ERROR! You cannot cancel that order.");
		}
	    }
	    catch(SQLException ex)
	    {
		System.out.println("ERROR! Not able to cancel order.");
	    }
	    error = false;
	}
	while(error);
    }
    
    private void createDB()
    {
	System.out.print("Attempting to create database");
	try
	{
	    Thread.sleep(750);
	    System.out.print(".");
	    Thread.sleep(750);
	    System.out.print(".");
	    Thread.sleep(750);
	    System.out.print(".");
	    Thread.sleep(750);
	}
	catch (InterruptedException ex){}
	try
	{
	    stmt.executeUpdate(
	    "create table carpeLibrum.Suppliers(" +
	    "	SupplierID		int not null auto_increment," +
	    "	CompanyName		varchar(25)," +
	    "	ContactLastName		varchar(25)," +
	    "	ContactFirstName	varchar(25)," +
	    "	Phone			char(12)," +
	    "	primary key(SupplierID));");
	    
	    stmt.executeUpdate(
	    "create table carpeLibrum.Books(" +
	    "	BookID			int not null auto_increment," +
	    "	Title			varchar(50)," +
	    "	Unit_Price		float(5,2)," +
	    "	Author			varchar(25)," +
	    "	Unit_in_Stock		int," +
	    "	SupplierID		int," +
	    "	SubjectID		int," +
	    "	primary key(BookID));");

	    stmt.executeUpdate(
	    "create table carpeLibrum.Subjects(" +
	    "	SubjectID		int not null auto_increment," +
	    "	CategoryName		varchar(25)," +
	    "	primary key(SubjectID));");

	    stmt.executeUpdate(
	    "create table carpeLibrum.OrderDetails(" +
	    "	BookID			int not null," +
	    "	OrderID			int not null," +
	    "	Quantity		int," +
	    "	primary key(BookID, OrderID));");

	    stmt.executeUpdate(
	    "create table carpeLibrum.Customers(" +
	    "	CustomerID		int not null auto_increment," +
	    "	LastName		varchar(25)," +
	    "	FirstName		varchar(25)," +
	    "	Phone			char(12)," +
	    "	ShippingAddress		varchar(50)," +
	    "	primary key(CustomerID));");
		    
	    stmt.executeUpdate(
	    "create table carpeLibrum.Orders(" +
	    "	OrderID			int not null auto_increment," +
	    "	CustomerID		int," +
	    "	EmployeeID		int," +
	    "	OrderDate		date," +
	    "	ShippedDate		date," +
	    "	ShipperID		int," +
	    "	primary key(OrderID));");

	    stmt.executeUpdate(
	    "create table carpeLibrum.Employees(" +
	    "	EmployeeID		int not null," +
	    "	LastName		varchar(25)," +
	    "	FirstName		varchar(25)," +
	    "	primary key(EmployeeID));");

	    stmt.executeUpdate(
	    "create table carpeLibrum.Shippers(" +
	    "	ShipperID		int not null auto_increment," +
	    "	ShipperName		varchar(25)," +
	    "	primary key(ShipperID));");

	    stmt.executeUpdate(
	    "alter table carpeLibrum.Books" +
	    "	add foreign key(SupplierID)" +
	    "	references carpeLibrum.Suppliers(SupplierID)," +
	    "	add foreign key(SubjectID)" +
	    "	references carpeLibrum.Subjects(SubjectID);");

	    stmt.executeUpdate(
	    "alter table carpeLibrum.OrderDetails" +
	    "	add foreign key(BookID)" +
	    "	references carpeLibrum.Books(BookID)," +
	    "	add foreign key(OrderID)" +
	    "	references carpeLibrum.Orders(OrderID);");

	    stmt.executeUpdate(
	    "alter table carpeLibrum.Orders" +
	    "	add foreign key(CustomerID)" +
	    "	references carpeLibrum.Customers(CustomerID)," +
	    "	add foreign key(EmployeeID)" +
	    "	references carpeLibrum.Employees(EmployeeID)," +
	    "	add foreign key(ShipperID)" +
	    "	references carpeLibrum.Shippers(ShipperID);");

	    stmt.executeUpdate(
	    "insert into carpeLibrum.Suppliers(CompanyName,ContactLastName,ContactFirstName,Phone) values" +
	    "('Amazon','Hamilton','Laurell','605-145-1875')," +
	    "('Ebay','Koontz','Dean','605-244-1104')," +
	    "('Booksamillion','Roberts','Nora','229-412-2004')," +
	    "('University','Carter','Stephen','229-412-2004');");
	    
	    stmt.executeUpdate(
	    "insert into carpeLibrum.Subjects(CategoryName) values" +
	    "('Fiction'),('History'),('Travel'),('Technology');");

	    stmt.executeUpdate(
	    "insert into carpeLibrum.Books(Title,Unit_Price,Author,Unit_in_Stock,SupplierID,SubjectID) values" +
	    "('The Quickie',15.94,'James',5,3,1)," +
	    "('Blaze',13.24,'Richard',2,3,1)," +
	    "('The Navigator',14.01,'Clive',10,2,1)," +
	    "('Birmingham',19.99,'Tim',12,3,2)," +
	    "('North Carolina Ghosts',7.95,'Lynne',5,2,2)," +
	    "('Why I Still Live in Louisiana',5.95,'Ellen',30,1,3)," +
	    "('The World is Flat',30,'Thomas',17,3,4);");

	    stmt.executeUpdate(
	    "insert into carpeLibrum.Employees values" +
	    "(1,'Larson','Erik'),(2,'Steely','John');");

	    stmt.executeUpdate(
	    "insert into carpeLibrum.Shippers(ShipperName) values" +
	    "('UPS'),('USPS'),('FedEx');");

	    stmt.executeUpdate(
	    "insert into carpeLibrum.Customers(LastName,FirstName,Phone,ShippingAddress) values" +
	    "('Lee','James','229-541-4568','214 Valdosta Rd., Valdosta, GA, 31605')," +
	    "('Smith','John','334-057-0087','140 Magnolia St., Auburn, GA, 36830')," +
	    "('See','Lisa','605-054-0010','411 Maple St., Sioux Falls, SD, 57104')," +
	    "('Collins','Jackie','605-044-6582','321 W. Avenue, Sioux Falls, SD, 57104');");

	    stmt.executeUpdate(
	    "insert into carpeLibrum.Orders(CustomerID,EmployeeID,OrderDate,ShippedDate,ShipperID) values" +
	    "(1,1,'13/08/01','13/08/03',1)," +
	    "(1,2,'13/08/04',NULL,NULL)," +
	    "(2,1,'13/08/01','13/08/03',2)," +
	    "(4,2,'13/08/04','13/08/05',1);");

	    stmt.executeUpdate(
	    "insert into carpeLibrum.OrderDetails(BookID,OrderID,Quantity) values" +
	    "(1,1,2),(4,1,1),(6,2,2),(7,2,3),(5,3,1),(3,4,1),(4,4,1),(7,4,1);");
	}
	catch (SQLException ex)
	{
	    Logger.getLogger(Bookstore.class.getName()).log(Level.SEVERE, null, ex);
	}
	finally
	{
	    System.out.println(" Database created sucessfully.\n");
	}
    }
}