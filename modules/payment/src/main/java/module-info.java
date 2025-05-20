module payment {
    requires stripe;
    requires cart;
    requires ticket;
    requires org.apache.commons.lang3;
    requires profile;
    requires common;
    requires stripe.java;
    requires jakarta.validation;
    requires jakarta.persistence;
    requires jwt;
    requires spring.web;
    requires io.swagger.v3.oas.annotations;
    requires static lombok;
    requires spring.beans;
    requires spring.security.core;
    requires spring.data.commons;
    exports pawa_be.payment.external.service;
}