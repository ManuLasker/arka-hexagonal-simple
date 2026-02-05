package com.enyoi.arka.adapters.out.repository;

import com.enyoi.arka.adapters.out.repository.entity.CustomerEntity;
import com.enyoi.arka.domain.entities.Customer;
import com.enyoi.arka.domain.ports.out.CustomerRepository;
import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.Email;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class JpaCustomerRepository implements CustomerRepository {
    private final EntityManager entityManager;

    public JpaCustomerRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

   @Override
   public Customer save(Customer customer) {
       EntityTransaction tx =  entityManager.getTransaction();
       try {
           tx.begin();
           entityManager.merge(toEntity(customer));
           tx.commit();
           return customer;
       } catch (Exception ex) {
           if (tx.isActive()) tx.rollback();
           throw ex;
       }
   }

   @Override
   public Optional<Customer> findById(CustomerId id) {
       CustomerEntity entity =  entityManager.find(CustomerEntity.class, id.value());
       return Optional.ofNullable(entity).map(this::toDomain);
   }

   @Override
   public Optional<Customer> findByEmail(String email) {
       List<CustomerEntity> entities = entityManager.createQuery(
               "FROM CustomerEntity WHERE email = :email", CustomerEntity.class )
               .setParameter("email", email).
               getResultList();
       return entities.stream().findFirst().map(this::toDomain);
   }

   @Override
   public List<Customer> findAll() {
       return entityManager.createQuery("FROM CustomerEntity", CustomerEntity.class)
               .getResultList()
               .stream()
               .map(this::toDomain)
               .toList();
   }

   @Override
   public boolean existsById(CustomerId id) {
       return findById(id).isPresent();
   }

   @Override
   public void deleteById(CustomerId id) {
       EntityTransaction tx =  entityManager.getTransaction();
       try {
           tx.begin();
           CustomerEntity entity = entityManager.find(CustomerEntity.class, id.value());
           if (entity != null) {
               entityManager.remove(entity);
           }
           tx.commit();
       } catch (Exception e) {
           if(tx.isActive()) tx.rollback();
           throw e;
       }
   }

   private CustomerEntity toEntity(Customer customer) {
       return new CustomerEntity(
               customer.getId().value(),
               customer.getName(),
               customer.getEmail().value(),
               customer.getCity()
       );
   }

   private Customer toDomain(CustomerEntity customerEntity) {
       return Customer.builder()
               .id(CustomerId.of(customerEntity.getId()))
               .name(customerEntity.getName())
               .email(Email.of(customerEntity.getEmail()))
               .city(customerEntity.getCity())
               .build();
   }
}
