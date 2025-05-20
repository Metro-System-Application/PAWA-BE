module jwt {
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires io.swagger.v3.oas.annotations;
    requires java.net.http;
    requires jjwt.api;
    requires jjwt.jackson;
    requires static lombok;
    requires org.apache.tomcat.embed.core;
    requires spring.beans;
    requires spring.boot;
    requires spring.context;
    requires spring.core;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.security.crypto;
    requires spring.security.web;
    requires spring.web;
    requires spring.webmvc;
    exports pawa_be.infrastructure.jwt;
    exports pawa_be.infrastructure.jwt.key;
    exports pawa_be.infrastructure.jwt.config;
    exports pawa_be.infrastructure.jwt.filter;
    exports pawa_be.infrastructure.jwt.misc;
    exports pawa_be.infrastructure.jwt.user_details;
}