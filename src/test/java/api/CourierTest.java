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
    @Step("Подготовка тестовых данных")
    public void setUp() {
        courierClient = new CourierClient();
        courier = new Courier(
                RandomUtils.generateRandomLogin(),
                RandomUtils.generateRandomPassword(),
                RandomUtils.generateRandomFirstName()
        );
    }

    @After
    @Step("Удаление тестовых данных")
    public void tearDown() {
        if (courierId != null && !courierId.isEmpty()) {
            try {
                courierClient.deleteCourier(courierId);
                System.out.println("Курьер с ID " + courierId + " удален.");
            } catch (Exception e) {
                System.out.println("Не удалось удалить курьера: " + e.getMessage());
            }
        }
    }

    // Тесты для создания курьера
    @Test
    @DisplayName("Курьера можно создать")
    @Description("Проверка успешного создания курьера с валидными данными")
    public void courierCanBeCreated() {
        Response createResponse = courierClient.createCourier(courier);
        createResponse.then()
                .assertThat()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        // Получаем ID созданного курьера для удаления
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
        Response loginResponse = courierClient.loginCourier(credentials);
        courierId = loginResponse.jsonPath().getString("id");
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    @Description("Проверка ошибки при создании курьера с существующим логином")
    public void cannotCreateDuplicateCourier() {
        // Создаем первого курьера
        Response firstCreateResponse = courierClient.createCourier(courier);
        firstCreateResponse.then()
                .assertThat()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        // Получаем ID для удаления
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
        Response loginResponse = courierClient.loginCourier(credentials);
        courierId = loginResponse.jsonPath().getString("id");

        // Пытаемся создать курьера с тем же логином
        Response secondCreateResponse = courierClient.createCourier(courier);
        secondCreateResponse.then()
                .assertThat()
                .statusCode(SC_CONFLICT)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Успешный запрос возвращает ok: true")
    @Description("Проверка, что при успешном создании курьера возвращается ok: true")
    public void createCourierReturnsOkTrue() {
        Response createResponse = courierClient.createCourier(courier);
        createResponse.then()
                .assertThat()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        // Получаем ID созданного курьера для удаления
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
        Response loginResponse = courierClient.loginCourier(credentials);
        courierId = loginResponse.jsonPath().getString("id");
    }

    @Test
    @DisplayName("Если одного из полей нет, запрос возвращает ошибку")
    @Description("Проверка ошибки при создании курьера без обязательного поля")
    public void createCourierFailsWhenFieldMissing() {
        // Создаем курьера без логина
        Courier courierWithoutLogin = new Courier(
                null,
                courier.getPassword(),
                courier.getFirstName()
        );

        Response response = courierClient.createCourier(courierWithoutLogin);
        response.then()
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Если создать пользователя с логином, который уже есть, возвращается ошибка")
    @Description("Проверка ошибки при создании курьера с существующим логином")
    public void createCourierFailsWithDuplicateLogin() {
        // Создаем первого курьера
        Response firstCreateResponse = courierClient.createCourier(courier);
        firstCreateResponse.then()
                .assertThat()
                .statusCode(SC_CREATED);

        // Получаем ID первого курьера для удаления
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
        Response loginResponse = courierClient.loginCourier(credentials);
        courierId = loginResponse.jsonPath().getString("id");

        // Пытаемся создать второго курьера с тем же логином, но другим именем
        Courier duplicateCourier = new Courier(
                courier.getLogin(),
                RandomUtils.generateRandomPassword(),
                RandomUtils.generateRandomFirstName()
        );

        Response secondCreateResponse = courierClient.createCourier(duplicateCourier);
        secondCreateResponse.then()
                .assertThat()
                .statusCode(SC_CONFLICT)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    // Тесты для авторизации
    @Test
    @DisplayName("Курьер может авторизоваться")
    @Description("Проверка успешной авторизации курьера с валидными данными")
    public void courierCanLoginSuccessfully() {
        // Сначала создаем курьера
        Response createResponse = courierClient.createCourier(courier);
        createResponse.then()
                .assertThat()
                .statusCode(SC_CREATED);

        // Затем авторизуемся
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

        courierId = loginResponse.jsonPath().getString("id");
        System.out.println("Успешная авторизация. ID курьера: " + courierId);
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неправильным паролем")
    @Description("Проверка ошибки при авторизации с неправильным паролем")
    public void cannotLoginWithWrongPassword() {
        // Создаем курьера
        Response createResponse = courierClient.createCourier(courier);
        createResponse.then()
                .assertThat()
                .statusCode(SC_CREATED);

        // Получаем ID для удаления
        CourierCredentials validCredentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
        Response loginResponse = courierClient.loginCourier(validCredentials);
        courierId = loginResponse.jsonPath().getString("id");

        // Пытаемся авторизоваться с неправильным паролем
        CourierCredentials wrongCredentials = new CourierCredentials(
                courier.getLogin(),
                "wrong_password"
        );
        Response wrongLoginResponse = courierClient.loginCourier(wrongCredentials);
        wrongLoginResponse.then()
                .assertThat()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
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
                .body("message", equalTo("Недостаточно данных для входа"));
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
        // Создаем курьера
        Response createResponse = courierClient.createCourier(courier);
        createResponse.then()
                .assertThat()
                .statusCode(SC_CREATED);

        // Получаем ID для удаления
        CourierCredentials validCredentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
        Response loginResponse = courierClient.loginCourier(validCredentials);
        courierId = loginResponse.jsonPath().getString("id");

        // Пытаемся авторизоваться с неправильным логином
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
                .body("message", equalTo("Недостаточно данных для входа"));
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
}