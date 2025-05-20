module cart {
    requires spring.tx;
    requires ticket;
    requires org.apache.commons.lang3;
    requires profile;
    requires common;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires jakarta.validation;
    requires jwt;
    requires org.slf4j;
    requires io.swagger.v3.oas.annotations;
    requires static lombok;
    requires spring.web;
    requires spring.security.core;
    requires spring.context;
    requires spring.beans;
    exports pawa_be.cart.external.service;
    exports pawa_be.cart.external.dto;
}