module stripe {
    requires com.google.gson;
    requires jakarta.annotation;
    requires jakarta.validation;
    requires static lombok;
    requires spring.beans;
    requires spring.context;
    requires stripe.java;
    exports pawa_be.insfrastructure.stripe.dto;
    exports pawa_be.insfrastructure.stripe.service;
}