module common {
    requires jakarta.persistence;
    requires jakarta.validation;
    requires stripe.java;
    requires jwt;
    requires com.fasterxml.jackson.databind;
    requires jjwt.api;
    requires spring.web;
    requires spring.context;
    requires static lombok;
    requires io.swagger.v3.oas.annotations;
    exports pawa_be.infrastructure.common.dto;
    exports pawa_be.infrastructure.common.validation;
    exports pawa_be.infrastructure.common.validation.constant;
    exports pawa_be.infrastructure.common.validation.custom;
    exports pawa_be.infrastructure.common.validation.exceptions;
}