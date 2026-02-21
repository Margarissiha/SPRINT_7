package api.clients;

import api.models.Courier;
import api.models.CourierCredentials;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class CourierClient {
    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";

    @Step("Создание курьера")
    public Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .baseUri(BASE_URL)
                .body(courier)
                .when()
                .post(Endpoints.CREATE_COURIER);
    }

    @Step("Логин курьера")
    public Response loginCourier(CourierCredentials credentials) {
        return given()
                .header("Content-type", "application/json")
                .baseUri(BASE_URL)
                .body(credentials)
                .when()
                .post(Endpoints.LOGIN_COURIER);
    }

    @Step("Удаление курьера")
    public void deleteCourier(String courierId) {
        given()
                .header("Content-type", "application/json")
                .baseUri(BASE_URL)
                .when()
                .delete(Endpoints.DELETE_COURIER + courierId);
    }
}