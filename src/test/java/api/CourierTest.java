package api;

import api.clients.CourierClient;
import api.models.Courier;
import api.models.CourierCredentials;
import api.utils.RandomUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.apache.http.HttpStatus.*;

public class CourierTest {
    private CourierClient courierClient;
    private Courier courier;
    private String courierId;

    @Before
    @Step("Подготовка тестовых данных и создание курьера")
    public void setUp() {
        courierClient = new CourierClient();
        courier = new Courier(
                RandomUtils.generateRandomLogin(),
                RandomUtils.generateRandomPassword(),
                RandomUtils.generateRandomFirstName()
        );

        // Создаем курьера перед каждым тестом
        Response createResponse = courierClient.createCourier(courier);
        createResponse.then()
                .assertThat()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        waitForServer(1500);
    }

    @After
    @Step("Удаление тестовых данных")
    public void tearDown() {
        if (courierId != null && !courierId.isEmpty()) {
            try {
                waitForServer(2000);
                courierClient.deleteCourier(courierId);
                System.out.println("Курьер с ID " + courierId + " удален.");
            } catch (Exception e) {
                System.out.println("Не удалось удалить курьера: " + e.getMessage());
            }
        }
    }

    private String getCourierId() {
        if (courierId == null) {
            CourierCredentials credentials = new CourierCredentials(
                    courier.getLogin(),
                    courier.getPassword()
            );
            Response loginResponse = courierClient.loginCourier(credentials);
            courierId = loginResponse.jsonPath().getString("id");
        }
        return courierId;
    }

    private void waitForServer(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForServer() {
        waitForServer(1500);
    }

    @Test
    @DisplayName("Курьер может авторизоваться")
    @Description("Проверка успешной авторизации курьера с валидными данными")
    public void courierCanLoginSuccessfully() {
        // Курьер уже создан в @Before, удаляем создание отсюда

        CourierCredentials credentials = new CourierCredentials(
                courier.getLogin(),
                courier.getPassword()
        );
        Response loginResponse = courierClient.loginCourier(credentials);

        loginResponse.then()
                .assertThat()
                .statusCode(SC_OK)
                .body("id", notNullValue());

        courierId = loginResponse.jsonPath().getString("id");
        System.out.println("Успешная авторизация. ID курьера: " + courierId);
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неправильным паролем")
    @Description("Проверка ошибки при авторизации с неправильным паролем")
    public void cannotLoginWithWrongPassword() {
        // Курьер уже создан в @Before

        CourierCredentials wrongCredentials = new CourierCredentials(
                courier.getLogin(),
                "wrong_password"
        );
        Response wrongLoginResponse = courierClient.loginCourier(wrongCredentials);
        wrongLoginResponse.then()
                .assertThat()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));

        courierId = getCourierId();
    }

    @Test
    @DisplayName("Для авторизации нужно передать все обязательные поля (логин)")
    @Description("Проверка ошибки при авторизации без логина")
    public void loginRequiresAllMandatoryFieldsLogin() {
        CourierCredentials credentialsWithoutLogin = new CourierCredentials(
                null,
                RandomUtils.generateRandomPassword()
        );
        Response response = courierClient.loginCourier(credentialsWithoutLogin);
        response.then()
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа")); // Добавлена проверка сообщения
    }

    @Test
    @DisplayName("Для авторизации нужно передать все обязательные поля (пароль)")
    @Description("Проверка ошибки при авторизации без пароля")
    public void loginRequiresAllMandatoryFieldsPassword() {
        CourierCredentials credentialsWithoutPassword = new CourierCredentials(
                RandomUtils.generateRandomLogin(),
                null
        );
        Response response = courierClient.loginCourier(credentialsWithoutPassword);
        response.then()
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Система вернёт ошибку, если неправильно указать логин")
    @Description("Проверка ошибки при авторизации с неправильным логином")
    public void loginFailsWithIncorrectLogin() {
        courierId = getCourierId();

        CourierCredentials wrongCredentials = new CourierCredentials(
                "incorrect_" + courier.getLogin(),
                courier.getPassword()
        );
        Response response = courierClient.loginCourier(wrongCredentials);
        response.then()
                .assertThat()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Если какого-то поля нет, запрос возвращает ошибку (оба поля отсутствуют)")
    @Description("Проверка ошибки при авторизации без данных")
    public void loginFailsWhenAllFieldsAreMissing() {
        CourierCredentials emptyCredentials = new CourierCredentials(null, null);
        Response response = courierClient.loginCourier(emptyCredentials);
        response.then()
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа")); // Добавлена проверка сообщения
    }

    @Test
    @DisplayName("Если авторизоваться под несуществующим пользователем, запрос возвращает ошибку")
    @Description("Проверка ошибки при авторизации несуществующего пользователя")
    public void loginFailsWithNonExistentUser() {
        CourierCredentials nonExistentCredentials = new CourierCredentials(
                "nonexistent_user_" + System.currentTimeMillis(),
                "nonexistent_password_" + System.currentTimeMillis()
        );
        Response response = courierClient.loginCourier(nonExistentCredentials);
        response.then()
                .assertThat()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Успешный запрос возвращает id")
    @Description("Проверка, что успешная авторизация возвращает id курьера")
    public void successfulLoginReturnsId() {
        // Курьер уже создан в @Before

        CourierCredentials credentials = new CourierCredentials(
                courier.getLogin(),
                courier.getPassword()
        );
        Response loginResponse = courierClient.loginCourier(credentials);

        loginResponse.then()
                .assertThat()
                .statusCode(SC_OK)
                .body("id", notNullValue())
                .body("id", greaterThan(0));

        Integer id = loginResponse.jsonPath().getInt("id");
        courierId = String.valueOf(id);
        System.out.println("Полученный ID курьера: " + id);
    }
}