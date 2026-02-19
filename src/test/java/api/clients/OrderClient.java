package api.clients;

import api.models.Order;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class OrderClient {
    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";

    @Step("Создание заказа")
    public Response createOrder(Order order) {
        return given()
                .header("Content-type", "application/json")
                .baseUri(BASE_URL)
                .body(order)
                .when()
                .post(Endpoints.CREATE_ORDER);
    }

    @Step("Получение списка заказов")
    public Response getOrdersList() {
        return given()
                .header("Content-type", "application/json")
                .baseUri(BASE_URL)
                .when()
                .get(Endpoints.GET_ORDERS_LIST);
    }

    @Step("Отмена заказа")
    public Response cancelOrder(int trackId) {
        return given()
                .header("Content-type", "application/json")
                .baseUri(BASE_URL)
                .body("{\"track\": " + trackId + "}")
                .when()
                .put(Endpoints.CANCEL_ORDER);
    }
}