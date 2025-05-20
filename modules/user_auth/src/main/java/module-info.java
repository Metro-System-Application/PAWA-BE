module user.auth {
    requires google.oauth;
    requires payment;
    requires profile;
    requires common;
    requires jwt;
    requires io.swagger.v3.oas.annotations;
    requires org.apache.tomcat.embed.core;
    requires jakarta.validation;
    requires static lombok;
    requires spring.beans;
    requires spring.web;
    requires spring.security.core;
    requires spring.context;
    requires com.fasterxml.jackson.databind;
    requires jakarta.persistence;
    requires spring.data.jpa;
    requires google.api.client;
    requires spring.security.crypto;
}