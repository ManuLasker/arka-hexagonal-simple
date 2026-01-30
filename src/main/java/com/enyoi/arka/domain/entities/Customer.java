package com.enyoi.arka.domain.entities;

import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.Email;

import java.util.Objects;

public class Customer {
    private final CustomerId id;
    private final String name;
    private final Email email;
    private final String city;

    public Customer(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.city = builder.city;
    }

   public static Builder builder() {
       return new Builder();
   }

    public CustomerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public String getCity() {
        return city;
    }

    public static class Builder {
       private CustomerId id;
       private String name;
       private Email email;
       private String city;

       public Builder id(CustomerId id) {
           this.id = id;
           return this;
       }

       public Builder name(String name) {
           this.name = name;
           return this;
       }

       public Builder email(Email email) {
           this.email = email;
           return this;
       }

       public Builder city(String city) {
           this.city = city;
           return this;
       }

       public Customer build() {
           Objects.requireNonNull(id, "Customer id is required");
           Objects.requireNonNull(city, "Customer city is required");
           Objects.requireNonNull(email, "Customer email is required");
           Objects.requireNonNull(name, "Customer name is required");
           return new Customer(this);
       }
   }
}
