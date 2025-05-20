module ticket {
    exports pawa_be.ticket.external.model;
    exports pawa_be.ticket.external.service;
    exports pawa_be.ticket.external.enumerator;
    exports pawa_be.ticket.internal.model;
    exports pawa_be.ticket.internal.repository;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires jakarta.annotation;
    requires org.apache.commons.lang3;
    requires spring.boot;
    requires spring.messaging;
    requires spring.websocket;
    requires profile;
    requires spring.beans;
    requires spring.data.jpa;
    requires common;
    requires org.slf4j;
    requires spring.context;
    requires static lombok;
    requires spring.web;
    requires com.fasterxml.jackson.databind;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
}