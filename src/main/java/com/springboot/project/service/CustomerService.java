package com.springboot.project.service;

import com.springboot.project.repository.CustomerRepository;
import com.springboot.project.entity.Customer;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    
    public Optional<Customer> findByAccountNumber(String accountNumber) {
        return customerRepository.findByAccountNumber(accountNumber);
    }
    
    public boolean existsByAccountNumber(String accountNumber) {
        return customerRepository.existsByAccountNumber(accountNumber);
    }
    
    public List<Customer> searchByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Customer> searchByKeyword(String keyword) {
        return customerRepository.searchByKeyword(keyword);
    }
    
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setName(updatedCustomer.getName());
                    customer.setAddress(updatedCustomer.getAddress());
                    customer.setTelephone(updatedCustomer.getTelephone());
                    customer.setEmail(updatedCustomer.getEmail());
                    // Note: accountNumber is typically not updated as it's unique identifier
                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }
    
    public Customer updateCustomerContact(Long id, String telephone, String email) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setTelephone(telephone);
                    customer.setEmail(email);
                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }
    
    public Customer updateCustomerAddress(Long id, String address) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setAddress(address);
                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }
    
    public void deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
        } else {
            throw new RuntimeException("Customer not found with id: " + id);
        }
    }
    
    public long countCustomers() {
        return customerRepository.count();
    }
    
    public boolean customerExists(Long id) {
        return customerRepository.existsById(id);
    }
    
    public Customer getCustomerWithBills(Long id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    // This will trigger lazy loading of bills
                    customer.getBills().size(); // Force initialization
                    return customer;
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }
}
