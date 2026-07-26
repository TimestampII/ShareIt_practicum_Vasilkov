package ru.practicum.shareit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;


@Configuration
public class ClientConfig {

    @Value("${shareit-server.url}")
    private String serverUrl;

    @Bean
    public UserClient userClient(RestTemplateBuilder builder) {
        return new UserClient(buildRestTemplate(builder, "/users"));
    }

    @Bean
    public ItemClient itemClient(RestTemplateBuilder builder) {
        return new ItemClient(buildRestTemplate(builder, "/items"));
    }

    @Bean
    public BookingClient bookingClient(RestTemplateBuilder builder) {
        return new BookingClient(buildRestTemplate(builder, "/bookings"));
    }

    @Bean
    public ItemRequestClient itemRequestClient(RestTemplateBuilder builder) {
        return new ItemRequestClient(buildRestTemplate(builder, "/requests"));
    }

    private RestTemplate buildRestTemplate(RestTemplateBuilder builder, String pathPrefix) {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + pathPrefix))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }
}