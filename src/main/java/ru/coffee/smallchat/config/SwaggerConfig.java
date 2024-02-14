package ru.coffee.smallchat.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private String websocketApiDescription = "<h2> Websocket </h2> " +

            "<h3> /websocket/endpoint </h3> " +
            "метод подклчения к ws портоколу <br/><br/>" +

            "<h3> /chat/send/public </h3>" +
            "метод отправки сообщений в общий чат, <br/><br/>" +
            "принимает объект: String <br/><br/>" +

            "<h3> /topic/public </h3> " +
            "метод подписки к общему чату <br/><br/>" +
            "возвращает объект: <br/><br/> " +
            "{\n" +
            "  \"message\": \"String\",\n" +
            "  \"sendTime\": \"String\",\n" +
            "  \"producerUserId\": \"String\"\n" +
            "} <br/><br/>" +

            "<h3> /user/topic/public.error </h3> " +
            "метод подписки на обшибки при отпарвке в общий чат <br/><br/>" +
            "возвращает объект: String <br/><br/>" +

            "<h3> /chat/send/personal </h3> " +
            "метод отправки сообщений в личный чат <br/><br/>" +
            "принимает объект:  <br/><br/>" +
            "{\n" +
            "  \"message\": \"String\",\n" +
            "  \"consumerUserId\": \"String\",\n" +
            "  \"chatId\": Long\n" +
            "} <br/><br/>" +

            "<h3> /user/topic/private </h3>" +
            "метод подписки на все личные чаты <br/><br/>" +
            "возвращает объект: <br/><br/> " +
            "{\n" +
            "  \"message\": \"String\",\n" +
            "  \"sendTime\": \"String\",\n" +
            "  \"producerUserId\": \"String\",\n" +
            "  \"consumerUserId\": \"String\",\n" +
            "  \"chatId\": Long,\n" +
            "  \"itIsProducer\": Boolean\n" +
            "}<br/><br/>" +

            "<h3> /user/topic/private.error </h3> " +
            "метод подписки на обшибки при отпарвке в личные чат <br/><br/>" +
            "возвращает объект: String ";

    String descriptionSeparator = "</br><br/> <br/><br/>";

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .externalDocs(new ExternalDocumentation()
                        .description("GitHub")
                        .url("https://github.com/RedArmyIsStrongestAll/small-chat"))
                .info(new Info().title("Small-Chat API-UI")
                        .description(websocketApiDescription
                                .concat(descriptionSeparator))
                        .version("v0.0.1"));
    }
}
