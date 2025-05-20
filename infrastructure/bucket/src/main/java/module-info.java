module bucket {
    requires jakarta.annotation;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.s3;
    requires spring.beans;
    requires spring.context;
    requires spring.web;
    exports pawa_be.bucket.service;
}