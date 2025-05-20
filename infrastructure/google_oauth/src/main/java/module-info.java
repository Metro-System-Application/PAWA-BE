module google.oauth {
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires google.api.client;
    requires spring.beans;
    requires spring.context;
    exports pawa_be.infrastructure.google_oauth.service;
}