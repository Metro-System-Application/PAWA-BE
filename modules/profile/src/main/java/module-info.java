module profile {
    requires bucket;
    requires common;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires org.hibernate.orm.core;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires jwt;
    requires spring.web;
    requires spring.security.core;
    requires spring.beans;
    requires static lombok;
    requires io.swagger.v3.oas.annotations;
    requires spring.context;
    requires com.fasterxml.jackson.databind;
    exports pawa_be.profile.external.dto;
    exports pawa_be.profile.external.service;
    exports pawa_be.profile.internal.dto;
    exports pawa_be.profile.internal.model;
    exports pawa_be.profile.internal.repository;
}