package com.tfg.tms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.tms.dao.CustomerDAO;
import com.tfg.tms.dao.CustomerRepository;
import com.tfg.tms.dao.EmployeeDAO;
import com.tfg.tms.dao.RoleRepository;
import com.tfg.tms.dto.LoginDTO;
import com.tfg.tms.dto.RegisterDTO;
import com.tfg.tms.entity.Customer;
import com.tfg.tms.entity.Employee;
import com.tfg.tms.entity.Role;
import com.tfg.tms.exceptions.CustomerAlreadyExistsException;

/*
 * This class is the implementation of the visitor service
 */

@Service
public class VisitorServiceImpl implements VisitorService {

	@Autowired
	public VisitorServiceImpl(CustomerDAO customerDAO, EmployeeDAO employeeDAO, RoleRepository roleRepository,
			CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
		this.customerDAO = customerDAO;
		this.employeeDAO = employeeDAO;
		this.roleRepository = roleRepository;
		this.customerRepository = customerRepository;
		this.passwordEncoder = passwordEncoder;
	}

	private CustomerDAO customerDAO;
	private EmployeeDAO employeeDAO;
	private RoleRepository roleRepository;
	private CustomerRepository customerRepository;
	PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public Customer getCustomerByEmail(String email) {
		return customerDAO.getCustomerByEmail(email);
	}

	@Override
	@Transactional
	public void register(RegisterDTO registerDTO) throws CustomerAlreadyExistsException {

		// if the user already exists
		if (checkIfUserExists(registerDTO.getEmail())) {
			// throw an exception
			throw new CustomerAlreadyExistsException("This email is already in use");
		}

		Customer customer = new Customer(registerDTO);
		updateCustomerRole(customer);
		encodePassword(registerDTO, customer);
		customerRepository.save(customer);
	}

	// method to add the customer role to a customer entity
	private void updateCustomerRole(Customer customer) {
		Role role = roleRepository.findByCode("customer");
		customer.addCustomerRoles(role);
	}

	// method to see if the email address is already in the database
	@Override
	@Transactional
	public boolean checkIfUserExists(String email) {
		return customerRepository.findByEmail(email) != null ? true : false;
	}

	// use Bcrypt to hash the password before saving it in the database
	private void encodePassword(RegisterDTO registerDTO, Customer customer) {
		customer.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
	}

	@Override
	@Transactional
	public Customer loginCustomer(LoginDTO loginDTO) {

		// get the customer by the entered email
		Customer customer = customerDAO.getCustomerByEmail(loginDTO.getEmail());
		// if the customer doesn't exist
		if (customer == null) {
			// return null
			return null;
		}
		// encrypt the password they enter to compare against the pw in the database
		String tempPass = passwordEncoder.encode(loginDTO.getPassword());
		// if the password is not correct
		if (!customer.getPassword().equals(tempPass)) {
			// return null
			return null;
		}
		// else the info is correct, return the customer
		return customer;
	}

	@Override
	@Transactional
	public Employee loginEmployee(LoginDTO loginDTO) {

		// get the customer by the entered email
		Employee employee = employeeDAO.getEmployeeByEmail(loginDTO.getEmail());
		// if the customer doesn't exist
		if (employee == null) {
			// return null
			return null;
		}
		// if the password is not correct
		if (!employee.getPassword().equals(loginDTO.getPassword())) {
			// return null
			return null;
		}
		// else the info is correct, return the employee
		return employee;
	}

	@Override
	@Transactional
	public Customer findByEmail(String email) {
		return customerRepository.findByEmail(email);
	}

}
